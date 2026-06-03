package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.AccountService;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Account;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.AccountCurrencyResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.AccountOpenRequest;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.AccountResponse;
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

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private static final CacheControl PRIVATE_ACCOUNT_CACHE = CacheControl.noStore().cachePrivate();
    private static final CacheControl PRIVATE_ACCOUNT_CURRENCY_CACHE = CacheControl.maxAge(Duration.ofHours(12)).cachePrivate().mustRevalidate();

    private final AccountService accountService;
    private final AccountModelAssembler accountModelAssembler;

    public AccountController(AccountService accountService, AccountModelAssembler accountModelAssembler) {
        this.accountService = accountService;
        this.accountModelAssembler = accountModelAssembler;
    }

    @GetMapping
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

    @GetMapping("/{accountId}")
    public ResponseEntity<EntityModel<AccountResponse>> getAccountById(@PathVariable Long accountId, Principal principal) {
        Account account = accountService.getAccountForUsernameById(principal.getName(), accountId);
        return ResponseEntity.ok()
                .cacheControl(PRIVATE_ACCOUNT_CACHE)
                .varyBy("Authorization")
                .body(accountModelAssembler.toModel(account));
    }

    @PostMapping
    public ResponseEntity<EntityModel<AccountResponse>> openAccount(@RequestBody AccountOpenRequest request, Principal principal) {
        Account account = accountService.openAccountForUsername(principal.getName(), request.getCurrency());
        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .varyBy("Authorization")
                .body(accountModelAssembler.toModel(account));
    }

    @GetMapping("/currency/{accountNumber}")
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
