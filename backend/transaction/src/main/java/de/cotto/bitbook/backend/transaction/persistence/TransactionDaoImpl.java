package de.cotto.bitbook.backend.transaction.persistence;

import de.cotto.bitbook.backend.transaction.TransactionDao;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
@Transactional
public class TransactionDaoImpl implements TransactionDao {
    private final TransactionRepository transactionRepository;

    public TransactionDaoImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction getTransaction(String transactionHash) {
        return transactionRepository.findById(transactionHash)
                .map(TransactionJpaDto::toModel)
                .orElse(Transaction.UNKNOWN);
    }

    @Override
    public void saveTransaction(Transaction transaction) {
        transactionRepository.save(TransactionJpaDto.fromModel(transaction));
    }
}
