package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.assembler;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Account;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.AccountResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.AccountController;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.CurrencyController;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.PaymentController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AccountModelAssembler implements RepresentationModelAssembler<Account, EntityModel<AccountResponse>> {

    @Override
    public EntityModel<AccountResponse> toModel(Account account) {
        AccountResponse response = new AccountResponse(
                account.getId(),
                account.getIban(),
                account.getOwnerName(),
                account.getBalance(),
                account.getCurrency(),
                account.getCreatedAt()
        );

        return EntityModel.of(response,
                linkTo(methodOn(AccountController.class).getAccountById(response.id(), null)).withSelfRel(),
                linkTo(methodOn(AccountController.class).getAllAccounts(null)).withRel("accounts"),
                linkTo(methodOn(PaymentController.class).getAllPayments(null)).withRel("payments"),
                linkTo(methodOn(CurrencyController.class).allCurrencies()).withRel("currencies"),
                linkTo(methodOn(AccountController.class).getAccountCurrency(response.iban())).withRel("currency"));
    }
}
