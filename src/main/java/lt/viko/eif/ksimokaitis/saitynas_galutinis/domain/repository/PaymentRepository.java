package lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByOrderByCreatedAtDescIdDesc();

    @Query("""
            select p
            from Payment p
            where p.senderAccount in :visibleIbans or p.receiverAccount in :visibleIbans
            order by p.createdAt desc, p.id desc
            """)
    List<Payment> findVisiblePayments(@Param("visibleIbans") List<String> visibleIbans);

    @Query("""
            select p
            from Payment p
            where p.id = :paymentId
              and (p.senderAccount in :visibleIbans or p.receiverAccount in :visibleIbans)
            """)
    Optional<Payment> findVisiblePaymentById(@Param("paymentId") Long paymentId, @Param("visibleIbans") List<String> visibleIbans);
}
