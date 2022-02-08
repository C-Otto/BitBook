package de.cotto.bitbook.backend.transaction.persistence;

import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.TransactionCompletionDao;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional
public class TransactionCompletionDaoImpl implements TransactionCompletionDao {
    private final TransactionRepository transactionRepository;
    private final AddressTransactionsRepository addressTransactionsRepository;

    public TransactionCompletionDaoImpl(
            TransactionRepository transactionRepository,
            AddressTransactionsRepository addressTransactionsRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.addressTransactionsRepository = addressTransactionsRepository;
    }

    @Override
    public Set<TransactionHash> completeFromTransactionDetails(String hashPrefix) {
        return transactionRepository.findByHashStartingWith(hashPrefix).stream()
                .map(TransactionHashView::getHash)
                .map(TransactionHash::new)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<TransactionHash> completeFromAddressTransactionHashes(String hashPrefix) {
        return addressTransactionsRepository.findTransactionHashesByPrefix(hashPrefix).stream()
                .map(TransactionHash::new)
                .collect(Collectors.toSet());
    }
}
