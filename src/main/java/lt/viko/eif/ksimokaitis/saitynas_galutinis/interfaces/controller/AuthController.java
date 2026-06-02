package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.controller;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.RegistrationService;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.RegistrationForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegistrationForm registrationForm, Model model, RedirectAttributes redirectAttributes) {
        if (isBlank(registrationForm.getUsername())
                || isBlank(registrationForm.getEmail())
                || isBlank(registrationForm.getPassword())
                || isBlank(registrationForm.getConfirmPassword())) {
            model.addAttribute("registrationError", "All fields are required.");
            return "register";
        }

        if (!registrationForm.getPassword().equals(registrationForm.getConfirmPassword())) {
            model.addAttribute("registrationError", "Passwords do not match.");
            return "register";
        }

        try {
            registrationService.register(
                    registrationForm.getUsername().trim(),
                    registrationForm.getEmail().trim(),
                    registrationForm.getPassword()
            );
        } catch (IllegalArgumentException ex) {
            model.addAttribute("registrationError", ex.getMessage());
            return "register";
        }

        redirectAttributes.addAttribute("registered", "");
        return "redirect:/login";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
