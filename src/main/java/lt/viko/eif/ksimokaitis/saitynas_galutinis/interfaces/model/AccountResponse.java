package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        Long id,
        String iban,
        String ownerName,
        BigDecimal balance,
        String currency,
        LocalDateTime createdAt
) {
}
