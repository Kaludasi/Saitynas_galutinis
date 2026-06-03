package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.AccountService;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Account;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.AccountCurrencyResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.AccountOpenRequest;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.assembler.AccountModelAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final AccountModelAssembler accountModelAssembler;

    public AccountController(AccountService accountService, AccountModelAssembler accountModelAssembler) {
        this.accountService = accountService;
        this.accountModelAssembler = accountModelAssembler;
    }

    @GetMapping
    public CollectionModel<EntityModel<Account>> getAllAccounts(Principal principal) {
        List<EntityModel<Account>> accounts = accountService.getAccountsForUsername(principal.getName())
                .stream()
                .map(accountModelAssembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(
                accounts,
                linkTo(methodOn(AccountController.class).getAllAccounts(principal)).withSelfRel()
        );
    }

    @GetMapping("/{accountId}")
    public EntityModel<Account> getAccountById(@PathVariable Long accountId, Principal principal) {
        Account account = accountService.getAccountForUsernameById(principal.getName(), accountId);
        return accountModelAssembler.toModel(account);
    }

    @PostMapping
    public ResponseEntity<EntityModel<Account>> openAccount(@RequestBody AccountOpenRequest request, Principal principal) {
        Account account = accountService.openAccountForUsername(principal.getName(), request.getCurrency());
        return new ResponseEntity<>(accountModelAssembler.toModel(account), HttpStatus.CREATED);
    }

    @GetMapping("/currency/{accountNumber}")
    public ResponseEntity<EntityModel<AccountCurrencyResponse>> getAccountCurrency(@PathVariable String accountNumber) {
        String currency = accountService.getCurrencyByAccountNumber(accountNumber);
        AccountCurrencyResponse response = new AccountCurrencyResponse(accountNumber, currency);
        return ResponseEntity.ok(
                EntityModel.of(
                        response,
                        linkTo(methodOn(AccountController.class).getAccountCurrency(accountNumber)).withSelfRel(),
                        linkTo(methodOn(AccountController.class).getAllAccounts(null)).withRel("accounts")
                )
        );
    }
}
