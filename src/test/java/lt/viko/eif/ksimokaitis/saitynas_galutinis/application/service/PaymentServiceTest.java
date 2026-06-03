package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Account;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Payment;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.PaymentStatus;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private PaymentTransferService paymentTransferService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void getAllPaymentsForUsernameUsesVisibleAccountIbans() {
        when(accountService.getAccountsForUsername("jonas")).thenReturn(List.of(
                account("LT1", "jonas", new BigDecimal("100.00"), "EUR", 10L),
                account("LT2", "jonas", new BigDecimal("5.00"), "USD", 10L)
        ));
        when(paymentRepository.findVisiblePayments(List.of("LT1", "LT2"))).thenReturn(List.of());

        List<Payment> result = paymentService.getAllPaymentsForUsername("jonas");

        assertThat(result).isEmpty();
        verify(paymentRepository).findVisiblePayments(List.of("LT1", "LT2"));
    }

    @Test
    void transferPaymentDelegatesToDedicatedTransferService() {
        PaymentTransferRequest request = request("LT1", "LT2", "10.00", "EUR");
        Principal principal = principal("jonas");
        Payment completedPayment = new Payment(
                "LT1",
                "LT2",
                new BigDecimal("10.00"),
                "EUR",
                PaymentStatus.COMPLETED,
                "Test transfer",
                LocalDateTime.now()
        );

        when(paymentTransferService.transfer(any(PaymentTransferRequest.class), any())).thenReturn(completedPayment);

        Payment payment = paymentService.transferPayment(request, principal);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(paymentTransferService).transfer(eq(request), eq(principal));
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
