package lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service.PaymentService;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Payment;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.PaymentTransferRequest;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.PaymentTransferResponse;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.assembler.PaymentModelAssembler;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.assembler.PaymentTransferModelAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final CacheControl PRIVATE_PAYMENT_CACHE = CacheControl.noStore().cachePrivate();

    private final PaymentService paymentService;
    private final PaymentModelAssembler paymentModelAssembler;
    private final PaymentTransferModelAssembler paymentTransferModelAssembler;

    public PaymentController(
            PaymentService paymentService,
            PaymentModelAssembler paymentModelAssembler,
            PaymentTransferModelAssembler paymentTransferModelAssembler
    ) {
        this.paymentService = paymentService;
        this.paymentModelAssembler = paymentModelAssembler;
        this.paymentTransferModelAssembler = paymentTransferModelAssembler;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Payment>>> getAllPayments() {
        List<EntityModel<Payment>> payments = paymentService.getAllPayments()
                .stream()
                .map(paymentModelAssembler::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .cacheControl(PRIVATE_PAYMENT_CACHE)
                .varyBy("Authorization")
                .body(CollectionModel.of(
                        payments,
                        linkTo(methodOn(PaymentController.class).getAllPayments()).withSelfRel()
                ));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<EntityModel<Payment>> getPaymentById(@PathVariable Long paymentId) {
        return ResponseEntity.ok()
                .cacheControl(PRIVATE_PAYMENT_CACHE)
                .varyBy("Authorization")
                .body(paymentModelAssembler.toModel(paymentService.getPaymentById(paymentId)));
    }

    @PostMapping("/transfer")
    public ResponseEntity<EntityModel<PaymentTransferResponse>> transferPayment(@RequestBody PaymentTransferRequest request, Principal principal) {
        Payment payment = paymentService.transferPayment(request, principal);
        PaymentTransferResponse response = new PaymentTransferResponse("Payment was sent successfully", payment.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .varyBy("Authorization")
                .body(paymentTransferModelAssembler.toModel(response));
    }
}
