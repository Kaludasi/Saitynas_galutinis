package lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByOrderByCreatedAtDescIdDesc();

    List<Account> findAllByAppUserIdOrderByCreatedAtDescIdDesc(Long appUserId);

    Optional<Account> findByIban(String accountNumber);

    List<Account> findByOwnerName(String ownerName);

    Optional<Account> findById(Long id);

    Optional<Account> findByIdAndAppUserId(Long id, Long appUserId);
}
