package de.cotto.bitbook.backend.transaction.persistence;

import de.cotto.bitbook.backend.transaction.TransactionCompletionDao;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional
public class TransactionCompletionDaoImpl implements TransactionCompletionDao {
    private final TransactionRepository transactionRepository;

    public TransactionCompletionDaoImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Set<String> getTransactionHashesStartingWith(String hashPrefix) {
        return transactionRepository.findByHashStartingWith(hashPrefix).stream()
                .map(TransactionHashView::getHash)
                .collect(Collectors.toSet());
    }
}
