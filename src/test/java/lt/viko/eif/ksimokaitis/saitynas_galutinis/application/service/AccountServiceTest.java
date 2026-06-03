package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Account;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.AccountRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AppUserJpaRepository appUserJpaRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void openAccountForUserIdRejectsUnsupportedCurrency() {
        assertThatThrownBy(() -> accountService.openAccountForUserId(1L, "jonas", "JPY"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported currency.");
    }

    @Test
    void openAccountForUserIdCreatesZeroBalanceAccountWithGeneratedIban() {
        when(accountRepository.existsByIban(any())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account account = accountService.openAccountForUserId(7L, "jonas", "eur");

        assertThat(account.getAppUserId()).isEqualTo(7L);
        assertThat(account.getOwnerName()).isEqualTo("jonas");
        assertThat(account.getCurrency()).isEqualTo("EUR");
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2));
        assertThat(account.getIban()).startsWith("LT");
        assertThat(account.getIban()).hasSize(20);
        verify(accountRepository).save(any(Account.class));
    }
}
