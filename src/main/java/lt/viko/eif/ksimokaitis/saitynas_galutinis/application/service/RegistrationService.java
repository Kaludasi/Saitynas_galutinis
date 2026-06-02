package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserEntity;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final AppUserJpaRepository appUserJpaRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(AppUserJpaRepository appUserJpaRepository, PasswordEncoder passwordEncoder) {
        this.appUserJpaRepository = appUserJpaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(String username, String email, String rawPassword) {
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

        appUserJpaRepository.save(appUser);
    }
}
