package lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class ApiTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] signingSecret;
    private final long tokenValiditySeconds;

    public ApiTokenService(
            @Value("${app.security.api-token-secret}") String signingSecret,
            @Value("${app.security.api-token-validity-seconds:3600}") long tokenValiditySeconds
    ) {
        this.signingSecret = signingSecret.getBytes(StandardCharsets.UTF_8);
        this.tokenValiditySeconds = tokenValiditySeconds;
    }

    public String generateToken(String username) {
        long expiresAt = Instant.now().plusSeconds(tokenValiditySeconds).getEpochSecond();
        String encodedUsername = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(username.getBytes(StandardCharsets.UTF_8));
        String payload = encodedUsername + ":" + expiresAt;
        String signature = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(sign(payload));

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8))
                + "."
                + signature;
    }

    public String validateAndExtractUsername(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid API token format.");
        }

        String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String expectedSignature = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(sign(payload));
        if (!expectedSignature.equals(parts[1])) {
            throw new IllegalArgumentException("Invalid API token signature.");
        }

        String[] payloadParts = payload.split(":");
        if (payloadParts.length != 2) {
            throw new IllegalArgumentException("Invalid API token payload.");
        }

        long expiresAt = Long.parseLong(payloadParts[1]);
        if (Instant.now().getEpochSecond() > expiresAt) {
            throw new IllegalArgumentException("API token has expired.");
        }

        return new String(Base64.getUrlDecoder().decode(payloadParts[0]), StandardCharsets.UTF_8);
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(signingSecret, HMAC_ALGORITHM));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign API token.", exception);
        }
    }
}
