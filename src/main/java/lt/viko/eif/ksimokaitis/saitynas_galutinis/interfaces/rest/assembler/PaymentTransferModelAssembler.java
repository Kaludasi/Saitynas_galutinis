package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.assembler;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.PaymentTransferResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.PaymentController;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PaymentTransferModelAssembler {

    public EntityModel<PaymentTransferResponse> toModel(PaymentTransferResponse response) {
        EntityModel<PaymentTransferResponse> model = EntityModel.of(response,
                linkTo(methodOn(PaymentController.class).getAllPayments()).withRel("payments"));

        if (response.getPaymentId() != null) {
            model.add(linkTo(methodOn(PaymentController.class).getPaymentById(response.getPaymentId())).withRel("payment"));
        }

        return model;
    }
}
