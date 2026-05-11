package lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Payment;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.PaymentStatus;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.PaymentRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcPaymentRepository implements PaymentRepository {

    private final JdbcClient jdbcClient;

    public JdbcPaymentRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<Payment> findAll() {
        return jdbcClient.sql("""
                        SELECT id,
                               sender_account,
                               receiver_account,
                               amount,
                               currency,
                               status,
                               description,
                               created_at
                        FROM payment
                        ORDER BY created_at DESC, id DESC
                        """)
                .query((rs, rowNum) -> new Payment(
                        rs.getLong("id"),
                        rs.getString("sender_account"),
                        rs.getString("receiver_account"),
                        rs.getBigDecimal("amount"),
                        rs.getString("currency"),
                        PaymentStatus.valueOf(rs.getString("status")),
                        rs.getString("description"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ))
                .list();
    }
}
