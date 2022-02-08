package de.cotto.bitbook.ownership;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Input;
import de.cotto.bitbook.backend.model.Output;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.AddressTransactionsService;
import de.cotto.bitbook.backend.transaction.BalanceService;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.ownership.persistence.AddressOwnershipDaoImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS_2;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.model.TransactionFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.TransactionFixtures.ADDRESS_2;
import static de.cotto.bitbook.backend.model.TransactionFixtures.ADDRESS_3;
import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_2;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_3;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_4;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH_4;
import static de.cotto.bitbook.ownership.OwnershipStatus.OWNED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressOwnershipServiceTest {
    private static final String DESCRIPTION = "foo";

    @InjectMocks
    private AddressOwnershipService addressOwnershipService;

    @Mock
    private AddressOwnershipDaoImpl ownedAddressesDao;

    @Mock
    private BalanceService balanceService;

    @Mock
    private AddressTransactionsService addressTransactionsService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Test
    void getOwnedAddresses() {
        Set<Address> addresses = Set.of(ADDRESS, ADDRESS_2);
        when(ownedAddressesDao.getOwnedAddresses()).thenReturn(addresses);

        Set<Address> ownedAddresses = addressOwnershipService.getOwnedAddresses();

        assertThat(ownedAddresses).isEqualTo(addresses);
    }

    @Test
    void getOwnedAddressesWithDescription() {
        Set<Address> addresses = Set.of(ADDRESS, ADDRESS_2);
        AddressWithDescription addressWithDescription1 = new AddressWithDescription(ADDRESS, "x1");
        AddressWithDescription addressWithDescription2 = new AddressWithDescription(ADDRESS_2);
        when(ownedAddressesDao.getOwnedAddresses()).thenReturn(addresses);
        when(addressDescriptionService.get(ADDRESS)).thenReturn(addressWithDescription1);
        when(addressDescriptionService.get(ADDRESS_2)).thenReturn(addressWithDescription2);

        Set<AddressWithDescription> ownedAddresses = addressOwnershipService.getOwnedAddressesWithDescription();

        assertThat(ownedAddresses).isEqualTo(Set.of(addressWithDescription1, addressWithDescription2));
    }

    @Nested
    class GetNeighbourTransactions {
        @Test
        void counts_transactions_only_once() {
            Set<Address> addresses = Set.of(INPUT_ADDRESS_1, ADDRESS_2);
            when(ownedAddressesDao.getOwnedAddresses()).thenReturn(addresses);
            mockTransactionHashes(Set.of(INPUT_ADDRESS_1, ADDRESS_2), TRANSACTION);

            assertThat(addressOwnershipService.getNeighbourTransactions()).containsOnly(
                    entry(TRANSACTION, TRANSACTION.getDifferenceForAddress(INPUT_ADDRESS_1).add(TRANSACTION.getFees()))
            );
        }

        @Test
        void ignores_fee_owned_to_foreign() {
            Address ownedAddress = ADDRESS;
            Address foreignAddress = ADDRESS_2;
            when(ownedAddressesDao.getOwnedAddresses()).thenReturn(Set.of(ownedAddress));
            when(ownedAddressesDao.getForeignAddresses()).thenReturn(Set.of(foreignAddress));
            Transaction transactionWithFee = sendOneSatoshiTo(ownedAddress, foreignAddress);
            mockTransactionHashes(ownedAddress, transactionWithFee);

            assertThat(addressOwnershipService.getNeighbourTransactions()).containsOnly(
                    entry(transactionWithFee, Coins.NONE)
            );
        }

        @Test
        void ignores_fee_foreign_to_owned() {
            Address foreignAddress = ADDRESS;
            Address ownedAddress = ADDRESS_2;
            when(ownedAddressesDao.getForeignAddresses()).thenReturn(Set.of(foreignAddress));
            when(ownedAddressesDao.getOwnedAddresses()).thenReturn(Set.of(ownedAddress));
            Transaction transactionWithFee = sendOneSatoshiWithFeeTo(foreignAddress, ownedAddress);
            mockTransactionHashes(ownedAddress, transactionWithFee);

            assertThat(addressOwnershipService.getNeighbourTransactions()).containsOnly(
                    entry(transactionWithFee, Coins.NONE)
            );
        }

        @Test
        void ignores_owned_to_foreign() {
            Address ownedAddress = ADDRESS;
            Address foreignAddress = ADDRESS_2;
            when(ownedAddressesDao.getOwnedAddresses()).thenReturn(Set.of(ownedAddress));
            when(ownedAddressesDao.getForeignAddresses()).thenReturn(Set.of(foreignAddress));
            Transaction transactionToForeignAddress = getTransaction(
                    new Input(Coins.ofSatoshis(1), ownedAddress),
                    new Output(Coins.ofSatoshis(1), foreignAddress)
            );
            mockTransactionHashes(ADDRESS, transactionToForeignAddress);

            assertThat(addressOwnershipService.getNeighbourTransactions()).containsOnly(
                    entry(transactionToForeignAddress, Coins.NONE)
            );
        }

        @Test
        void foreign_to_owned_ignores_unknown_output_sibling() {
            Address foreignAddress = ADDRESS;
            Address ownedAddress = ADDRESS_2;
            when(ownedAddressesDao.getOwnedAddresses()).thenReturn(Set.of(ownedAddress));
            when(ownedAddressesDao.getForeignAddresses()).thenReturn(Set.of(foreignAddress));
            Transaction transaction = createTransactionSplittingInput(foreignAddress, ownedAddress, ADDRESS_3);
            mockTransactionHashes(ownedAddress, transaction);

            assertThat(addressOwnershipService.getNeighbourTransactions()).containsOnly(
                    entry(transaction, Coins.NONE)

            );
        }

        @Test
        void owned_to_foreign_includes_unknown_output_sibling() {
            Address ownedAddress = ADDRESS;
            Address foreignAddress = ADDRESS_2;
            when(ownedAddressesDao.getOwnedAddresses()).thenReturn(Set.of(ownedAddress));
            when(ownedAddressesDao.getForeignAddresses()).thenReturn(Set.of(foreignAddress));
            Transaction transaction = createTransactionSplittingInput(ownedAddress, foreignAddress, ADDRESS_3);
            mockTransactionHashes(ownedAddress, transaction);

            assertThat(addressOwnershipService.getNeighbourTransactions()).containsOnly(
                    entry(transaction, Coins.ofSatoshis(-1))
            );
        }

        @Test
        void owned_to_owned_includes_unknown_output_sibling() {
            Address ownedAddress = ADDRESS;
            Address ownedAddress2 = ADDRESS_2;
            Set<Address> ownedAddresses = Set.of(ownedAddress, ownedAddress2);
            when(ownedAddressesDao.getOwnedAddresses()).thenReturn(ownedAddresses);
            Transaction transaction = createTransactionSplittingInput(ownedAddress, ownedAddress2, ADDRESS_3);
            when(addressTransactionsService.getTransactionsForAddresses(ownedAddresses)).thenReturn(Set.of(
                    new AddressTransactions(
                            ownedAddress,
                            Set.of(TRANSACTION_HASH),
                            LAST_CHECKED_AT_BLOCK_HEIGHT
                    ),
                    new AddressTransactions(
                            ownedAddress,
                            Set.of(TRANSACTION_HASH_2),
                            LAST_CHECKED_AT_BLOCK_HEIGHT
                    )
            ));
            when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2)))
                    .thenReturn(Set.of(transaction));

            assertThat(addressOwnershipService.getNeighbourTransactions()).containsOnly(
                    entry(transaction, Coins.ofSatoshis(-1))
            );
        }

        @Test
        void unknown_to_owned() {
            Address ownedAddress = ADDRESS_2;
            when(ownedAddressesDao.getOwnedAddresses()).thenReturn(Set.of(ownedAddress));
            Transaction transaction = getTransaction(
                    new Input(Coins.ofSatoshis(1), ADDRESS),
                    new Output(Coins.ofSatoshis(1), ownedAddress)
            );
            mockTransactionHashes(ownedAddress, transaction);

            assertThat(addressOwnershipService.getNeighbourTransactions()).containsOnly(
                    entry(transaction, Coins.ofSatoshis(1))
            );
        }

        @Test
        void returns_aggregated_coin_difference_per_transaction() {
            Set<Address> addresses = Set.of(INPUT_ADDRESS_1, INPUT_ADDRESS_2);
            when(ownedAddressesDao.getOwnedAddresses()).thenReturn(addresses);
            when(addressTransactionsService.getTransactionsForAddresses(addresses))
                    .thenReturn(Set.of(ADDRESS_TRANSACTIONS, ADDRESS_TRANSACTIONS_2));
            when(transactionService.getTransactionDetails(
                    Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2, TRANSACTION_HASH_3, TRANSACTION_HASH_4)
            )).thenReturn(Set.of(TRANSACTION, TRANSACTION_2, TRANSACTION_3, TRANSACTION_4));

            assertThat(addressOwnershipService.getNeighbourTransactions()).containsOnly(
                    entry(TRANSACTION, Coins.ofSatoshis(-2_147_484_882L)),
                    entry(TRANSACTION_2, Coins.NONE),
                    entry(TRANSACTION_3, Coins.ofSatoshis(-22_749)),
                    entry(TRANSACTION_4, Coins.ofSatoshis(-2_147_483_646L))
            );
        }
    }

    private Transaction sendOneSatoshiWithFeeTo(Address sourceAddress, Address targetAddress) {
        return new Transaction(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                Coins.ofSatoshis(1),
                List.of(new Input(Coins.ofSatoshis(2), sourceAddress)),
                List.of(new Output(Coins.ofSatoshis(1), targetAddress))
        );
    }

    private Transaction sendOneSatoshiTo(Address sourceAddress, Address targetAddress) {
        return new Transaction(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                Coins.NONE,
                List.of(new Input(Coins.ofSatoshis(1), sourceAddress)),
                List.of(new Output(Coins.ofSatoshis(1), targetAddress))
        );
    }

    @Test
    void setAddressAsOwned() {
        addressOwnershipService.setAddressAsOwned(ADDRESS);
        verify(ownedAddressesDao).setAddressAsOwned(ADDRESS);
    }

    @Test
    void setAddressAsOwned_with_description() {
        addressOwnershipService.setAddressAsOwned(ADDRESS, DESCRIPTION);
        verify(ownedAddressesDao).setAddressAsOwned(ADDRESS);
    }

    @Test
    void setAddressAsOwned_with_description_persists_description() {
        addressOwnershipService.setAddressAsOwned(ADDRESS, DESCRIPTION);
        verify(addressDescriptionService).set(ADDRESS, DESCRIPTION);
    }

    @Test
    void setAddressAsOwned_requests_address_details_in_background() {
        addressOwnershipService.setAddressAsOwned(ADDRESS);
        verify(addressTransactionsService).requestTransactionsInBackground(ADDRESS);
    }

    @Test
    void setAddressAsOwned_with_description_requests_address_details_in_background() {
        addressOwnershipService.setAddressAsOwned(ADDRESS, DESCRIPTION);
        verify(addressTransactionsService).requestTransactionsInBackground(ADDRESS);
    }

    @Test
    void setAddressAsForeign() {
        addressOwnershipService.setAddressAsForeign(ADDRESS);
        verify(ownedAddressesDao).setAddressAsForeign(ADDRESS);
    }

    @Test
    void setAddressAsForeign_with_description() {
        addressOwnershipService.setAddressAsForeign(ADDRESS, "hi");
        verify(ownedAddressesDao).setAddressAsForeign(ADDRESS);
    }

    @Test
    void setAddressAsForeign_with_description_persists_description() {
        addressOwnershipService.setAddressAsForeign(ADDRESS, DESCRIPTION);
        verify(addressDescriptionService).set(ADDRESS, DESCRIPTION);
    }

    @Test
    void resetOwnership() {
        addressOwnershipService.resetOwnership(ADDRESS);
        verify(ownedAddressesDao).remove(ADDRESS);
    }

    @Test
    void getBalance() {
        Address address1 = new Address("abc");
        Address address2 = new Address("def");
        when(ownedAddressesDao.getOwnedAddresses()).thenReturn(Set.of(address1, address2));
        when(balanceService.getBalance(address1)).thenReturn(Coins.ofSatoshis(100));
        when(balanceService.getBalance(address2)).thenReturn(Coins.ofSatoshis(23));

        Coins balance = addressOwnershipService.getBalance();

        assertThat(balance).isEqualTo(Coins.ofSatoshis(123));
    }

    @Test
    void getBalance_preloads_address_transactions() {
        Set<Address> addresses = Set.of(ADDRESS, ADDRESS_2);
        when(ownedAddressesDao.getOwnedAddresses()).thenReturn(addresses);
        when(balanceService.getBalance(any())).thenReturn(Coins.ofSatoshis(1));

        addressOwnershipService.getBalance();

        InOrder inOrder = inOrder(addressTransactionsService, balanceService);
        inOrder.verify(addressTransactionsService).getTransactionsForAddresses(addresses);
        inOrder.verify(balanceService, atLeastOnce()).getBalance(any());
    }

    @Test
    void getOwnershipStatus_owned() {
        when(ownedAddressesDao.getOwnershipStatus(ADDRESS)).thenReturn(OWNED);
        assertThat(addressOwnershipService.getOwnershipStatus(ADDRESS)).isEqualTo(OWNED);
    }

    @Nested
    class GetMyTransactionsWithCoins {

        private Address ownedAddress;

        @BeforeEach
        void setUp() {
            ownedAddress = ADDRESS;
            when(ownedAddressesDao.getOwnedAddresses()).thenReturn(Set.of(ownedAddress));
        }

        @Test
        void returns_transactions() {
            Set<Address> ownedAddresses = Set.of(ownedAddress, ADDRESS_2);
            when(ownedAddressesDao.getOwnedAddresses()).thenReturn(ownedAddresses);
            when(addressTransactionsService.getTransactionsForAddresses(ownedAddresses))
                    .thenReturn(Set.of(ADDRESS_TRANSACTIONS, ADDRESS_TRANSACTIONS_2));
            Set<TransactionHash> hashes = Set.of(
                    TRANSACTION_HASH,
                    TRANSACTION_HASH_2,
                    TRANSACTION_HASH_3,
                    TRANSACTION_HASH_4
            );
            when(transactionService.getTransactionDetails(hashes)).thenReturn(Set.of(TRANSACTION, TRANSACTION_2));

            assertThat(addressOwnershipService.getMyTransactionsWithCoins())
                    .containsOnlyKeys(TRANSACTION, TRANSACTION_2);
        }

        @Test
        void counts_fee() {
            Address ownedAddress2 = ADDRESS_2;
            Set<Address> ownedAddresses = Set.of(ownedAddress, ownedAddress2);
            when(ownedAddressesDao.getOwnedAddresses()).thenReturn(ownedAddresses);
            Transaction transactionWithFee = sendOneSatoshiWithFeeTo(ownedAddress, ownedAddress2);
            mockTransactionHashes(ownedAddresses, transactionWithFee);

            assertThat(addressOwnershipService.getMyTransactionsWithCoins()).containsOnly(
                    entry(transactionWithFee, Coins.ofSatoshis(-1))
            );
        }

        @Test
        void owned_to_foreign() {
            Transaction transactionWithFee = sendOneSatoshiTo(ownedAddress, ADDRESS_2);
            mockTransactionHashes(ownedAddress, transactionWithFee);

            assertThat(addressOwnershipService.getMyTransactionsWithCoins()).containsOnly(
                    entry(transactionWithFee, Coins.ofSatoshis(-1))
            );
        }

        @Test
        void foreign_to_owned() {
            Transaction transactionWithFee = sendOneSatoshiTo(ADDRESS_2, ownedAddress);
            mockTransactionHashes(ownedAddress, transactionWithFee);

            assertThat(addressOwnershipService.getMyTransactionsWithCoins()).containsOnly(
                    entry(transactionWithFee, Coins.ofSatoshis(1))
            );
        }

        @Test
        void ignores_fee_foreign_to_owned() {
            Transaction transactionWithFee = sendOneSatoshiWithFeeTo(ADDRESS_2, ownedAddress);
            mockTransactionHashes(ownedAddress, transactionWithFee);

            assertThat(addressOwnershipService.getMyTransactionsWithCoins()).containsOnly(
                    entry(transactionWithFee, Coins.ofSatoshis(1))
            );
        }

        @Test
        void foreign_to_owned_ignores_unknown_output_sibling() {
            Transaction transaction = createTransactionSplittingInput(ADDRESS_2, ownedAddress, ADDRESS_3);
            mockTransactionHashes(ownedAddress, transaction);

            assertThat(addressOwnershipService.getMyTransactionsWithCoins()).containsOnly(
                    entry(transaction, Coins.ofSatoshis(1))

            );
        }

        @Test
        void owned_to_two_addresses() {
            Transaction transaction = createTransactionSplittingInput(ownedAddress, ADDRESS_2, ADDRESS_3);
            mockTransactionHashes(ownedAddress, transaction);

            assertThat(addressOwnershipService.getMyTransactionsWithCoins()).containsOnly(
                    entry(transaction, Coins.ofSatoshis(-2))
            );
        }

        @Test
        void owned_to_other_address_with_change() {
            Address ownedAddress2 = ADDRESS_3;
            Set<Address> ownedAddresses = Set.of(ownedAddress, ownedAddress2);
            when(ownedAddressesDao.getOwnedAddresses()).thenReturn(ownedAddresses);
            Transaction transaction = createTransactionSplittingInput(ownedAddress, ADDRESS_2, ownedAddress2);
            mockTransactionHashes(ownedAddresses, transaction);

            assertThat(addressOwnershipService.getMyTransactionsWithCoins()).containsOnly(
                    entry(transaction, Coins.ofSatoshis(-1))
            );
        }
    }

    private void mockTransactionHashes(Address address, Transaction transaction) {
        mockTransactionHashes(Set.of(address), transaction);
    }

    private void mockTransactionHashes(Set<Address> addresses, Transaction transaction) {
        when(addressTransactionsService.getTransactionsForAddresses(addresses)).thenReturn(
                Set.of(new AddressTransactions(
                        new Address("x"),
                        Set.of(TRANSACTION_HASH),
                        LAST_CHECKED_AT_BLOCK_HEIGHT
                )));
        when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH))).thenReturn(Set.of(transaction));
    }

    @SuppressWarnings("SameParameterValue")
    private Transaction createTransactionSplittingInput(Address source, Address target1, Address target2) {
        return getTransaction(
                new Input(Coins.ofSatoshis(2), source),
                new Output(Coins.ofSatoshis(1), target1),
                new Output(Coins.ofSatoshis(1), target2)
        );
    }

    private Transaction getTransaction(Input input, Output... outputs) {
        return new Transaction(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                Coins.NONE,
                List.of(input),
                Arrays.asList(outputs)
        );
    }
}