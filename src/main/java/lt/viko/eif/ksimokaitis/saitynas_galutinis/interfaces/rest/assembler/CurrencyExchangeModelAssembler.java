package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.assembler;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.CurrencyExchangeResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.AccountController;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.CurrencyController;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.PaymentController;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CurrencyExchangeModelAssembler {

    public EntityModel<CurrencyExchangeResponse> toModel(CurrencyExchangeResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(CurrencyController.class).allCurrencies()).withRel("currencies"),
                linkTo(methodOn(AccountController.class).getAccountById(response.sourceAccountId(), null)).withRel("source-account"),
                linkTo(methodOn(AccountController.class).getAccountById(response.targetAccountId(), null)).withRel("target-account"),
                linkTo(methodOn(PaymentController.class).getAllPayments(null)).withRel("payments"));
    }
}
