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

    private final String URL = "https://api.currencyapi.com/v3/latest";

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
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (request.getSourceAccountId().equals(request.getTargetAccountId())) {
            throw new IllegalArgumentException("Source and target accounts must be different");
        }

        Long appUserId = appUserJpaRepository.findByUsername(username)
                .map(AppUserEntity::getId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Account sourceAccount = accountRepository.findByIdAndAppUserId(request.getSourceAccountId(), appUserId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        Account targetAccount = accountRepository.findById(request.getTargetAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Target account not found"));

        if (sourceAccount.getCurrency().equals(targetAccount.getCurrency())) {
            throw new IllegalArgumentException("Choose accounts with different currencies");
        }

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds in the source account");
        }

        String apiUrl = UriComponentsBuilder
                .fromUriString(URL)
                .queryParam("apikey", currencyApiKey)
                .queryParam("base_currency", sourceAccount.getCurrency())
                .queryParam("currencies", targetAccount.getCurrency())
                .toUriString();

        CurrencyApiResponse apiResponse = restTemplate.getForObject(apiUrl, CurrencyApiResponse.class);
        if (apiResponse == null || apiResponse.data() == null || apiResponse.data().get(targetAccount.getCurrency()) == null) {
            throw new IllegalStateException("Exchange rate is unavailable");
        }

        BigDecimal exchangeRate = apiResponse.data().get(targetAccount.getCurrency()).value();
        BigDecimal convertedAmount = request.getAmount().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        targetAccount.setBalance(targetAccount.getBalance().add(convertedAmount));

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);
        currencyExchangeRepository.save(new CurrencyExchange(
                sourceAccount,
                targetAccount,
                request.getAmount(),
                sourceAccount.getCurrency(),
                convertedAmount,
                targetAccount.getCurrency(),
                exchangeRate
        ));
        paymentRepository.save(new Payment(
                sourceAccount.getIban(),
                targetAccount.getIban(),
                request.getAmount(),
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

        return new CurrencyExchangeResponse(
                sourceAccount.getId(),
                sourceAccount.getIban(),
                targetAccount.getId(),
                targetAccount.getIban(),
                request.getAmount(),
                sourceAccount.getCurrency(),
                targetAccount.getCurrency(),
                exchangeRate,
                convertedAmount,
                sourceAccount.getBalance(),
                targetAccount.getBalance()
        );
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
