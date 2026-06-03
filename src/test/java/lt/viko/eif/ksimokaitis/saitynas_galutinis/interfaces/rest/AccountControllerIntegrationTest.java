package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;

import com.fasterxml.jackson.databind.JsonNode;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountControllerIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void accountEndpointsReturnOnlyAuthorizedUsersAccounts() throws Exception {
        AppUserEntity visibleUser = createUser("visible.user", "visible.user@example.com", "Visible#2026");
        AppUserEntity hiddenUser = createUser("hidden.user", "hidden.user@example.com", "Hidden#2026");

        createAccount("LT111111111111111111", "visible.user", "EUR", new BigDecimal("100.00"), visibleUser.getId());
        createAccount("LT222222222222222222", "visible.user", "USD", new BigDecimal("50.00"), visibleUser.getId());
        createAccount("LT333333333333333333", "hidden.user", "GBP", new BigDecimal("75.00"), hiddenUser.getId());

        String token = issueToken("visible.user", "Visible#2026");

        MvcResult listResult = mockMvc.perform(get("/api/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(listResult.getResponse().getContentAsString());
        JsonNode accounts = firstEmbeddedArray(root);

        assertThat(accounts).hasSize(2);
        assertThat(accounts.get(0).get("ownerName").asText()).isEqualTo("visible.user");
        assertThat(accounts.get(1).get("ownerName").asText()).isEqualTo("visible.user");

        Long accountId = accounts.get(0).get("id").asLong();

        mockMvc.perform(get("/api/accounts/{id}", accountId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId));
    }

    @Test
    void openAccountCreatesAdditionalAccountForAuthorizedUser() throws Exception {
        AppUserEntity user = createUser("new.account.user", "new.account.user@example.com", "Open#2026");
        createAccount("LT444444444444444444", "new.account.user", "EUR", BigDecimal.ZERO, user.getId());
        String token = issueToken("new.account.user", "Open#2026");

        mockMvc.perform(post("/api/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currency": "USD"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.ownerName").value("new.account.user"));
    }
}
