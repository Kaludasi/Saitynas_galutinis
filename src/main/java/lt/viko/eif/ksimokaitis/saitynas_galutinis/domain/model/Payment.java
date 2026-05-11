package lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Payment(
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
