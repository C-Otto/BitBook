package de.cotto.bitbook.backend.transaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionUpdateHeuristicsTest {
    private static final int LIMIT = 48;
    @InjectMocks
    private TransactionUpdateHeuristics transactionUpdateHeuristics;

    @Mock
    private BlockHeightService blockHeightService;

    @Test
    void recent_enough() {
        when(blockHeightService.getBlockHeight())
                .thenReturn(ADDRESS_TRANSACTIONS.getLastCheckedAtBlockHeight() + LIMIT);
        assertThat(transactionUpdateHeuristics.isRecentEnough(ADDRESS_TRANSACTIONS)).isTrue();
    }

    @Test
    void too_old() {
        when(blockHeightService.getBlockHeight())
                .thenReturn(ADDRESS_TRANSACTIONS.getLastCheckedAtBlockHeight() + LIMIT + 1);
        assertThat(transactionUpdateHeuristics.isRecentEnough(ADDRESS_TRANSACTIONS)).isFalse();
    }
}