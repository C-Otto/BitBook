package de.cotto.bitbook.backend.transaction.persistence;

import de.cotto.bitbook.backend.transaction.AddressTransactionsDao;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
@Transactional
public class AddressTransactionsDaoImpl implements AddressTransactionsDao {
    private final AddressTransactionsRepository addressTransactionsRepository;

    public AddressTransactionsDaoImpl(AddressTransactionsRepository addressTransactionsRepository) {
        this.addressTransactionsRepository = addressTransactionsRepository;
    }

    @Override
    public void saveAddressTransactions(AddressTransactions addressTransactions) {
        if (addressTransactions.getTransactionHashes().isEmpty()) {
            return;
        }
        addressTransactionsRepository.save(AddressTransactionsJpaDto.fromModel(addressTransactions));
    }

    @Override
    public AddressTransactions getAddressTransactions(String address) {
        return addressTransactionsRepository.findById(address)
                .map(AddressTransactionsJpaDto::toModel)
                .orElse(AddressTransactions.UNKNOWN);
    }

    @Override
    public Set<String> getAddressesStartingWith(String addressPrefix) {
        return addressTransactionsRepository.findByAddressStartingWith(addressPrefix).stream()
                .map(AddressView::getAddress)
                .collect(toSet());
    }
}
