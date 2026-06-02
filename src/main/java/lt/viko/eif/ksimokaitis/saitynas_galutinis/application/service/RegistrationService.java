package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final JdbcClient jdbcClient;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(JdbcClient jdbcClient, PasswordEncoder passwordEncoder) {
        this.jdbcClient = jdbcClient;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(String username, String email, String rawPassword) {
        boolean usernameExists = jdbcClient.sql("""
                        SELECT COUNT(*)
                        FROM app_user
                        WHERE username = :username
                        """)
                .param("username", username)
                .query(Long.class)
                .single() > 0;

        if (usernameExists) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        boolean emailExists = jdbcClient.sql("""
                        SELECT COUNT(*)
                        FROM app_user
                        WHERE email = :email
                        """)
                .param("email", email)
                .query(Long.class)
                .single() > 0;

        if (emailExists) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        jdbcClient.sql("""
                        INSERT INTO app_user (username, email, password_hash, role, enabled)
                        VALUES (:username, :email, :passwordHash, :role, :enabled)
                        """)
                .param("username", username)
                .param("email", email)
                .param("passwordHash", passwordEncoder.encode(rawPassword))
                .param("role", "USER")
                .param("enabled", true)
                .update();
    }
}
