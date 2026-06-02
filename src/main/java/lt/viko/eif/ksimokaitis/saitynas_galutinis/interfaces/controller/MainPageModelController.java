package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainPageModelController {

    @GetMapping("/")
    public String redirectToMainPage() {
        return "redirect:/main";
    }

    @GetMapping("/main")
    public String showMainPage() {
        return "_mainPage";
    }
}
