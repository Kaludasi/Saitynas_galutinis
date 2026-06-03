package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Account;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Payment;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.PaymentStatus;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.AccountRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.PaymentRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.PaymentTransferRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentTransferServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private PaymentTransferService paymentTransferService;

    @Test
    void transferRejectsSenderAccountOfAnotherUser() {
        PaymentTransferRequest request = request("LT1", "LT2", "10.00", "EUR");
        Account senderAccount = account("LT1", "jonas", new BigDecimal("100.00"), "EUR", 99L);

        when(accountRepository.findByIban("LT1")).thenReturn(Optional.of(senderAccount));
        when(accountService.getUserIdByUsername("jonas")).thenReturn(10L);

        assertThatThrownBy(() -> paymentTransferService.transfer(request, principal("jonas")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sender account does not belong to the authorized user");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void transferMovesFundsAndCompletesPaymentForSameCurrency() {
        PaymentTransferRequest request = request("LT1", "LT2", "10.00", "EUR");
        Account senderAccount = account("LT1", "jonas", new BigDecimal("100.00"), "EUR", 10L);
        Account receiverAccount = account("LT2", "jonas", new BigDecimal("5.00"), "EUR", 10L);

        when(accountRepository.findByIban("LT1")).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByIban("LT2")).thenReturn(Optional.of(receiverAccount));
        when(accountService.getUserIdByUsername("jonas")).thenReturn(10L);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment payment = paymentTransferService.transfer(request, principal("jonas"));

        assertThat(senderAccount.getBalance()).isEqualByComparingTo("90.00");
        assertThat(receiverAccount.getBalance()).isEqualByComparingTo("15.00");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(accountRepository).flush();
    }

    private static PaymentTransferRequest request(String sender, String receiver, String amount, String currency) {
        PaymentTransferRequest request = new PaymentTransferRequest();
        request.setSenderAccount(sender);
        request.setReceiverAccount(receiver);
        request.setAmount(new BigDecimal(amount));
        request.setCurrency(currency);
        request.setDescription("Test transfer");
        return request;
    }

    private static Account account(String iban, String ownerName, BigDecimal balance, String currency, Long appUserId) {
        return new Account(iban, ownerName, balance, currency, appUserId, LocalDateTime.now());
    }

    private static Principal principal(String username) {
        return () -> username;
    }
}
