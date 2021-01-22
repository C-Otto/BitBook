package de.cotto.bitbook.backend.transaction.persistence;

import com.google.common.collect.Sets;
import de.cotto.bitbook.backend.transaction.AddressCompletionDao;
import de.cotto.bitbook.backend.transaction.AddressTransactionsDao;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
@Transactional
public class AddressCompletionDaoImpl implements AddressCompletionDao {
    private final AddressTransactionsDao addressTransactionsDao;
    private final InputRepository inputRepository;
    private final OutputRepository outputRepository;

    public AddressCompletionDaoImpl(
            AddressTransactionsDao addressTransactionsDao,
            InputRepository inputRepository,
            OutputRepository outputRepository
    ) {
        this.addressTransactionsDao = addressTransactionsDao;
        this.inputRepository = inputRepository;
        this.outputRepository = outputRepository;
    }

    @Override
    public Set<String> completeFromAddressTransactions(String prefix) {
        return addressTransactionsDao.getAddressesStartingWith(prefix);
    }

    @Override
    public Set<String> completeFromInputsAndOutputs(String prefix) {
        return Sets.union(
                inputRepository.findBySourceAddressStartingWith(prefix),
                outputRepository.findByTargetAddressStartingWith(prefix)
        ).stream().map(InputOutputJpaDto::getAddress).collect(toSet());
    }

}
