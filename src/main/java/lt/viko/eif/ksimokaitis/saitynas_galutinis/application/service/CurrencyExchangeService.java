package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;


import lombok.Getter;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.CurrencyExchangeRequest;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.CurrencyExchangeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyExchangeService {

    private final String URL = "https://api.currencyapi.com/v3/latest";

    @Getter
    private final List<String> currencies = List.of("EUR", "USD", "GBP", "PLN");
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${currency.api.key}")
    private String currencyApiKey;

    public CurrencyExchangeResponse exchange(CurrencyExchangeRequest request) {
        String apiUrl = UriComponentsBuilder
                .fromUriString(URL)
                .queryParam("apikey", currencyApiKey)
                .queryParam("base_currency", request.fromCurrency())
                .queryParam("currencies", request.toCurrency())
                .toUriString();

        CurrencyApiResponse apiResponse = restTemplate.getForObject(apiUrl, CurrencyApiResponse.class);
        BigDecimal exchangeRate = apiResponse.data().get(request.toCurrency()).value();
        BigDecimal convertedAmount = request.amount().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);

        return new CurrencyExchangeResponse(
                request.amount(),
                request.fromCurrency(),
                request.toCurrency(),
                exchangeRate,
                convertedAmount
        );
    }

    public record CurrencyApiResponse(
            Map<String, CurrencyRate> data
    ) {
    }

    public record CurrencyRate(
            String code,
            BigDecimal value
    ) {
    }
}
