package lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Payment;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcPaymentRepository implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    public JdbcPaymentRepository(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public List<Payment> findAll() {
        return paymentJpaRepository.findAllByOrderByCreatedAtDescIdDesc()
                .stream()
                .map(payment -> new Payment(
                        payment.getId(),
                        payment.getSenderAccount(),
                        payment.getReceiverAccount(),
                        payment.getAmount(),
                        payment.getCurrency(),
                        payment.getStatus(),
                        payment.getDescription(),
                        payment.getCreatedAt()
                ))
                .toList();
    }
}
