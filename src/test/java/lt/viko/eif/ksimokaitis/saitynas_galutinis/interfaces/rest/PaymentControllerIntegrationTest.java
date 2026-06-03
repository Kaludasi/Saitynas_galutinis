package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;

import com.fasterxml.jackson.databind.JsonNode;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.PaymentStatus;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void paymentHistoryReturnsOnlyVisiblePayments() throws Exception {
        AppUserEntity visibleUser = createUser("payer.user", "payer.user@example.com", "Payer#2026");
        AppUserEntity hiddenUser = createUser("hidden.payer", "hidden.payer@example.com", "Hidden#2026");

        var visibleAccount = createAccount("LT555555555555555555", "payer.user", "EUR", new BigDecimal("200.00"), visibleUser.getId());
        var visibleReceiver = createAccount("LT666666666666666666", "payer.user", "USD", new BigDecimal("0.00"), visibleUser.getId());
        var hiddenAccount = createAccount("LT777777777777777777", "hidden.payer", "EUR", new BigDecimal("90.00"), hiddenUser.getId());
        var hiddenReceiver = createAccount("LT888888888888888888", "hidden.payer", "GBP", new BigDecimal("50.00"), hiddenUser.getId());

        createPayment(visibleAccount.getIban(), visibleReceiver.getIban(), new BigDecimal("20.00"), "EUR", PaymentStatus.COMPLETED, "Visible payment");
        createPayment(hiddenAccount.getIban(), hiddenReceiver.getIban(), new BigDecimal("30.00"), "EUR", PaymentStatus.COMPLETED, "Hidden payment");

        String token = issueToken("payer.user", "Payer#2026");

        MvcResult result = mockMvc.perform(get("/api/payments")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode payments = firstEmbeddedArray(root);

        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).get("description").asText()).isEqualTo("Visible payment");
    }

    @Test
    void transferPaymentCreatesCompletedPaymentForAuthorizedUser() throws Exception {
        AppUserEntity user = createUser("transfer.user", "transfer.user@example.com", "Transfer#2026");
        var sender = createAccount("LT999999999999999999", "transfer.user", "EUR", new BigDecimal("250.00"), user.getId());
        var receiver = createAccount("LT101010101010101010", "transfer.user", "EUR", BigDecimal.ZERO, user.getId());

        String token = issueToken("transfer.user", "Transfer#2026");

        mockMvc.perform(post("/api/payments/transfer")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senderAccount": "LT999999999999999999",
                                  "receiverAccount": "LT101010101010101010",
                                  "amount": 15.00,
                                  "currency": "EUR",
                                  "description": "Acceptance transfer"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Payment was sent successfully"))
                .andExpect(jsonPath("$.paymentId").isNumber());
    }

    @Test
    void getPaymentByIdReturnsOnlyVisiblePayment() throws Exception {
        AppUserEntity user = createUser("payment.viewer", "payment.viewer@example.com", "Viewer#2026");
        var sender = createAccount("LT202020202020202020", "payment.viewer", "EUR", new BigDecimal("80.00"), user.getId());
        var receiver = createAccount("LT212121212121212121", "payment.viewer", "EUR", new BigDecimal("5.00"), user.getId());
        Long paymentId = createPayment(sender.getIban(), receiver.getIban(), new BigDecimal("12.00"), "EUR", PaymentStatus.COMPLETED, "Visible payment details").getId();

        String token = issueToken("payment.viewer", "Viewer#2026");

        mockMvc.perform(get("/api/payments/{id}", paymentId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.description").value("Visible payment details"));
    }

    @Test
    void transferPaymentReturnsBadRequestWhenSenderAccountBelongsToAnotherUser() throws Exception {
        AppUserEntity actor = createUser("actor.user", "actor.user@example.com", "Actor#2026");
        AppUserEntity owner = createUser("owner.user", "owner.user@example.com", "Owner#2026");

        createAccount("LT313131313131313131", "owner.user", "EUR", new BigDecimal("40.00"), owner.getId());
        createAccount("LT323232323232323232", "actor.user", "EUR", BigDecimal.ZERO, actor.getId());

        String token = issueToken("actor.user", "Actor#2026");

        mockMvc.perform(post("/api/payments/transfer")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senderAccount": "LT313131313131313131",
                                  "receiverAccount": "LT323232323232323232",
                                  "amount": 10.00,
                                  "currency": "EUR",
                                  "description": "Forbidden transfer"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .isEqualTo("Sender account does not belong to the authorized user"));
    }
}
