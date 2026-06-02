package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Payment;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.PaymentStatus;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
    }

    @Test
    void getAllPaymentsReturnsPaymentsOrderedByNewestFirst() throws Exception {
        paymentRepository.save(new Payment(
                "LT111111111111111111",
                "LT222222222222222222",
                new BigDecimal("25.50"),
                "EUR",
                PaymentStatus.COMPLETED,
                "First payment",
                LocalDateTime.of(2026, 5, 10, 10, 0)
        ));
        paymentRepository.save(new Payment(
                "LT333333333333333333",
                "LT444444444444444444",
                new BigDecimal("99.99"),
                "USD",
                PaymentStatus.PENDING,
                "Second payment",
                LocalDateTime.of(2026, 5, 10, 11, 0)
        ));

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].senderAccount").value("LT333333333333333333"))
                .andExpect(jsonPath("$[0].receiverAccount").value("LT444444444444444444"))
                .andExpect(jsonPath("$[0].amount").value(99.99))
                .andExpect(jsonPath("$[0].currency").value("USD"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].description").value("Second payment"))
                .andExpect(jsonPath("$[1].senderAccount").value("LT111111111111111111"))
                .andExpect(jsonPath("$[1].receiverAccount").value("LT222222222222222222"))
                .andExpect(jsonPath("$[1].amount").value(25.50))
                .andExpect(jsonPath("$[1].currency").value("EUR"))
                .andExpect(jsonPath("$[1].status").value("COMPLETED"))
                .andExpect(jsonPath("$[1].description").value("First payment"));
    }

    @Test
    void getAllPaymentsReturnsEmptyListWhenNoPaymentsExist() throws Exception {
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
