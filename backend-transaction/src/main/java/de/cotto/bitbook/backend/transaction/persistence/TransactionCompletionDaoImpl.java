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
    private final AddressTransactionsRepository addressTransactionsRepository;

    public TransactionCompletionDaoImpl(
            TransactionRepository transactionRepository,
            AddressTransactionsRepository addressTransactionsRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.addressTransactionsRepository = addressTransactionsRepository;
    }

    @Override
    public Set<String> completeFromTransactionDetails(String hashPrefix) {
        return transactionRepository.findByHashStartingWith(hashPrefix).stream()
                .map(TransactionHashView::getHash)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> completeFromAddressTransactionHashes(String hashPrefix) {
        return addressTransactionsRepository.findTransactionHashesByPrefix(hashPrefix);
    }
}
