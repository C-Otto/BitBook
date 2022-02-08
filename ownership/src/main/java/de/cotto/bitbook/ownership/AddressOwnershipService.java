package de.cotto.bitbook.ownership;

import com.google.common.base.Functions;
import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.AddressTransactionsService;
import de.cotto.bitbook.backend.transaction.BalanceService;
import de.cotto.bitbook.backend.transaction.TransactionService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Component
public class AddressOwnershipService {
    private final AddressOwnershipDao addressOwnershipDao;
    private final AddressDescriptionService addressDescriptionService;
    private final BalanceService balanceService;
    private final AddressTransactionsService addressTransactionsService;
    private final TransactionService transactionService;

    public AddressOwnershipService(
            AddressOwnershipDao addressOwnershipDao,
            AddressDescriptionService addressDescriptionService,
            BalanceService balanceService,
            AddressTransactionsService addressTransactionsService,
            TransactionService transactionService
    ) {
        this.addressOwnershipDao = addressOwnershipDao;
        this.addressDescriptionService = addressDescriptionService;
        this.balanceService = balanceService;
        this.addressTransactionsService = addressTransactionsService;
        this.transactionService = transactionService;
    }

    public Set<Address> getOwnedAddresses() {
        return addressOwnershipDao.getOwnedAddresses();
    }

    public Set<AddressWithDescription> getOwnedAddressesWithDescription() {
        return addressOwnershipDao.getOwnedAddresses().stream().map(addressDescriptionService::get).collect(toSet());
    }

    public Map<Transaction, Coins> getNeighbourTransactions() {
        Set<Address> ownedAddresses = getOwnedAddresses();
        Set<Address> foreignAddresses = addressOwnershipDao.getForeignAddresses();
        Set<Transaction> myTransactions = getMyTransactions();

        Map<Transaction, Coins> differencesOwned = getDifferences(myTransactions, ownedAddresses);
        Map<Transaction, Coins> differencesForeign = getDifferences(myTransactions, foreignAddresses);
        updateWithFeesAsForeign(differencesOwned);
        updateWithKnownForeignIgnoringForeignSurplus(differencesOwned, differencesForeign);
        return differencesOwned;
    }

    private void updateWithFeesAsForeign(Map<Transaction, Coins> differences) {
        for (Map.Entry<Transaction, Coins> entry : differences.entrySet()) {
            Coins fees = entry.getKey().getFees();
            entry.setValue(entry.getValue().add(fees));
        }
    }

    public void setAddressAsOwned(Address address) {
        addressOwnershipDao.setAddressAsOwned(address);
        addressTransactionsService.requestTransactionsInBackground(address);
    }

    public void setAddressAsOwned(Address address, String description) {
        setAddressAsOwned(address);
        addressDescriptionService.set(address, description);
    }

    public void setAddressAsForeign(Address address) {
        addressOwnershipDao.setAddressAsForeign(address);
    }

    public void setAddressAsForeign(Address address, String description) {
        setAddressAsForeign(address);
        addressDescriptionService.set(address, description);
    }

    public void resetOwnership(Address address) {
        addressOwnershipDao.remove(address);
    }

    public Coins getBalance() {
        Set<Address> ownedAddresses = getOwnedAddresses();
        addressTransactionsService.getTransactionsForAddresses(ownedAddresses);
        return ownedAddresses.parallelStream()
                .map(balanceService::getBalance)
                .reduce(Coins.NONE, Coins::add);
    }

    public OwnershipStatus getOwnershipStatus(Address address) {
        return addressOwnershipDao.getOwnershipStatus(address);
    }

    public Map<Transaction, Coins> getMyTransactionsWithCoins() {
        return getDifferences(getMyTransactions(), getOwnedAddresses());
    }

    private Set<Transaction> getMyTransactions() {
        Set<TransactionHash> hashes =
                addressTransactionsService.getTransactionsForAddresses(getOwnedAddresses()).stream()
                .map(AddressTransactions::getTransactionHashes)
                .flatMap(Set::stream)
                .collect(toSet());
        return transactionService.getTransactionDetails(hashes);
    }

    private Map<Transaction, Coins> getDifferences(
            Set<Transaction> transactions,
            Set<Address> addresses
    ) {
        return transactions.stream().collect(toMap(
                Functions.identity(),
                transaction -> transaction.getDifferenceForAddresses(addresses)
        ));
    }

    private void updateWithKnownForeignIgnoringForeignSurplus(
            Map<Transaction, Coins> differencesOwned,
            Map<Transaction, Coins> differencesForeign
    ) {
        for (Map.Entry<Transaction, Coins> entry : differencesOwned.entrySet()) {
            Coins differenceOwned = entry.getValue();
            Coins differenceForeign = differencesForeign.getOrDefault(entry.getKey(), Coins.NONE);
            Coins updated = differenceOwned.add(differenceForeign);
            if (differenceOwned.isPositive() && updated.isNegative()) {
                entry.setValue(Coins.NONE);
            } else {
                entry.setValue(updated);
            }
        }
    }
}
