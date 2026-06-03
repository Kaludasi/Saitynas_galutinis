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

/**
 * Provides account lookup and account opening operations for application users.
 */
@Service
public class AccountService {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("EUR", "USD", "GBP", "PLN");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AccountRepository accountRepository;
    private final AppUserJpaRepository appUserJpaRepository;

    /**
     * Creates the service with repositories required for account and user lookups.
     *
     * @param accountRepository account persistence gateway
     * @param appUserJpaRepository user persistence gateway
     */
    public AccountService(AccountRepository accountRepository, AppUserJpaRepository appUserJpaRepository) {
        this.accountRepository = accountRepository;
        this.appUserJpaRepository = appUserJpaRepository;
    }

    /**
     * Returns every account currently stored in the system ordered by creation time descending.
     *
     * @return ordered list of all accounts
     */
    public List<Account> getAllAccounts() {
        return accountRepository.findAllByOrderByCreatedAtDescIdDesc();
    }

    /**
     * Returns accounts visible to the authenticated user.
     *
     * @param username authenticated username
     * @return ordered list of accounts owned by the user
     */
    public List<Account> getAccountsForUsername(String username) {
        return accountRepository.findAllByAppUserIdOrderByCreatedAtDescIdDesc(getUserIdByUsername(username));
    }

    /**
     * Loads a single account for the authenticated user.
     *
     * @param username authenticated username
     * @param accountId account identifier
     * @return matching account
     */
    public Account getAccountForUsernameById(String username, Long accountId) {
        return accountRepository.findByIdAndAppUserId(accountId, getUserIdByUsername(username))
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    /**
     * Resolves currency by IBAN.
     *
     * @param accountNumber IBAN of the account
     * @return account currency code
     */
    public String getCurrencyByAccountNumber(String accountNumber) {
        return accountRepository.findByIban(accountNumber)
                .orElseThrow()
                .getCurrency();
    }

    public List<Account> getAccountsByUsername(String username) {
        return accountRepository.findByOwnerName(username);
    }

    /**
     * Resolves internal user identifier by username.
     *
     * @param username application username
     * @return user identifier
     */
    public Long getUserIdByUsername(String username) {
        AppUserEntity user = appUserJpaRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return user.getId();
    }

    /**
     * Opens a new account for the given user.
     *
     * @param username authenticated username
     * @param currency requested account currency
     * @return persisted account
     */
    @Transactional
    public Account openAccountForUsername(String username, String currency) {
        return openAccountForUserId(getUserIdByUsername(username), username, currency);
    }

    /**
     * Opens a new account for a known user identifier.
     *
     * @param appUserId owner identifier
     * @param username owner username used as the account display owner
     * @param currency requested account currency
     * @return persisted account
     */
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

    /**
     * Lists supported account currencies.
     *
     * @return sorted currency codes
     */
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
