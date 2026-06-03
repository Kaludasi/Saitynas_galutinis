package lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PaymentTransferResponse {
    private String message;
    private Long paymentId;
}
