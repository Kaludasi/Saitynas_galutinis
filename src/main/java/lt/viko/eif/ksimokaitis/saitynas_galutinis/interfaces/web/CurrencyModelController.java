package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.web;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class CurrencyModelController {

    private final AccountService accountService;

    public CurrencyModelController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/currency")
    public String showCurrencyPage(Model model, Principal principal) {
        model.addAttribute("accounts", accountService.getAccountsForUsername(principal.getName()));
        return "_currencyPage";
    }

}

