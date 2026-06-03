package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.RegistrationService;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.security.ApiAuthenticationService;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.ApiTokenRequest;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.ApiTokenResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.RegistrationRequest;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.RegistrationResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.validator.RegistrationRequestValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Registration and API token issuance")
public class AuthApiController {

    private final RegistrationService registrationService;
    private final RegistrationRequestValidator registrationRequestValidator;
    private final ApiAuthenticationService apiAuthenticationService;

    public AuthApiController(
            RegistrationService registrationService,
            RegistrationRequestValidator registrationRequestValidator,
            ApiAuthenticationService apiAuthenticationService
    ) {
        this.registrationService = registrationService;
        this.registrationRequestValidator = registrationRequestValidator;
        this.apiAuthenticationService = apiAuthenticationService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user and opens the first account in the selected currency.")
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

    @PostMapping("/token")
    @Operation(summary = "Issue API token", description = "Returns a bearer token for calling stateless REST endpoints.")
    public ResponseEntity<ApiTokenResponse> issueToken(@RequestBody ApiTokenRequest request) {
        String token = apiAuthenticationService.issueToken(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new ApiTokenResponse(token));
    }
}
