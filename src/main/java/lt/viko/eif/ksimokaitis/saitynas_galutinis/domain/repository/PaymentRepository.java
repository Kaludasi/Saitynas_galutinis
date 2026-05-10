package lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.repository;

import lt.viko.eif.ksimokaitis.saitynas_galutinis.domain.model.Payment;

import java.util.List;

public interface PaymentRepository {

    List<Payment> findAll();
}
