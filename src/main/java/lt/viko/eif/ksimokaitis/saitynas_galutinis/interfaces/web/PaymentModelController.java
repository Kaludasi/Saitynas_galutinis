package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.web;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.AccountService;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.CurrencyExchangeService;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/payment")
public class PaymentModelController {

    private final PaymentService paymentService;
    private final AccountService accountService;
    private final CurrencyExchangeService currencyExchangeService;

    public PaymentModelController(
            PaymentService paymentService,
            AccountService accountService,
            CurrencyExchangeService currencyExchangeService
    ) {
        this.paymentService = paymentService;
        this.accountService = accountService;
        this.currencyExchangeService = currencyExchangeService;
    }

    @GetMapping
    public String showPayments(Model model, Principal principal) {
        model.addAttribute("payments", paymentService.getAllPaymentsForUsername(principal.getName()));
        model.addAttribute("exchangeHistory", currencyExchangeService.getExchangeHistoryResponsesForUsername(principal.getName()));
        return "_payments";
    }

    @GetMapping("/transfer")
    public String showTransferPage(Model model, Principal principal) {
        model.addAttribute("accounts", accountService.getAccountsForUsername(principal.getName()));
        return "_paymentTransferPage";
    }

}

