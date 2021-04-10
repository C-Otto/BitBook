package de.cotto.bitbook.backend.persistence;

import de.cotto.bitbook.backend.TransactionWithDescriptionDao;
import de.cotto.bitbook.backend.model.TransactionWithDescription;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
@Transactional
public class TransactionWithDescriptionDaoImpl implements TransactionWithDescriptionDao {
    private final TransactionWithDescriptionRepository repository;

    public TransactionWithDescriptionDaoImpl(TransactionWithDescriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public TransactionWithDescription get(String transactionHash) {
        return repository.findById(transactionHash)
                .map(TransactionWithDescriptionJpaDto::toModel)
                .orElseGet(() -> new TransactionWithDescription(transactionHash));
    }

    @Override
    public void save(TransactionWithDescription transactionWithDescription) {
        repository.save(TransactionWithDescriptionJpaDto.fromModel(transactionWithDescription));
    }

    @Override
    public void remove(String transactionHash) {
        repository.deleteById(transactionHash);
    }

    @Override
    public Set<TransactionWithDescription> findWithDescriptionInfix(String infix) {
        return repository.findByDescriptionContaining(infix).stream()
                .map(TransactionWithDescriptionJpaDto::toModel)
                .collect(toSet());
    }
}
