package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.*;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.PaymentRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.PaymentTransferRequest;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

/**
 * Handles payment history access and money transfer execution between accounts.
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountService accountService;
    private final PaymentTransferService paymentTransferService;

    /**
     * Creates the service with repositories and dependent services required for transfers.
     *
     * @param paymentRepository payment persistence gateway
     * @param accountService account ownership helper
     * @param paymentTransferService dedicated transfer workflow service
     */
    public PaymentService(
            PaymentRepository paymentRepository,
            AccountService accountService,
            PaymentTransferService paymentTransferService
    ) {
        this.paymentRepository = paymentRepository;
        this.accountService = accountService;
        this.paymentTransferService = paymentTransferService;
    }

    /**
     * Returns payments visible to the authenticated user.
     *
     * @param username authenticated username
     * @return ordered list of visible payments
     */
    public List<Payment> getAllPaymentsForUsername(String username) {
        return paymentRepository.findVisiblePayments(getVisibleIbans(username));
    }

    /**
     * Returns a single visible payment for the authenticated user.
     *
     * @param username authenticated username
     * @param paymentId payment identifier
     * @return visible payment
     */
    public Payment getPaymentByIdForUsername(String username, Long paymentId) {
        return paymentRepository.findVisiblePaymentById(paymentId, getVisibleIbans(username))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
    }

    /**
     * Transfers money from one account to another and marks the payment as completed.
     *
     * @param request transfer request payload
     * @param principal authenticated principal
     * @return completed payment entity
     */
    public Payment transferPayment(PaymentTransferRequest request, Principal principal) {
        return paymentTransferService.transfer(request, principal);
    }

    private List<String> getVisibleIbans(String username) {
        return accountService.getAccountsForUsername(username)
                .stream()
                .map(Account::getIban)
                .toList();
    }
}
