package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Payment;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.PaymentStatus;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.PaymentTransferRequest;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDescIdDesc();
    }

    public void transferPayment(PaymentTransferRequest request) {
        Payment payment = new Payment(
                request.getSenderAccount(),
                request.getReceiverAccount(),
                request.getAmount(),
                request.getCurrency(),
                PaymentStatus.PENDING,
                request.getDescription(),
                LocalDateTime.now()
        );

        paymentRepository.save(payment);
    }
}
