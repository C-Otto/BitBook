package de.cotto.bitbook.backend.transaction.blockcypher;

import de.cotto.bitbook.backend.transaction.TransactionsRequestKey;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.bitbook.backend.transaction.blockcypher.BlockcypherAddressTransactionsFixtures.ADDRESS_DETAILS_INCOMPLETE;
import static de.cotto.bitbook.backend.transaction.blockcypher.BlockcypherAddressTransactionsFixtures.ADDRESS_DETAILS_SECOND_PART;
import static de.cotto.bitbook.backend.transaction.blockcypher.BlockcypherAddressTransactionsFixtures.BLOCKCYPHER_ADDRESS_DETAILS;
import static de.cotto.bitbook.backend.transaction.blockcypher.BlockcypherAddressTransactionsFixtures.BLOCKCYPHER_ADDRESS_UPDATE;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS_UPDATED;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.TRANSACTION_HASH_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockcypherAddressTransactionsProviderTest {
    private static final TransactionsRequestKey REQUEST_KEY_WITH_TRANSACTIONS =
            new TransactionsRequestKey(
                    ADDRESS_TRANSACTIONS,
                    ADDRESS_TRANSACTIONS_UPDATED.getLastCheckedAtBlockHeight()
            );

    @InjectMocks
    private BlockcypherAddressTransactionsProvider provider;

    @Mock
    private BlockcypherClient blockcypherClient;

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("BlockcypherAddressTransactionsProvider");
    }

    @Test
    void getAddressDetails() {
        when(blockcypherClient.getAllAddressDetails(ADDRESS))
                .thenReturn(Optional.of(BLOCKCYPHER_ADDRESS_DETAILS));
        Optional<AddressTransactions> addressTransactions =
                provider.get(new TransactionsRequestKey(ADDRESS, LAST_CHECKED_AT_BLOCK_HEIGHT));
        assertThat(addressTransactions).contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getAddressDetails_combines_when_update_has_many_transactions() {
        int beforeBlocksForSecondRequest = ADDRESS_DETAILS_INCOMPLETE.getLowestCompletedBlockHeight();
        when(blockcypherClient.getAllAddressDetails(ADDRESS))
                .thenReturn(Optional.of(ADDRESS_DETAILS_INCOMPLETE));
        when(blockcypherClient.getAddressDetailsBefore(ADDRESS, beforeBlocksForSecondRequest))
                .thenReturn(Optional.of(ADDRESS_DETAILS_SECOND_PART));

        Optional<AddressTransactions> addressTransactions =
                provider.get(new TransactionsRequestKey(ADDRESS, LAST_CHECKED_AT_BLOCK_HEIGHT));

        assertThat(addressTransactions).contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getUpdates() {
        when(blockcypherClient.getAddressDetailsAfter(ADDRESS, LAST_CHECKED_AT_BLOCK_HEIGHT + 1))
                .thenReturn(Optional.of(BLOCKCYPHER_ADDRESS_UPDATE));
        Optional<AddressTransactions> updated = provider.get(REQUEST_KEY_WITH_TRANSACTIONS);
        assertThat(updated).contains(ADDRESS_TRANSACTIONS_UPDATED);
    }

    @Test
    void getUpdates_no_update_returned() {
        when(blockcypherClient.getAddressDetailsAfter(ADDRESS, LAST_CHECKED_AT_BLOCK_HEIGHT + 1))
                .thenReturn(Optional.empty());

        Optional<AddressTransactions> updated = provider.get(REQUEST_KEY_WITH_TRANSACTIONS);

        assertThat(updated).contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void update_pieces_need_to_be_combined() {
        BlockcypherAddressTransactionsDto incompleteUpdate = new BlockcypherAddressTransactionsDto(
                ADDRESS,
                Set.of(TRANSACTION_HASH_3),
                true,
                LAST_CHECKED_AT_BLOCK_HEIGHT
        );

        BlockcypherAddressTransactionsDto completedUpdate = new BlockcypherAddressTransactionsDto(
                ADDRESS,
                Set.of(TRANSACTION_HASH_4),
                false,
                LAST_CHECKED_AT_BLOCK_HEIGHT - 1
        );

        int afterHighestKnownBlockHeight = LAST_CHECKED_AT_BLOCK_HEIGHT + 1;
        when(blockcypherClient.getAddressDetailsAfter(ADDRESS, afterHighestKnownBlockHeight))
                .thenReturn(Optional.of(incompleteUpdate));
        when(blockcypherClient.getAddressDetailsBetween(
                ADDRESS, afterHighestKnownBlockHeight, LAST_CHECKED_AT_BLOCK_HEIGHT
        )).thenReturn(Optional.of(completedUpdate));

        int updateBlockHeight = ADDRESS_TRANSACTIONS_UPDATED.getLastCheckedAtBlockHeight();
        Optional<AddressTransactions> updated =
                provider.get(new TransactionsRequestKey(ADDRESS_TRANSACTIONS, updateBlockHeight));

        assertThat(updated).contains(ADDRESS_TRANSACTIONS_UPDATED);
    }

}