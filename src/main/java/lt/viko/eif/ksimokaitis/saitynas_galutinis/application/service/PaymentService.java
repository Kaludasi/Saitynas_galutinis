package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.*;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.AccountRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.PaymentRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.PaymentTransferRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles payment history access and money transfer execution between accounts.
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final CurrencyExchangeService currencyExchangeService;
    private final AccountService accountService;

    /**
     * Creates the service with repositories and dependent services required for transfers.
     *
     * @param paymentRepository payment persistence gateway
     * @param accountRepository account persistence gateway
     * @param currencyExchangeService exchange helper used for cross-currency transfers
     * @param accountService account ownership helper
     */
    public PaymentService(
            PaymentRepository paymentRepository,
            AccountRepository accountRepository,
            CurrencyExchangeService currencyExchangeService,
            AccountService accountService
    ) {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.currencyExchangeService = currencyExchangeService;
        this.accountService = accountService;
    }

    private static Payment createPaymentFromRequest(PaymentTransferRequest request) {
        return new Payment(
                request.getSenderAccount(),
                request.getReceiverAccount(),
                request.getAmount(),
                request.getCurrency(),
                PaymentStatus.PENDING,
                request.getDescription(),
                LocalDateTime.now()
        );
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
        Payment payment = createPaymentFromRequest(request);

        Account senderAccount = accountRepository.findByIban(payment.getSenderAccount())
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found"));
        Long currentUserId = accountService.getUserIdByUsername(principal.getName());
        if (!senderAccount.getAppUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("Sender account does not belong to the authorized user");
        }

        paymentRepository.save(payment);
        if (senderAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Sender account balance less than or equal to sent amount");
        }
        senderAccount.setBalance(senderAccount.getBalance().subtract(request.getAmount()));
        accountRepository.save(senderAccount);
        accountRepository.flush();
        Account receiverAccount = accountRepository.findByIban(payment.getReceiverAccount())
                .orElseThrow(() -> new IllegalArgumentException("Receiver account not found"));
        if (receiverAccount.getCurrency().equals(senderAccount.getCurrency())) {
            receiverAccount.setBalance(receiverAccount.getBalance().add(request.getAmount()));
            accountRepository.save(receiverAccount);
        } else {
            BigDecimal exchangeRate = currencyExchangeService.fetchExchangeRate(
                    senderAccount.getCurrency(),
                    receiverAccount.getCurrency()
            );
            BigDecimal convertedAmount = currencyExchangeService.calculateConvertedAmount(payment.getAmount(), exchangeRate);
            receiverAccount.setBalance(receiverAccount.getBalance().add(convertedAmount));
            accountRepository.save(receiverAccount);
        }
        payment.setStatus(PaymentStatus.COMPLETED);
        return paymentRepository.save(payment);
    }

    private List<String> getVisibleIbans(String username) {
        return accountService.getAccountsForUsername(username)
                .stream()
                .map(Account::getIban)
                .toList();
    }
}
