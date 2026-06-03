package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.CurrencyExchangeService;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.CurrencyExchange;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.CurrencyExchangeRequest;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.CurrencyExchangeResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.CurrencyExchangeHistoryResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.CurrencyOptionResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.assembler.CurrencyExchangeHistoryModelAssembler;
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

/**
 * Exposes currency reference data and exchange history/operations.
 */
@RestController
@RequestMapping("/api/currency")
@Tag(name = "Currency", description = "Supported currencies and exchange operations")
public class CurrencyController {

    private static final CacheControl PRIVATE_CURRENCY_LIST_CACHE = CacheControl.maxAge(Duration.ofHours(6)).cachePrivate().mustRevalidate();

    private final CurrencyExchangeService currencyExchangeService;
    private final CurrencyExchangeModelAssembler currencyExchangeModelAssembler;
    private final CurrencyExchangeHistoryModelAssembler currencyExchangeHistoryModelAssembler;

    /**
     * Creates the controller for currency endpoints.
     *
     * @param currencyExchangeService currency exchange business service
     * @param currencyExchangeModelAssembler HATEOAS assembler for exchange responses
     * @param currencyExchangeHistoryModelAssembler HATEOAS assembler for exchange history responses
     */
    public CurrencyController(
            CurrencyExchangeService currencyExchangeService,
            CurrencyExchangeModelAssembler currencyExchangeModelAssembler,
            CurrencyExchangeHistoryModelAssembler currencyExchangeHistoryModelAssembler
    ) {
        this.currencyExchangeService = currencyExchangeService;
        this.currencyExchangeModelAssembler = currencyExchangeModelAssembler;
        this.currencyExchangeHistoryModelAssembler = currencyExchangeHistoryModelAssembler;
    }

    /**
     * Lists supported currency codes for exchange operations.
     *
     * @return HATEOAS collection of supported currency resources
     */
    @GetMapping("/all")
    @Operation(summary = "List supported currencies")
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

    /**
     * Returns exchange history visible to the authenticated user.
     *
     * @param principal authenticated principal
     * @return HATEOAS collection of exchange history resources
     */
    @GetMapping("/exchanges")
    @Operation(summary = "List currency exchange history")
    public ResponseEntity<CollectionModel<EntityModel<CurrencyExchangeHistoryResponse>>> getExchangeHistory(Principal principal) {
        List<EntityModel<CurrencyExchangeHistoryResponse>> exchanges = currencyExchangeService.getExchangeHistoryForUsername(principal.getName())
                .stream()
                .map(currencyExchangeHistoryModelAssembler::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore().cachePrivate())
                .varyBy("Authorization")
                .body(CollectionModel.of(
                        exchanges,
                        linkTo(methodOn(CurrencyController.class).getExchangeHistory(principal)).withSelfRel()
                ));
    }

    /**
     * Exchanges money between two accounts owned by the authenticated user.
     *
     * @param currencyExchangeRequest exchange request payload
     * @param principal authenticated principal
     * @return HATEOAS exchange result resource
     */
    @PostMapping("/exchange")
    @Operation(summary = "Exchange currency between two user accounts")
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
