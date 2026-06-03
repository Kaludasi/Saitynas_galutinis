package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiAcceptanceTest extends ApiIntegrationTestSupport {

    @Test
    void registerAndIssueTokenFlowWorks() throws Exception {
        String registerRequest = """
                {
                  "username": "acceptance.user",
                  "email": "acceptance.user@example.com",
                  "password": "Acceptance#2026",
                  "confirmPassword": "Acceptance#2026",
                  "accountCurrency": "EUR"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration completed successfully."));

        mockMvc.perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "acceptance.user",
                                  "password": "Acceptance#2026"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
