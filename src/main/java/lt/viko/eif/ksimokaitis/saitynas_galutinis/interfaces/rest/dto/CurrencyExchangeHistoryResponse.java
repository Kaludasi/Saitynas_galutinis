package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CurrencyExchangeHistoryResponse(
        Long id,
        Long sourceAccountId,
        String sourceAccountIban,
        Long targetAccountId,
        String targetAccountIban,
        BigDecimal sourceAmount,
        String sourceCurrency,
        BigDecimal targetAmount,
        String targetCurrency,
        BigDecimal exchangeRate,
        LocalDateTime createdAt
) {
}

