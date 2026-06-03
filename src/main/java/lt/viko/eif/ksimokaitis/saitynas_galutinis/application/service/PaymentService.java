package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.*;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.AccountRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final CurrencyExchangeService currencyExchangeService;

    public PaymentService(PaymentRepository paymentRepository, AccountRepository accountRepository, CurrencyExchangeService currencyExchangeService) {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.currencyExchangeService = currencyExchangeService;
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

    public List<Payment> getAllPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDescIdDesc();
    }

    public void transferPayment(PaymentTransferRequest request, Principal principal) {
        Payment payment = createPaymentFromRequest(request);
        paymentRepository.save(payment);

        Account senderAccount = accountRepository.findByIban(payment.getSenderAccount())
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found"));
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
        paymentRepository.save(payment);

    }
}
