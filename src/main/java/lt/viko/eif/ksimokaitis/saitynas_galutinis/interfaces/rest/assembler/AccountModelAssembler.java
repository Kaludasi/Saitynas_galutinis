package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.assembler;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Account;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.AccountController;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.CurrencyController;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.PaymentController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AccountModelAssembler implements RepresentationModelAssembler<Account, EntityModel<Account>> {

    @Override
    public EntityModel<Account> toModel(Account account) {
        return EntityModel.of(account,
                linkTo(methodOn(AccountController.class).getAccountById(account.getId(), null)).withSelfRel(),
                linkTo(methodOn(AccountController.class).getAllAccounts(null)).withRel("accounts"),
                linkTo(methodOn(PaymentController.class).getAllPayments(null)).withRel("payments"),
                linkTo(methodOn(CurrencyController.class).allCurrencies()).withRel("currencies"),
                linkTo(methodOn(AccountController.class).getAccountCurrency(account.getIban())).withRel("currency"));
    }
}
