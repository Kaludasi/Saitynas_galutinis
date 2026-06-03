package lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.CurrencyExchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CurrencyExchangeRepository extends JpaRepository<CurrencyExchange, Long> {

    @Query("""
            select ce
            from CurrencyExchange ce
            where ce.sourceAccount.appUserId = :appUserId or ce.targetAccount.appUserId = :appUserId
            order by ce.createdAt desc, ce.id desc
            """)
    List<CurrencyExchange> findAllVisibleByAppUserId(@Param("appUserId") Long appUserId);
}
