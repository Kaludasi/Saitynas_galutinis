package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;

import com.fasterxml.jackson.databind.JsonNode;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CurrencyControllerIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void supportedCurrenciesEndpointReturnsConfiguredCurrencies() throws Exception {
        AppUserEntity user = createUser("currency.user", "currency.user@example.com", "Currency#2026");
        createAccount("LT121212121212121212", "currency.user", "EUR", new BigDecimal("50.00"), user.getId());
        String token = issueToken("currency.user", "Currency#2026");

        MvcResult result = mockMvc.perform(get("/api/currency/all")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode currencies = firstEmbeddedArray(root);

        assertThat(currencies).hasSize(4);
    }

    @Test
    void exchangeHistoryReturnsOnlyAuthorizedUsersExchanges() throws Exception {
        AppUserEntity visibleUser = createUser("exchange.user", "exchange.user@example.com", "Exchange#2026");
        AppUserEntity hiddenUser = createUser("hidden.exchange", "hidden.exchange@example.com", "Hidden#2026");

        var visibleSource = createAccount("LT131313131313131313", "exchange.user", "EUR", new BigDecimal("100.00"), visibleUser.getId());
        var visibleTarget = createAccount("LT141414141414141414", "exchange.user", "USD", new BigDecimal("10.00"), visibleUser.getId());
        var hiddenSource = createAccount("LT151515151515151515", "hidden.exchange", "EUR", new BigDecimal("100.00"), hiddenUser.getId());
        var hiddenTarget = createAccount("LT161616161616161616", "hidden.exchange", "USD", new BigDecimal("10.00"), hiddenUser.getId());

        createExchange(visibleSource, visibleTarget, new BigDecimal("20.00"), new BigDecimal("22.00"), new BigDecimal("1.10"));
        createExchange(hiddenSource, hiddenTarget, new BigDecimal("30.00"), new BigDecimal("33.00"), new BigDecimal("1.10"));

        String token = issueToken("exchange.user", "Exchange#2026");

        MvcResult result = mockMvc.perform(get("/api/currency/exchanges")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode exchanges = firstEmbeddedArray(root);

        assertThat(exchanges).hasSize(1);
        assertThat(exchanges.get(0).get("sourceAccountIban").asText()).isEqualTo("LT131313131313131313");
    }
}
