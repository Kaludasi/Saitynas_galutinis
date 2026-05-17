package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Account;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
    }

    @Test
    void getAllAccountsReturnsAccountsOrderedByNewestFirst() throws Exception {
        // given
        accountRepository.save(account(
                "LT111111111111111111",
                "First Owner",
                new BigDecimal("100.00"),
                "EUR",
                LocalDateTime.of(2026, 5, 10, 10, 0)
        ));
        accountRepository.save(account(
                "LT222222222222222222",
                "Second Owner",
                new BigDecimal("250.25"),
                "EUR",
                LocalDateTime.of(2026, 5, 10, 11, 0)
        ));

        // when
        ResultActions response = mockMvc.perform(get("/api/accounts"));

        // then
        response
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].iban").value("LT222222222222222222"))
                .andExpect(jsonPath("$[0].ownerName").value("Second Owner"))
                .andExpect(jsonPath("$[0].balance").value(250.25))
                .andExpect(jsonPath("$[0].currency").value("EUR"))
                .andExpect(jsonPath("$[1].iban").value("LT111111111111111111"))
                .andExpect(jsonPath("$[1].ownerName").value("First Owner"))
                .andExpect(jsonPath("$[1].balance").value(100.00))
                .andExpect(jsonPath("$[1].currency").value("EUR"));
    }

    @Test
    void getAllAccountsReturnsEmptyListWhenNoAccountsExist() throws Exception {
        // given
        accountRepository.deleteAll();

        // when
        ResultActions response = mockMvc.perform(get("/api/accounts"));

        // then
        response
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private Account account(String iban, String ownerName, BigDecimal balance, String currency, LocalDateTime createdAt) {
        return new Account(iban, ownerName, balance, currency, createdAt);
    }
}
