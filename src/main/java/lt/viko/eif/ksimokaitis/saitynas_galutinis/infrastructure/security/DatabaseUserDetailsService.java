package lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.security;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final JdbcClient jdbcClient;

    public DatabaseUserDetailsService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUserRow user = jdbcClient.sql("""
                        SELECT username, password_hash, role, enabled
                        FROM app_user
                        WHERE username = :username
                        """)
                .param("username", username)
                .query((rs, rowNum) -> new AppUserRow(
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getBoolean("enabled")
                ))
                .optional()
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.withUsername(user.username())
                .password(user.passwordHash())
                .roles(user.role())
                .disabled(!user.enabled())
                .build();
    }

    private record AppUserRow(String username, String passwordHash, String role, boolean enabled) {
    }
}
