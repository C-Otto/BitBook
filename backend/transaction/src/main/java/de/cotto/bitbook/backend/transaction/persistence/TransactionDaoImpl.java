package de.cotto.bitbook.backend.transaction.persistence;

import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.TransactionDao;
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
    public Transaction getTransaction(TransactionHash transactionHash, Chain chain) {
        return transactionRepository.findById(new TransactionJpaDtoId(transactionHash.toString(), chain.toString()))
                .map(TransactionJpaDto::toModel)
                .orElse(Transaction.unknown(chain));
    }

    @Override
    public void saveTransaction(Transaction transaction) {
        transactionRepository.save(TransactionJpaDto.fromModel(transaction));
    }
}
