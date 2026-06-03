package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.validator;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserJpaRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.RegistrationRequest;
import org.springframework.stereotype.Component;

/**
 * Validates incoming registration request payloads before user creation is attempted.
 */
@Component
public class RegistrationRequestValidator {

    private final AppUserJpaRepository appUserJpaRepository;

    /**
     * Creates the validator with access to user uniqueness checks.
     *
     * @param appUserJpaRepository user persistence gateway
     */
    public RegistrationRequestValidator(AppUserJpaRepository appUserJpaRepository) {
        this.appUserJpaRepository = appUserJpaRepository;
    }

    /**
     * Validates required fields, password confirmation, and uniqueness constraints.
     *
     * @param request incoming registration payload
     */
    public void validate(RegistrationRequest request) {
        if (isBlank(request.getUsername())
                || isBlank(request.getEmail())
                || isBlank(request.getPassword())
                || isBlank(request.getConfirmPassword())
                || isBlank(request.getAccountCurrency())) {
            throw new IllegalArgumentException("All fields are required.");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        if (appUserJpaRepository.existsByUsername(request.getUsername().trim())) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        if (appUserJpaRepository.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("Email is already registered.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
