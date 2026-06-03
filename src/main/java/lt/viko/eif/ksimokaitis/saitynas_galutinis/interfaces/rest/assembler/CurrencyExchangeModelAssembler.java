package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.assembler;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.CurrencyExchangeResponse;
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
                linkTo(methodOn(AccountController.class).getAccountById(response.getSourceAccountId(), null)).withRel("source-account"),
                linkTo(methodOn(AccountController.class).getAccountById(response.getTargetAccountId(), null)).withRel("target-account"),
                linkTo(methodOn(PaymentController.class).getAllPayments(null)).withRel("payments"));
    }
}
