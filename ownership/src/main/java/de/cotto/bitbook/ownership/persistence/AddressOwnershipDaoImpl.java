package de.cotto.bitbook.ownership.persistence;

import de.cotto.bitbook.backend.model.Address;
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
    public Set<Address> getOwnedAddresses() {
        return getAddressesWithStatus(OwnershipStatus.OWNED);
    }

    @Override
    public Set<Address> getForeignAddresses() {
        return getAddressesWithStatus(OwnershipStatus.FOREIGN);
    }

    @Override
    public void setAddressAsOwned(Address address) {
        addressOwnershipRepository.save(new AddressOwnershipJpaDto(address.toString(), OwnershipStatus.OWNED));
    }

    @Override
    public void setAddressAsForeign(Address address) {
        addressOwnershipRepository.save(new AddressOwnershipJpaDto(address.toString(), OwnershipStatus.FOREIGN));
    }

    @Override
    public void remove(Address address) {
        addressOwnershipRepository.deleteById(address.toString());
    }

    @Override
    public OwnershipStatus getOwnershipStatus(Address address) {
        return addressOwnershipRepository.findByAddress(address.toString())
                .map(AddressOwnershipJpaDto::getOwnershipStatus)
                .orElse(OwnershipStatus.UNKNOWN);
    }

    private Set<Address> getAddressesWithStatus(OwnershipStatus foreign) {
        return addressOwnershipRepository.findAll().stream()
                .filter(dto -> dto.getOwnershipStatus().equals(foreign))
                .map(AddressOwnershipJpaDto::getAddress)
                .map(Address::new)
                .collect(toSet());
    }
}
