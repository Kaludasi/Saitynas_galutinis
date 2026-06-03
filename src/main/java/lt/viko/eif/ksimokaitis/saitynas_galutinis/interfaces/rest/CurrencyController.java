package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;


import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.CurrencyExchangeService;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.CurrencyExchangeRequest;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.CurrencyExchangeResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.CurrencyOptionResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.assembler.CurrencyExchangeModelAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    private static final CacheControl PRIVATE_CURRENCY_LIST_CACHE = CacheControl.maxAge(Duration.ofHours(6)).cachePrivate().mustRevalidate();

    private final CurrencyExchangeService currencyExchangeService;
    private final CurrencyExchangeModelAssembler currencyExchangeModelAssembler;

    public CurrencyController(
            CurrencyExchangeService currencyExchangeService,
            CurrencyExchangeModelAssembler currencyExchangeModelAssembler
    ) {
        this.currencyExchangeService = currencyExchangeService;
        this.currencyExchangeModelAssembler = currencyExchangeModelAssembler;
    }


    @GetMapping("/all")
    public ResponseEntity<CollectionModel<EntityModel<CurrencyOptionResponse>>> allCurrencies() {
        List<EntityModel<CurrencyOptionResponse>> currencies = currencyExchangeService.getCurrencies()
                .stream()
                .map(currency -> EntityModel.of(new CurrencyOptionResponse(currency),
                        linkTo(methodOn(CurrencyController.class).allCurrencies()).withSelfRel(),
                        linkTo(methodOn(CurrencyController.class).exchangeCurrency(null, null)).withRel("exchange")))
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .cacheControl(PRIVATE_CURRENCY_LIST_CACHE)
                .varyBy("Authorization")
                .body(CollectionModel.of(
                        currencies,
                        linkTo(methodOn(CurrencyController.class).allCurrencies()).withSelfRel()
                ));
    }

    @PostMapping("/exchange")
    public ResponseEntity<EntityModel<CurrencyExchangeResponse>> exchangeCurrency(
            @RequestBody CurrencyExchangeRequest currencyExchangeRequest,
            Principal principal) {
        CurrencyExchangeResponse response = currencyExchangeService.exchange(currencyExchangeRequest, principal.getName());
        return ResponseEntity.status(HttpStatus.OK)
                .cacheControl(CacheControl.noStore())
                .varyBy("Authorization")
                .body(currencyExchangeModelAssembler.toModel(response));
    }

}
