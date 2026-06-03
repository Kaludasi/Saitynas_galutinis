package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.controller;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/accounts")
public class AccountModelController {

    private final AccountService accountService;

    public AccountModelController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public String showAccounts(Model model, Principal principal) {
        model.addAttribute("accounts", accountService.getAccountsForUsername(principal.getName()));
        return "_accounts";
    }
}
