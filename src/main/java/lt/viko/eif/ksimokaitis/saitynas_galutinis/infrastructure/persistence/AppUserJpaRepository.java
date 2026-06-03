package lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserJpaRepository extends JpaRepository<AppUserEntity, Long> {

    Optional<AppUserEntity> findByUsername(String username);

    Optional<AppUserEntity> findById(Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
