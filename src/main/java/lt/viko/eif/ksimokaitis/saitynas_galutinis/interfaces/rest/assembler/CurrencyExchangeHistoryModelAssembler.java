package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.assembler;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.CurrencyExchange;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.model.CurrencyExchangeHistoryResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.AccountController;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.CurrencyController;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CurrencyExchangeHistoryModelAssembler {

    public EntityModel<CurrencyExchangeHistoryResponse> toModel(CurrencyExchange currencyExchange) {
        CurrencyExchangeHistoryResponse response = new CurrencyExchangeHistoryResponse(
                currencyExchange.getId(),
                currencyExchange.getSourceAccount().getId(),
                currencyExchange.getSourceAccount().getIban(),
                currencyExchange.getTargetAccount().getId(),
                currencyExchange.getTargetAccount().getIban(),
                currencyExchange.getSourceAmount(),
                currencyExchange.getSourceCurrency(),
                currencyExchange.getTargetAmount(),
                currencyExchange.getTargetCurrency(),
                currencyExchange.getExchangeRate(),
                currencyExchange.getCreatedAt()
        );

        return EntityModel.of(
                response,
                linkTo(methodOn(CurrencyController.class).getExchangeHistory(null)).withRel("exchange-history"),
                linkTo(methodOn(AccountController.class).getAccountById(response.sourceAccountId(), null)).withRel("source-account"),
                linkTo(methodOn(AccountController.class).getAccountById(response.targetAccountId(), null)).withRel("target-account")
        );
    }
}
