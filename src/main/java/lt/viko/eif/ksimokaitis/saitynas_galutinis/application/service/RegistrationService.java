package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserEntity;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserJpaRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Registers new application users and ensures an initial account is opened for them.
 */
@Service
public class RegistrationService {

    private final AppUserJpaRepository appUserJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;

    /**
     * Creates the service with collaborators needed for user registration.
     *
     * @param appUserJpaRepository user persistence gateway
     * @param passwordEncoder password encoder
     * @param accountService account opening service
     */
    public RegistrationService(
            AppUserJpaRepository appUserJpaRepository,
            PasswordEncoder passwordEncoder,
            AccountService accountService
    ) {
        this.appUserJpaRepository = appUserJpaRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountService = accountService;
    }

    /**
     * Creates a new enabled user and opens the first account in the selected currency.
     *
     * @param username desired username
     * @param email user email address
     * @param rawPassword unencrypted password
     * @param accountCurrency initial account currency
     */
    @Transactional
    public void register(String username, String email, String rawPassword, String accountCurrency) {
        if (appUserJpaRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        if (appUserJpaRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        AppUserEntity appUser = new AppUserEntity();
        appUser.setUsername(username);
        appUser.setEmail(email);
        appUser.setPasswordHash(passwordEncoder.encode(rawPassword));
        appUser.setRole("USER");
        appUser.setEnabled(true);

        AppUserEntity savedUser = appUserJpaRepository.save(appUser);
        accountService.openAccountForUserId(savedUser.getId(), savedUser.getUsername(), accountCurrency);
    }

    /**
     * Returns currencies available during registration.
     *
     * @return supported currency codes
     */
    public List<String> getSupportedCurrencies() {
        return accountService.getSupportedCurrencies();
    }
}
