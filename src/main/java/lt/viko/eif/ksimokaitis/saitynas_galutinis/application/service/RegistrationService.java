package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserEntity;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserJpaRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegistrationService {

    private final AppUserJpaRepository appUserJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;

    public RegistrationService(
            AppUserJpaRepository appUserJpaRepository,
            PasswordEncoder passwordEncoder,
            AccountService accountService
    ) {
        this.appUserJpaRepository = appUserJpaRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountService = accountService;
    }

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

    public List<String> getSupportedCurrencies() {
        return accountService.getSupportedCurrencies();
    }
}
