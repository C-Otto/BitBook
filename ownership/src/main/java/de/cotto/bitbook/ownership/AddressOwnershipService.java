package de.cotto.bitbook.ownership;

import com.google.common.base.Functions;
import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.transaction.AddressTransactionsService;
import de.cotto.bitbook.backend.transaction.BalanceService;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Set<String> getOwnedAddresses() {
        return addressOwnershipDao.getOwnedAddresses();
    }

    public Set<AddressWithDescription> getOwnedAddressesWithDescription() {
        return addressOwnershipDao.getOwnedAddresses().stream().map(addressDescriptionService::get).collect(toSet());
    }

    public Map<Transaction, Coins> getNeighbourTransactions() {
        Set<String> ownedAddresses = getOwnedAddresses();
        Set<String> foreignAddresses = addressOwnershipDao.getForeignAddresses();
        Set<Transaction> transactionsFromToOwned = ownedAddresses.parallelStream()
                .map(addressTransactionsService::getTransactions)
                .map(AddressTransactions::getTransactionHashes)
                .map(transactionService::getTransactionDetails)
                .flatMap(Set::stream)
                .collect(toSet());
        Map<Transaction, Coins> differencesOwned = getDifferences(transactionsFromToOwned, ownedAddresses);
        Map<Transaction, Coins> differencesForeign = getDifferences(transactionsFromToOwned, foreignAddresses);
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

    public void setAddressAsOwned(String address) {
        addressOwnershipDao.setAddressAsOwned(address);
        addressTransactionsService.requestTransactionsInBackground(address);
    }

    public void setAddressAsOwned(String address, String description) {
        setAddressAsOwned(address);
        addressDescriptionService.set(address, description);
    }

    public void setAddressAsForeign(String address) {
        addressOwnershipDao.setAddressAsForeign(address);
    }

    public void setAddressAsForeign(String address, String description) {
        setAddressAsForeign(address);
        addressDescriptionService.set(address, description);
    }

    public void resetOwnership(String address) {
        addressOwnershipDao.remove(address);
    }

    public Coins getBalance() {
        return getOwnedAddresses().parallelStream()
                .map(balanceService::getBalance)
                .reduce(Coins.NONE, Coins::add);
    }

    public OwnershipStatus getOwnershipStatus(String address) {
        return addressOwnershipDao.getOwnershipStatus(address);
    }

    private Map<Transaction, Coins> getDifferences(
            Set<Transaction> transactions,
            Set<String> addresses
    ) {
        return transactions.stream().collect(Collectors.toMap(
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
