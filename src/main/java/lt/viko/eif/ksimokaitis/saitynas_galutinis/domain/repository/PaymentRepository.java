package lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByOrderByCreatedAtDescIdDesc();
}
