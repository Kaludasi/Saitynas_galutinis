package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.assembler;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.PaymentTransferResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.PaymentController;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PaymentTransferModelAssembler {

    public EntityModel<PaymentTransferResponse> toModel(PaymentTransferResponse response) {
        EntityModel<PaymentTransferResponse> model = EntityModel.of(response,
                linkTo(methodOn(PaymentController.class).getAllPayments(null)).withRel("payments"));

        if (response.paymentId() != null) {
            model.add(linkTo(methodOn(PaymentController.class).getPaymentById(response.paymentId(), null)).withRel("payment"));
        }

        return model;
    }
}
