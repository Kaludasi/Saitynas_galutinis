package lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CurrencyExchangeResponse {

    private Long sourceAccountId;
    private String sourceAccountIban;
    private Long targetAccountId;
    private String targetAccountIban;
    private BigDecimal amount;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal exchangeRate;
    private BigDecimal convertedAmount;
    private BigDecimal sourceBalance;
    private BigDecimal targetBalance;

}
