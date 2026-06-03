package lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class ApiAuthenticationService {

    private final DatabaseUserDetailsService databaseUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final ApiTokenService apiTokenService;

    public ApiAuthenticationService(
            DatabaseUserDetailsService databaseUserDetailsService,
            PasswordEncoder passwordEncoder,
            ApiTokenService apiTokenService
    ) {
        this.databaseUserDetailsService = databaseUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.apiTokenService = apiTokenService;
    }

    public String issueToken(String username, String rawPassword) {
        UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(username);
        if (!passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        return apiTokenService.generateToken(userDetails.getUsername());
    }
}
