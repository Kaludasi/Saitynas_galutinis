package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Account;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.CurrencyExchange;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Payment;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.AccountRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.CurrencyExchangeRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.PaymentRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserEntity;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserJpaRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.CurrencyExchangeRequest;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.interfaces.rest.dto.CurrencyExchangeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyExchangeServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CurrencyExchangeRepository currencyExchangeRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AppUserJpaRepository appUserJpaRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Spy
    @InjectMocks
    private CurrencyExchangeService currencyExchangeService;

    @Test
    void exchangeRejectsSameSourceAndTargetAccount() {
        CurrencyExchangeRequest request = new CurrencyExchangeRequest();
        request.setSourceAccountId(1L);
        request.setTargetAccountId(1L);
        request.setAmount(new BigDecimal("10.00"));

        assertThatThrownBy(() -> currencyExchangeService.exchange(request, "jonas"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Source and target accounts must be different");
    }

    @Test
    void exchangeAppliesBalancesAndCreatesAuditRecords() {
        AppUserEntity user = new AppUserEntity();
        user.setId(9L);
        user.setUsername("jonas");

        Account source = account(1L, "LT1", "jonas", new BigDecimal("100.00"), "EUR", 9L);
        Account target = account(2L, "LT2", "jonas", new BigDecimal("20.00"), "USD", 9L);
        CurrencyExchangeRequest request = new CurrencyExchangeRequest();
        request.setSourceAccountId(1L);
        request.setTargetAccountId(2L);
        request.setAmount(new BigDecimal("10.00"));

        when(appUserJpaRepository.findByUsername("jonas")).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndAppUserId(1L, 9L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdAndAppUserId(2L, 9L)).thenReturn(Optional.of(target));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(currencyExchangeRepository.save(any(CurrencyExchange.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(exchangeRateService.fetchExchangeRate("EUR", "USD")).thenReturn(new BigDecimal("1.20"));

        CurrencyExchangeResponse response = currencyExchangeService.exchange(request, "jonas");

        assertThat(response.convertedAmount()).isEqualByComparingTo("12.00");
        assertThat(response.sourceBalance()).isEqualByComparingTo("90.00");
        assertThat(response.targetBalance()).isEqualByComparingTo("32.00");
        assertThat(source.getBalance()).isEqualByComparingTo("90.00");
        assertThat(target.getBalance()).isEqualByComparingTo("32.00");

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(2)).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getAllValues()).hasSize(2);
        verify(currencyExchangeRepository).save(any(CurrencyExchange.class));
    }

    private static Account account(Long id, String iban, String ownerName, BigDecimal balance, String currency, Long appUserId) {
        Account account = new Account(iban, ownerName, balance, currency, appUserId, LocalDateTime.now());
        try {
            var field = Account.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(account, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
        return account;
    }
}
