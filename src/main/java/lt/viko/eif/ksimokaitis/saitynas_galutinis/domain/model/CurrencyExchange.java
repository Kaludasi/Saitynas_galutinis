package lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;

@Entity
@Table(name = "currency_exchange")
public class CurrencyExchange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_account_id", nullable = false)
    private Account targetAccount;

    @Column(name = "source_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal sourceAmount;

    @JdbcTypeCode(Types.CHAR)
    @Column(name = "source_currency", nullable = false, columnDefinition = "char(3)")
    private String sourceCurrency;

    @Column(name = "target_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal targetAmount;

    @JdbcTypeCode(Types.CHAR)
    @Column(name = "target_currency", nullable = false, columnDefinition = "char(3)")
    private String targetCurrency;

    @Column(name = "exchange_rate", nullable = false, precision = 19, scale = 8)
    private BigDecimal exchangeRate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected CurrencyExchange() {
    }

    public CurrencyExchange(
            Account sourceAccount,
            Account targetAccount,
            BigDecimal sourceAmount,
            String sourceCurrency,
            BigDecimal targetAmount,
            String targetCurrency,
            BigDecimal exchangeRate
    ) {
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.sourceAmount = sourceAmount;
        this.sourceCurrency = sourceCurrency;
        this.targetAmount = targetAmount;
        this.targetCurrency = targetCurrency;
        this.exchangeRate = exchangeRate;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Account getSourceAccount() {
        return sourceAccount;
    }

    public Account getTargetAccount() {
        return targetAccount;
    }

    public BigDecimal getSourceAmount() {
        return sourceAmount;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
