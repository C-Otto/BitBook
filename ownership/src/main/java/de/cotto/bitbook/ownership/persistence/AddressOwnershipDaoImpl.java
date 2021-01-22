package de.cotto.bitbook.ownership.persistence;

import de.cotto.bitbook.ownership.AddressOwnershipDao;
import de.cotto.bitbook.ownership.OwnershipStatus;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
@Transactional
public class AddressOwnershipDaoImpl implements AddressOwnershipDao {
    private final AddressOwnershipRepository addressOwnershipRepository;

    public AddressOwnershipDaoImpl(AddressOwnershipRepository addressOwnershipRepository) {
        this.addressOwnershipRepository = addressOwnershipRepository;
    }

    @Override
    public Set<String> getOwnedAddresses() {
        return getAddressesWithStatus(OwnershipStatus.OWNED);
    }

    @Override
    public Set<String> getForeignAddresses() {
        return getAddressesWithStatus(OwnershipStatus.FOREIGN);
    }

    @Override
    public void setAddressAsOwned(String address) {
        addressOwnershipRepository.save(new AddressOwnershipJpaDto(address, OwnershipStatus.OWNED));
    }

    @Override
    public void setAddressAsForeign(String address) {
        addressOwnershipRepository.save(new AddressOwnershipJpaDto(address, OwnershipStatus.FOREIGN));
    }

    @Override
    public void remove(String address) {
        addressOwnershipRepository.deleteById(address);
    }

    @Override
    public OwnershipStatus getOwnershipStatus(String address) {
        return addressOwnershipRepository.findByAddress(address)
                .map(AddressOwnershipJpaDto::getOwnershipStatus)
                .orElse(OwnershipStatus.UNKNOWN);
    }

    private Set<String> getAddressesWithStatus(OwnershipStatus foreign) {
        return addressOwnershipRepository.findAll().stream()
                .filter(dto -> dto.getOwnershipStatus().equals(foreign))
                .map(AddressOwnershipJpaDto::getAddress)
                .collect(toSet());
    }
}
