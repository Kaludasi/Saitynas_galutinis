package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;


import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.CurrencyExchangeService;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.CurrencyExchangeRequest;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.CurrencyExchangeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    private final CurrencyExchangeService currencyExchangeService;

    public CurrencyController(CurrencyExchangeService currencyExchangeService) {
        this.currencyExchangeService = currencyExchangeService;
    }


    @GetMapping("/all")
    public List<String> allCurrencies() {
        return currencyExchangeService.getCurrencies();
    }

    @PostMapping("/exchange")
    public ResponseEntity<CurrencyExchangeResponse> exchangeCurrency(
            @RequestBody CurrencyExchangeRequest currencyExchangeRequest) {
        CurrencyExchangeResponse response = currencyExchangeService.exchange(currencyExchangeRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> handleExchangeError(RuntimeException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

}
