package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.assembler;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Payment;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.PaymentResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.AccountController;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.PaymentController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PaymentModelAssembler implements RepresentationModelAssembler<Payment, EntityModel<PaymentResponse>> {

    @Override
    public EntityModel<PaymentResponse> toModel(Payment payment) {
        PaymentResponse response = new PaymentResponse(
                payment.getId(),
                payment.getSenderAccount(),
                payment.getReceiverAccount(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getDescription(),
                payment.getCreatedAt()
        );

        return EntityModel.of(response,
                linkTo(methodOn(PaymentController.class).getPaymentById(response.id(), null)).withSelfRel(),
                linkTo(methodOn(PaymentController.class).getAllPayments(null)).withRel("payments"),
                linkTo(methodOn(AccountController.class).getAccountCurrency(response.senderAccount())).withRel("sender-currency"),
                linkTo(methodOn(AccountController.class).getAccountCurrency(response.receiverAccount())).withRel("receiver-currency"));
    }
}
