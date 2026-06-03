package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.validator;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserJpaRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.RegistrationForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class RegistrationFormValidator implements Validator {

    private final AppUserJpaRepository appUserJpaRepository;

    public RegistrationFormValidator(AppUserJpaRepository appUserJpaRepository) {
        this.appUserJpaRepository = appUserJpaRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return RegistrationForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RegistrationForm form = (RegistrationForm) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "registration.username.blank", "All fields are required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "registration.email.blank", "All fields are required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "registration.password.blank", "All fields are required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "confirmPassword", "registration.confirmPassword.blank", "All fields are required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "accountCurrency", "registration.accountCurrency.blank", "All fields are required.");

        if (errors.hasErrors()) {
            return;
        }

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            errors.reject("registration.password.mismatch", "Passwords do not match.");
        }

        if (appUserJpaRepository.existsByUsername(form.getUsername().trim())) {
            errors.reject("registration.username.taken", "Username is already taken.");
        }

        if (appUserJpaRepository.existsByEmail(form.getEmail().trim())) {
            errors.reject("registration.email.taken", "Email is already registered.");
        }
    }
}
