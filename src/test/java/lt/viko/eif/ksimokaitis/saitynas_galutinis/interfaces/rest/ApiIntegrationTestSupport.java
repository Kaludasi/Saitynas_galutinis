package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Account;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.CurrencyExchange;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Payment;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.PaymentStatus;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.AccountRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.CurrencyExchangeRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.PaymentRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserEntity;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Iterator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
abstract class ApiIntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected AppUserJpaRepository appUserJpaRepository;

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected PaymentRepository paymentRepository;

    @Autowired
    protected CurrencyExchangeRepository currencyExchangeRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        currencyExchangeRepository.deleteAll();
        paymentRepository.deleteAll();
        accountRepository.deleteAll();
        appUserJpaRepository.deleteAll();
    }

    protected AppUserEntity createUser(String username, String email, String rawPassword) {
        AppUserEntity user = new AppUserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole("USER");
        user.setEnabled(true);
        return appUserJpaRepository.save(user);
    }

    protected Account createAccount(String iban, String ownerName, String currency, BigDecimal balance, Long appUserId) {
        return accountRepository.save(new Account(
                iban,
                ownerName,
                balance,
                currency,
                appUserId,
                LocalDateTime.now()
        ));
    }

    protected Payment createPayment(
            String senderAccount,
            String receiverAccount,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            String description
    ) {
        return paymentRepository.save(new Payment(
                senderAccount,
                receiverAccount,
                amount,
                currency,
                status,
                description,
                LocalDateTime.now()
        ));
    }

    protected CurrencyExchange createExchange(
            Account sourceAccount,
            Account targetAccount,
            BigDecimal sourceAmount,
            BigDecimal targetAmount,
            BigDecimal exchangeRate
    ) {
        return currencyExchangeRepository.save(new CurrencyExchange(
                sourceAccount,
                targetAccount,
                sourceAmount,
                sourceAccount.getCurrency(),
                targetAmount,
                targetAccount.getCurrency(),
                exchangeRate
        ));
    }

    protected String issueToken(String username, String password) throws Exception {
        String requestBody = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        MvcResult result = mockMvc.perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        return body.get("token").asText();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected JsonNode firstEmbeddedArray(JsonNode root) {
        JsonNode embedded = root.get("_embedded");
        if (embedded == null || !embedded.isObject()) {
            return objectMapper.createArrayNode();
        }

        Iterator<JsonNode> iterator = embedded.elements();
        return iterator.hasNext() ? iterator.next() : objectMapper.createArrayNode();
    }
}
