package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Account;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Payment;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.PaymentStatus;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.AccountRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.PaymentRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.PaymentTransferRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;

/**
 * Executes payment transfer workflow and balance updates.
 */
@Service
public class PaymentTransferService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final ExchangeRateService exchangeRateService;
    private final AccountService accountService;

    /**
     * Creates the transfer service with collaborators needed for payment execution.
     *
     * @param paymentRepository payment persistence gateway
     * @param accountRepository account persistence gateway
     * @param exchangeRateService external exchange rate service
     * @param accountService account ownership helper
     */
    public PaymentTransferService(
            PaymentRepository paymentRepository,
            AccountRepository accountRepository,
            ExchangeRateService exchangeRateService,
            AccountService accountService
    ) {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.exchangeRateService = exchangeRateService;
        this.accountService = accountService;
    }

    /**
     * Transfers money from one account to another and marks the payment as completed.
     *
     * @param request transfer request payload
     * @param principal authenticated principal
     * @return completed payment entity
     */
    public Payment transfer(PaymentTransferRequest request, Principal principal) {
        Payment payment = createPaymentFromRequest(request);
        Account senderAccount = loadAuthorizedSenderAccount(payment, principal);

        paymentRepository.save(payment);
        validateSufficientFunds(senderAccount, request.getAmount());

        senderAccount.setBalance(senderAccount.getBalance().subtract(request.getAmount()));
        accountRepository.save(senderAccount);
        accountRepository.flush();

        Account receiverAccount = findAccountByIban(payment.getReceiverAccount(), "Receiver account not found");
        creditReceiverAccount(request, senderAccount, receiverAccount);

        payment.setStatus(PaymentStatus.COMPLETED);
        return paymentRepository.save(payment);
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

    private Account loadAuthorizedSenderAccount(Payment payment, Principal principal) {
        Account senderAccount = findAccountByIban(payment.getSenderAccount(), "Sender account not found");
        Long currentUserId = accountService.getUserIdByUsername(principal.getName());
        if (!senderAccount.getAppUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("Sender account does not belong to the authorized user");
        }
        return senderAccount;
    }

    private Account findAccountByIban(String iban, String notFoundMessage) {
        return accountRepository.findByIban(iban)
                .orElseThrow(() -> new IllegalArgumentException(notFoundMessage));
    }

    private void validateSufficientFunds(Account senderAccount, BigDecimal amount) {
        if (senderAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Sender account balance less than or equal to sent amount");
        }
    }

    private void creditReceiverAccount(PaymentTransferRequest request, Account senderAccount, Account receiverAccount) {
        if (receiverAccount.getCurrency().equals(senderAccount.getCurrency())) {
            receiverAccount.setBalance(receiverAccount.getBalance().add(request.getAmount()));
            accountRepository.save(receiverAccount);
            return;
        }

        BigDecimal exchangeRate = exchangeRateService.fetchExchangeRate(
                senderAccount.getCurrency(),
                receiverAccount.getCurrency()
        );
        BigDecimal convertedAmount = request.getAmount().multiply(exchangeRate).setScale(2, java.math.RoundingMode.HALF_UP);
        receiverAccount.setBalance(receiverAccount.getBalance().add(convertedAmount));
        accountRepository.save(receiverAccount);
    }
}
