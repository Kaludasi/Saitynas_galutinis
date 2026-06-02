package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.controller;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payment")
public class PaymentModelController {

    private final PaymentService paymentService;

    public PaymentModelController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public String showPayments(Model model) {
        model.addAttribute("payments", paymentService.getAllPayments());
        return "_payments";
    }

}
