package lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CurrencyExchangeRequest {

    private Long sourceAccountId;
    private Long targetAccountId;
    private BigDecimal amount;
    private String fromCurrency;
    private String toCurrency;

}

