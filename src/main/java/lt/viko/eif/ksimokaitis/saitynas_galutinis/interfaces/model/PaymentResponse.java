package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        String senderAccount,
        String receiverAccount,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String description,
        LocalDateTime createdAt
) {
}
