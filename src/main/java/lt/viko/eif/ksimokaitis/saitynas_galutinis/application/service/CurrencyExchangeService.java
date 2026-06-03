package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;


import jakarta.transaction.Transactional;
import lombok.Getter;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.*;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.AccountRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.CurrencyExchangeRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.PaymentRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserEntity;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyExchangeService {

    private static final String LATEST_RATES_URL = "https://api.currencyapi.com/v3/latest";

    @Getter
    private final List<String> currencies = List.of("EUR", "USD", "GBP", "PLN");
    private final RestTemplate restTemplate = new RestTemplate();
    private final AccountRepository accountRepository;
    private final CurrencyExchangeRepository currencyExchangeRepository;
    private final PaymentRepository paymentRepository;
    private final AppUserJpaRepository appUserJpaRepository;

    @Value("${currency.api.key}")
    private String currencyApiKey;

    public CurrencyExchangeService(
            AccountRepository accountRepository,
            CurrencyExchangeRepository currencyExchangeRepository,
            PaymentRepository paymentRepository,
            AppUserJpaRepository appUserJpaRepository
    ) {
        this.accountRepository = accountRepository;
        this.currencyExchangeRepository = currencyExchangeRepository;
        this.paymentRepository = paymentRepository;
        this.appUserJpaRepository = appUserJpaRepository;
    }

    @Transactional
    public CurrencyExchangeResponse exchange(CurrencyExchangeRequest request, String username) {
        validateExchangeRequest(request);

        Long appUserId = resolveUserId(username);
        ExchangeAccounts exchangeAccounts = loadExchangeAccounts(request, appUserId);
        ExchangeOutcome exchangeOutcome = performExchange(exchangeAccounts, request.getAmount());

        return buildExchangeResponse(
                exchangeAccounts,
                request.getAmount(),
                exchangeOutcome.exchangeRate(),
                exchangeOutcome.convertedAmount()
        );
    }

    private void validateExchangeRequest(CurrencyExchangeRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (request.getSourceAccountId() == null || request.getTargetAccountId() == null) {
            throw new IllegalArgumentException("Both accounts must be selected");
        }

        if (request.getSourceAccountId().equals(request.getTargetAccountId())) {
            throw new IllegalArgumentException("Source and target accounts must be different");
        }
    }

    private Long resolveUserId(String username) {
        return appUserJpaRepository.findByUsername(username)
                .map(AppUserEntity::getId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    public List<CurrencyExchange> getExchangeHistoryForUsername(String username) {
        return currencyExchangeRepository.findAllVisibleByAppUserId(resolveUserId(username));
    }

    private ExchangeAccounts loadExchangeAccounts(CurrencyExchangeRequest request, Long appUserId) {
        Account sourceAccount = accountRepository.findByIdAndAppUserId(request.getSourceAccountId(), appUserId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        Account targetAccount = accountRepository.findByIdAndAppUserId(request.getTargetAccountId(), appUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target account not found"));

        return new ExchangeAccounts(sourceAccount, targetAccount);
    }

    protected ExchangeOutcome performExchange(ExchangeAccounts exchangeAccounts, BigDecimal sourceAmount) {
        validateExchangeAccounts(exchangeAccounts, sourceAmount);

        BigDecimal exchangeRate = fetchExchangeRate(
                exchangeAccounts.sourceAccount().getCurrency(),
                exchangeAccounts.targetAccount().getCurrency()
        );
        BigDecimal convertedAmount = calculateConvertedAmount(sourceAmount, exchangeRate);

        applyBalanceChanges(exchangeAccounts, sourceAmount, convertedAmount);
        saveExchangeOutcome(exchangeAccounts, sourceAmount, exchangeRate, convertedAmount);

        return new ExchangeOutcome(exchangeRate, convertedAmount);
    }

    private void validateExchangeAccounts(ExchangeAccounts exchangeAccounts, BigDecimal amount) {
        validateDifferentCurrencies(exchangeAccounts);
        validateSufficientFunds(exchangeAccounts.sourceAccount(), amount);
    }

    private void validateDifferentCurrencies(ExchangeAccounts exchangeAccounts) {
        if (exchangeAccounts.sourceAccount().getCurrency().equals(exchangeAccounts.targetAccount().getCurrency())) {
            throw new IllegalArgumentException("Choose accounts with different currencies");
        }
    }

    private void validateSufficientFunds(Account sourceAccount, BigDecimal amount) {
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds in the source account");
        }
    }

    protected BigDecimal fetchExchangeRate(String sourceCurrency, String targetCurrency) {
        String apiUrl = UriComponentsBuilder
                .fromUriString(LATEST_RATES_URL)
                .queryParam("apikey", currencyApiKey)
                .queryParam("base_currency", sourceCurrency)
                .queryParam("currencies", targetCurrency)
                .toUriString();

        CurrencyApiResponse apiResponse = restTemplate.getForObject(apiUrl, CurrencyApiResponse.class);
        if (apiResponse == null || apiResponse.data() == null || apiResponse.data().get(targetCurrency) == null) {
            throw new IllegalStateException("Exchange rate is unavailable");
        }

        return apiResponse.data().get(targetCurrency).value();
    }

    protected BigDecimal calculateConvertedAmount(BigDecimal amount, BigDecimal exchangeRate) {
        return amount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
    }

    protected void applyBalanceChanges(ExchangeAccounts exchangeAccounts, BigDecimal sourceAmount, BigDecimal targetAmount) {
        exchangeAccounts.sourceAccount().setBalance(exchangeAccounts.sourceAccount().getBalance().subtract(sourceAmount));
        exchangeAccounts.targetAccount().setBalance(exchangeAccounts.targetAccount().getBalance().add(targetAmount));
    }

    protected CurrencyExchangeResponse buildExchangeResponse(
            ExchangeAccounts exchangeAccounts,
            BigDecimal sourceAmount,
            BigDecimal exchangeRate,
            BigDecimal convertedAmount
    ) {
        return new CurrencyExchangeResponse(
                exchangeAccounts.sourceAccount().getId(),
                exchangeAccounts.sourceAccount().getIban(),
                exchangeAccounts.targetAccount().getId(),
                exchangeAccounts.targetAccount().getIban(),
                sourceAmount,
                exchangeAccounts.sourceAccount().getCurrency(),
                exchangeAccounts.targetAccount().getCurrency(),
                exchangeRate,
                convertedAmount,
                exchangeAccounts.sourceAccount().getBalance(),
                exchangeAccounts.targetAccount().getBalance()
        );
    }

    protected void recordExchangeAudit(
            Account sourceAccount,
            Account targetAccount,
            BigDecimal sourceAmount,
            BigDecimal exchangeRate,
            BigDecimal convertedAmount
    ) {
        currencyExchangeRepository.save(new CurrencyExchange(
                sourceAccount,
                targetAccount,
                sourceAmount,
                sourceAccount.getCurrency(),
                convertedAmount,
                targetAccount.getCurrency(),
                exchangeRate
        ));
        paymentRepository.save(new Payment(
                sourceAccount.getIban(),
                targetAccount.getIban(),
                sourceAmount,
                sourceAccount.getCurrency(),
                PaymentStatus.COMPLETED,
                "Currency exchange debit to " + targetAccount.getCurrency(),
                null
        ));
        paymentRepository.save(new Payment(
                sourceAccount.getIban(),
                targetAccount.getIban(),
                convertedAmount,
                targetAccount.getCurrency(),
                PaymentStatus.COMPLETED,
                "Currency exchange credit from " + sourceAccount.getCurrency(),
                null
        ));
    }

    protected void persistExchange(Account sourceAccount, Account targetAccount) {
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);
    }

    protected void saveExchangeOutcome(
            ExchangeAccounts exchangeAccounts,
            BigDecimal sourceAmount,
            BigDecimal exchangeRate,
            BigDecimal convertedAmount
    ) {
        persistExchange(exchangeAccounts.sourceAccount(), exchangeAccounts.targetAccount());
        recordExchangeAudit(
                exchangeAccounts.sourceAccount(),
                exchangeAccounts.targetAccount(),
                sourceAmount,
                exchangeRate,
                convertedAmount
        );
    }

    protected record ExchangeAccounts(Account sourceAccount, Account targetAccount) {
    }

    protected record ExchangeOutcome(BigDecimal exchangeRate, BigDecimal convertedAmount) {
    }

    public record CurrencyApiResponse(
            Map<String, CurrencyRate> data
    ) {
    }

    public record CurrencyRate(
            String code,
            BigDecimal value
    ) {
    }
}
