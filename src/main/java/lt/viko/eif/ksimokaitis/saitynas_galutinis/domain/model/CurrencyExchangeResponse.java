package lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model;

import java.math.BigDecimal;

public record CurrencyExchangeResponse(

        BigDecimal amount,
        String fromCurrency,
        String toCurrency,
        BigDecimal exchangeRate,
        BigDecimal convertedAmount
) {
}
