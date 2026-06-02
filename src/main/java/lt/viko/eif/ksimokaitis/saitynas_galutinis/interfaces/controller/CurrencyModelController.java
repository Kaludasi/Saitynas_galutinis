package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.controller;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CurrencyModelController {

    private final AccountService accountService;

    public CurrencyModelController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/currency")
    public String showCurrencyPage(Model model) {
        model.addAttribute("accounts", accountService.getAllAccounts());
        return "_currencyPage";
    }

}
