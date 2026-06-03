package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.assembler;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Payment;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.AccountController;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.PaymentController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PaymentModelAssembler implements RepresentationModelAssembler<Payment, EntityModel<Payment>> {

    @Override
    public EntityModel<Payment> toModel(Payment payment) {
        return EntityModel.of(payment,
                linkTo(methodOn(PaymentController.class).getPaymentById(payment.getId(), null)).withSelfRel(),
                linkTo(methodOn(PaymentController.class).getAllPayments(null)).withRel("payments"),
                linkTo(methodOn(AccountController.class).getAccountCurrency(payment.getSenderAccount())).withRel("sender-currency"),
                linkTo(methodOn(AccountController.class).getAccountCurrency(payment.getReceiverAccount())).withRel("receiver-currency"));
    }
}
