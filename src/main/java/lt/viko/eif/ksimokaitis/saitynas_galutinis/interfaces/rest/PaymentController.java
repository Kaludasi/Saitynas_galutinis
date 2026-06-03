package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.PaymentService;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Payment;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.PaymentTransferRequest;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.PaymentTransferResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @PostMapping("/transfer")
    public ResponseEntity<PaymentTransferResponse> transferPayment(@RequestBody PaymentTransferRequest request) {
        paymentService.transferPayment(request);
        PaymentTransferResponse response = new PaymentTransferResponse("Payment was sent successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}