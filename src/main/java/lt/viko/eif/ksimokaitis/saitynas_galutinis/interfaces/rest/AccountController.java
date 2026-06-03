package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.AccountService;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Account;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.AccountCurrencyResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.AccountOpenRequest;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.AccountResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.assembler.AccountModelAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Exposes account-related REST endpoints for authenticated users.
 */
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Account queries and account opening")
public class AccountController {

    private static final CacheControl PRIVATE_ACCOUNT_CACHE = CacheControl.noStore().cachePrivate();
    private static final CacheControl PRIVATE_ACCOUNT_CURRENCY_CACHE = CacheControl.maxAge(Duration.ofHours(12)).cachePrivate().mustRevalidate();

    private final AccountService accountService;
    private final AccountModelAssembler accountModelAssembler;

    /**
     * Creates the controller for account endpoints.
     *
     * @param accountService account business service
     * @param accountModelAssembler HATEOAS assembler for account responses
     */
    public AccountController(AccountService accountService, AccountModelAssembler accountModelAssembler) {
        this.accountService = accountService;
        this.accountModelAssembler = accountModelAssembler;
    }

    /**
     * Lists accounts visible to the authenticated user.
     *
     * @param principal authenticated principal
     * @return HATEOAS collection of account resources
     */
    @GetMapping
    @Operation(summary = "List user accounts")
    public ResponseEntity<CollectionModel<EntityModel<AccountResponse>>> getAllAccounts(Principal principal) {
        List<EntityModel<AccountResponse>> accounts = accountService.getAccountsForUsername(principal.getName())
                .stream()
                .map(accountModelAssembler::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .cacheControl(PRIVATE_ACCOUNT_CACHE)
                .varyBy("Authorization")
                .body(CollectionModel.of(
                        accounts,
                        linkTo(methodOn(AccountController.class).getAllAccounts(principal)).withSelfRel()
                ));
    }

    /**
     * Returns a single account visible to the authenticated user.
     *
     * @param accountId account identifier
     * @param principal authenticated principal
     * @return HATEOAS account resource
     */
    @GetMapping("/{accountId}")
    @Operation(summary = "Get account by id")
    public ResponseEntity<EntityModel<AccountResponse>> getAccountById(@PathVariable Long accountId, Principal principal) {
        Account account = accountService.getAccountForUsernameById(principal.getName(), accountId);
        return ResponseEntity.ok()
                .cacheControl(PRIVATE_ACCOUNT_CACHE)
                .varyBy("Authorization")
                .body(accountModelAssembler.toModel(account));
    }

    /**
     * Opens a new account for the authenticated user.
     *
     * @param request account opening payload
     * @param principal authenticated principal
     * @return created HATEOAS account resource
     */
    @PostMapping
    @Operation(summary = "Open a new account")
    public ResponseEntity<EntityModel<AccountResponse>> openAccount(@RequestBody AccountOpenRequest request, Principal principal) {
        Account account = accountService.openAccountForUsername(principal.getName(), request.getCurrency());
        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .varyBy("Authorization")
                .body(accountModelAssembler.toModel(account));
    }

    /**
     * Resolves account currency by IBAN.
     *
     * @param accountNumber account IBAN
     * @return HATEOAS currency lookup resource
     */
    @GetMapping("/currency/{accountNumber}")
    @Operation(summary = "Get account currency by IBAN")
    public ResponseEntity<EntityModel<AccountCurrencyResponse>> getAccountCurrency(@PathVariable String accountNumber) {
        String currency = accountService.getCurrencyByAccountNumber(accountNumber);
        AccountCurrencyResponse response = new AccountCurrencyResponse(accountNumber, currency);
        return ResponseEntity.ok()
                .cacheControl(PRIVATE_ACCOUNT_CURRENCY_CACHE)
                .varyBy("Authorization")
                .body(EntityModel.of(
                        response,
                        linkTo(methodOn(AccountController.class).getAccountCurrency(accountNumber)).withSelfRel(),
                        linkTo(methodOn(AccountController.class).getAllAccounts(null)).withRel("accounts")
                ));
    }
}
