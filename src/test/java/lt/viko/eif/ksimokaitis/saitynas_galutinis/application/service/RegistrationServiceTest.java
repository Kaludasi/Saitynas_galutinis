package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserEntity;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private AppUserJpaRepository appUserJpaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void registerRejectsDuplicateUsername() {
        when(appUserJpaRepository.existsByUsername("jonas")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.register("jonas", "jonas@example.com", "Secret#2026", "EUR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username is already taken.");

        verify(accountService, never()).openAccountForUserId(any(), any(), any());
    }

    @Test
    void registerSavesUserAndOpensFirstAccount() {
        when(passwordEncoder.encode("Secret#2026")).thenReturn("encoded-secret");
        when(appUserJpaRepository.save(any(AppUserEntity.class))).thenAnswer(invocation -> {
            AppUserEntity user = invocation.getArgument(0);
            user.setId(15L);
            return user;
        });

        registrationService.register("jonas", "jonas@example.com", "Secret#2026", "EUR");

        ArgumentCaptor<AppUserEntity> userCaptor = ArgumentCaptor.forClass(AppUserEntity.class);
        verify(appUserJpaRepository).save(userCaptor.capture());
        AppUserEntity savedUser = userCaptor.getValue();

        assertThat(savedUser.getUsername()).isEqualTo("jonas");
        assertThat(savedUser.getEmail()).isEqualTo("jonas@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("encoded-secret");
        assertThat(savedUser.getRole()).isEqualTo("USER");
        assertThat(savedUser.isEnabled()).isTrue();

        verify(accountService).openAccountForUserId(15L, "jonas", "EUR");
    }
}
