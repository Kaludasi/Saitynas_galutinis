package lt.viko.eif.ksimokaitis.saitynas_galutinis.application.service;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Account;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository.AccountRepository;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserEntity;
import lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence.AppUserJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AppUserJpaRepository appUserJpaRepository;

    public AccountService(AccountRepository accountRepository, AppUserJpaRepository appUserJpaRepository) {
        this.accountRepository = accountRepository;
        this.appUserJpaRepository = appUserJpaRepository;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAllByOrderByCreatedAtDescIdDesc();
    }

    public List<Account> getAccountsForUsername(String username) {
        return accountRepository.findAllByAppUserIdOrderByCreatedAtDescIdDesc(getUserIdByUsername(username));
    }

    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
    }

    public Account getAccountByIdForUsername(Long accountId, String username) {
        return accountRepository.findByIdAndAppUserId(accountId, getUserIdByUsername(username))
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
    }

    public Long getUserIdByUsername(String username) {
        AppUserEntity user = appUserJpaRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return user.getId();
    }
}
