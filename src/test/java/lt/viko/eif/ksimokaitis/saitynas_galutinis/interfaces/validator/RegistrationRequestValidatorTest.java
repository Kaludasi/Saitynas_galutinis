package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.validator;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserJpaRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.RegistrationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationRequestValidatorTest {

    @Mock
    private AppUserJpaRepository appUserJpaRepository;

    @InjectMocks
    private RegistrationRequestValidator registrationRequestValidator;

    @Test
    void validateRejectsBlankFields() {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername(" ");
        request.setEmail("jonas@example.com");
        request.setPassword("Secret#2026");
        request.setConfirmPassword("Secret#2026");
        request.setAccountCurrency("EUR");

        assertThatThrownBy(() -> registrationRequestValidator.validate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("All fields are required.");
    }

    @Test
    void validateRejectsPasswordMismatch() {
        RegistrationRequest request = validRequest();
        request.setConfirmPassword("Other#2026");

        assertThatThrownBy(() -> registrationRequestValidator.validate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Passwords do not match.");
    }

    @Test
    void validateAcceptsUniqueAndCompleteRequest() {
        when(appUserJpaRepository.existsByUsername("jonas")).thenReturn(false);
        when(appUserJpaRepository.existsByEmail("jonas@example.com")).thenReturn(false);

        assertThatCode(() -> registrationRequestValidator.validate(validRequest()))
                .doesNotThrowAnyException();
    }

    private static RegistrationRequest validRequest() {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("jonas");
        request.setEmail("jonas@example.com");
        request.setPassword("Secret#2026");
        request.setConfirmPassword("Secret#2026");
        request.setAccountCurrency("EUR");
        return request;
    }
}
