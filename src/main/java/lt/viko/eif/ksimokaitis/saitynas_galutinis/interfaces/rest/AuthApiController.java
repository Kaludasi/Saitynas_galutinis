package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.RegistrationService;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.RegistrationRequest;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.RegistrationResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.validator.RegistrationRequestValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final RegistrationService registrationService;
    private final RegistrationRequestValidator registrationRequestValidator;

    public AuthApiController(
            RegistrationService registrationService,
            RegistrationRequestValidator registrationRequestValidator
    ) {
        this.registrationService = registrationService;
        this.registrationRequestValidator = registrationRequestValidator;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@RequestBody RegistrationRequest request) {
        registrationRequestValidator.validate(request);

        registrationService.register(
                request.getUsername().trim(),
                request.getEmail().trim(),
                request.getPassword(),
                request.getAccountCurrency().trim()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new RegistrationResponse("Registration completed successfully."));
    }
}
