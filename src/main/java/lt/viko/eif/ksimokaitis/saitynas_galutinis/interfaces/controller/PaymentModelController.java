package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.controller;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.AccountService;
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

    public PaymentModelController(PaymentService paymentService, AccountService accountService) {
        this.paymentService = paymentService;
        this.accountService = accountService;
    }

    @GetMapping
    public String showPayments(Model model) {
        model.addAttribute("payments", paymentService.getAllPayments());
        return "_payments";
    }

    @GetMapping("/transfer")
    public String showTransferPage(Model model, Principal principal) {
        String username = principal.getName();

        model.addAttribute("accounts", accountService.getAllAccounts());
        model.addAttribute(
                "accounts",
                accountService.getAccountsByUsername(username)
        );
        return "_paymentTransferPage";
    }

}
