package de.cotto.bitbook.backend.transaction.persistence;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.transaction.AddressTransactionsDao;
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
        addressTransactionsRepository.save(AddressTransactionsJpaDto.fromModel(addressTransactions));
    }

    @Override
    public AddressTransactions getAddressTransactions(Address address, Chain chain) {
        return addressTransactionsRepository.findById(AddressTransactionsJpaDtoId.fromModels(address, chain))
                .map(AddressTransactionsJpaDto::toModel)
                .orElse(AddressTransactions.unknown(chain));
    }

    @Override
    public Set<Address> getAddressesStartingWith(String addressPrefix) {
        return addressTransactionsRepository.findByAddressStartingWith(addressPrefix).stream()
                .map(AddressView::getAddress)
                .map(Address::new)
                .collect(toSet());
    }
}
