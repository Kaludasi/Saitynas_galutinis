package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.AccountService;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Account;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.AccountOpenRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<Account> getAllAccounts(Principal principal) {
        return accountService.getAccountsForUsername(principal.getName());
    }

    @PostMapping
    public ResponseEntity<Account> openAccount(@RequestBody AccountOpenRequest request, Principal principal) {
        Account account = accountService.openAccountForUsername(principal.getName(), request.getCurrency());
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }

    @GetMapping("/currency/{accountNumber}")
    public ResponseEntity<String> getAccountCurrency(@PathVariable String accountNumber) {
        String currency = accountService.getCurrencyByAccountNumber(accountNumber);
        return ResponseEntity.ok(currency);
    }
}
