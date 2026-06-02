package lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model;

import java.math.BigDecimal;

public record CurrencyExchangeRequest (

    BigDecimal amount,
    String fromCurrency,
    String toCurrency
)

{}
