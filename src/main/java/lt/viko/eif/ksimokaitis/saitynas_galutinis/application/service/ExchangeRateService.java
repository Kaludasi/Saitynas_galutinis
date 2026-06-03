package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

/**
 * Encapsulates external exchange rate retrieval from the configured currency API.
 */
@Service
public class ExchangeRateService {

    private static final String LATEST_RATES_URL = "https://api.currencyapi.com/v3/latest";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${currency.api.key}")
    private String currencyApiKey;

    /**
     * Fetches the latest exchange rate from the external currency API.
     *
     * @param sourceCurrency source currency code
     * @param targetCurrency target currency code
     * @return exchange rate between the currencies
     */
    public BigDecimal fetchExchangeRate(String sourceCurrency, String targetCurrency) {
        String apiUrl = UriComponentsBuilder
                .fromUriString(LATEST_RATES_URL)
                .queryParam("apikey", currencyApiKey)
                .queryParam("base_currency", sourceCurrency)
                .queryParam("currencies", targetCurrency)
                .toUriString();

        CurrencyExchangeService.CurrencyApiResponse apiResponse =
                restTemplate.getForObject(apiUrl, CurrencyExchangeService.CurrencyApiResponse.class);
        if (apiResponse == null || apiResponse.data() == null || apiResponse.data().get(targetCurrency) == null) {
            throw new IllegalStateException("Exchange rate is unavailable");
        }

        return apiResponse.data().get(targetCurrency).value();
    }
}
