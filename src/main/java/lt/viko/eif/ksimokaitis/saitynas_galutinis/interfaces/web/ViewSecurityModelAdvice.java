package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.web;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.security.ApiTokenService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice(basePackageClasses = {
        MainPageModelController.class,
        PaymentModelController.class,
        CurrencyModelController.class,
        AccountModelController.class
})
public class ViewSecurityModelAdvice {

    private final ApiTokenService apiTokenService;

    public ViewSecurityModelAdvice(ApiTokenService apiTokenService) {
        this.apiTokenService = apiTokenService;
    }

    @ModelAttribute("apiToken")
    public String apiToken(Principal principal) {
        if (principal == null) {
            return null;
        }

        return apiTokenService.generateToken(principal.getName());
    }
}

