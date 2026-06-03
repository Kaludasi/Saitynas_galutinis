package lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentTransferRequest {
    private String senderAccount;
    private String receiverAccount;
    private BigDecimal amount;
    private String currency;
    private String description;
}
