package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.controller;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.CurrencyExchangeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CurrencyController {

    private final CurrencyExchangeService currencyExchangeService;

    public CurrencyController(CurrencyExchangeService currencyExchangeService) {
        this.currencyExchangeService = currencyExchangeService;
    }

    @GetMapping("/currency")
    public String showCurrencyPage(Model model) {
        model.addAttribute("currencies", currencyExchangeService.getCurrencies());
        return "_currencyPage";
    }

}
