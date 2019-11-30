package webserver.repository;

import org.bitcoinj.core.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import webserver.model.Payment;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
}
