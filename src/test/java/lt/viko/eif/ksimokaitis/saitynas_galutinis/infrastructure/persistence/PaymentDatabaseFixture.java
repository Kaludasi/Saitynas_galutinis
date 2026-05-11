package lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class PaymentDatabaseFixture {

    private final JdbcClient jdbcClient;

    public PaymentDatabaseFixture(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void deleteAll() {
        jdbcClient.sql("DELETE FROM payment").update();
    }

    public void insertPayment(
            String senderAccount,
            String receiverAccount,
            String amount,
            String currency,
            String status,
            String description,
            String createdAt
    ) {
        jdbcClient.sql("""
                        INSERT INTO payment (
                            sender_account,
                            receiver_account,
                            amount,
                            currency,
                            status,
                            description,
                            created_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """)
                .param(senderAccount)
                .param(receiverAccount)
                .param(amount)
                .param(currency)
                .param(status)
                .param(description)
                .param(createdAt)
                .update();
    }
}
