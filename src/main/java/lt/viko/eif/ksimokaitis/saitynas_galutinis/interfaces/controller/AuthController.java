package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.controller;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.RegistrationService;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.RegistrationForm;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.validator.RegistrationFormValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final RegistrationService registrationService;
    private final RegistrationFormValidator registrationFormValidator;

    public AuthController(
            RegistrationService registrationService,
            RegistrationFormValidator registrationFormValidator
    ) {
        this.registrationService = registrationService;
        this.registrationFormValidator = registrationFormValidator;
    }

    @InitBinder("registrationForm")
    void initRegistrationFormBinder(WebDataBinder binder) {
        binder.addValidators(registrationFormValidator);
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        model.addAttribute("supportedCurrencies", registrationService.getSupportedCurrencies());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Validated @ModelAttribute RegistrationForm registrationForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("supportedCurrencies", registrationService.getSupportedCurrencies());
            return "register";
        }

        registrationService.register(
                registrationForm.getUsername().trim(),
                registrationForm.getEmail().trim(),
                registrationForm.getPassword(),
                registrationForm.getAccountCurrency().trim()
        );

        redirectAttributes.addAttribute("registered", "");
        return "redirect:/login";
    }
}
