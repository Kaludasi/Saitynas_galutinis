package lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.CurrencyExchange;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyExchangeRepository extends JpaRepository<CurrencyExchange, Long> {
}
