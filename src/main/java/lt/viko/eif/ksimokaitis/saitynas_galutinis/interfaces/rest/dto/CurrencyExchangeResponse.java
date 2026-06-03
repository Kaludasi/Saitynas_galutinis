package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto;

import java.math.BigDecimal;

public record CurrencyExchangeResponse(
        Long sourceAccountId,
        String sourceAccountIban,
        Long targetAccountId,
        String targetAccountIban,
        BigDecimal amount,
        String fromCurrency,
        String toCurrency,
        BigDecimal exchangeRate,
        BigDecimal convertedAmount,
        BigDecimal sourceBalance,
        BigDecimal targetBalance
) {
}

