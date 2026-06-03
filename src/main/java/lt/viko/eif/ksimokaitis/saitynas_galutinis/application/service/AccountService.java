package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Account;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.AccountRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserEntity;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserJpaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Set;

@Service
public class AccountService {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("EUR", "USD", "GBP", "PLN");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AccountRepository accountRepository;
    private final AppUserJpaRepository appUserJpaRepository;

    public AccountService(AccountRepository accountRepository, AppUserJpaRepository appUserJpaRepository) {
        this.accountRepository = accountRepository;
        this.appUserJpaRepository = appUserJpaRepository;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAllByOrderByCreatedAtDescIdDesc();
    }

    public List<Account> getAccountsForUsername(String username) {
        return accountRepository.findAllByAppUserIdOrderByCreatedAtDescIdDesc(getUserIdByUsername(username));
    }

    public Account getAccountForUsernameById(String username, Long accountId) {
        return accountRepository.findByIdAndAppUserId(accountId, getUserIdByUsername(username))
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    public String getCurrencyByAccountNumber(String accountNumber) {
        return accountRepository.findByIban(accountNumber)
                .orElseThrow()
                .getCurrency();
    }

    public List<Account> getAccountsByUsername(String username) {
        return accountRepository.findByOwnerName(username);
    }


    public Long getUserIdByUsername(String username) {
        AppUserEntity user = appUserJpaRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return user.getId();
    }

    @Transactional
    public Account openAccountForUsername(String username, String currency) {
        return openAccountForUserId(getUserIdByUsername(username), username, currency);
    }

    @Transactional
    public Account openAccountForUserId(Long appUserId, String username, String currency) {
        String normalizedUsername = username == null ? "" : username.trim();
        String normalizedCurrency = currency == null ? "" : currency.trim().toUpperCase();

        if (normalizedUsername.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }

        if (!SUPPORTED_CURRENCIES.contains(normalizedCurrency)) {
            throw new IllegalArgumentException("Unsupported currency.");
        }

        Account account = new Account(
                generateUniqueIban(),
                normalizedUsername,
                BigDecimal.ZERO.setScale(2),
                normalizedCurrency,
                appUserId,
                null
        );

        return accountRepository.save(account);
    }

    public List<String> getSupportedCurrencies() {
        return SUPPORTED_CURRENCIES.stream().sorted().toList();
    }

    private String generateUniqueIban() {
        String iban;
        do {
            iban = "LT" + randomDigits(18);
        } while (accountRepository.existsByIban(iban));
        return iban;
    }

    private String randomDigits(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(SECURE_RANDOM.nextInt(10));
        }
        return builder.toString();
    }
}
