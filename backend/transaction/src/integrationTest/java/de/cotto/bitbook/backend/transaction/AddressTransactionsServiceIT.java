package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.AddressTransactions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class AddressTransactionsServiceIT {
    @Autowired
    private AddressTransactionsService addressTransactionsService;

    @MockBean
    private AddressTransactionsProvider transactionAddressClient;

    @MockBean
    private PrioritizingBlockHeightProvider blockHeightProvider;

    @BeforeEach
    void setUp() {
        when(transactionAddressClient.isSupported(any())).thenReturn(true);
    }

    @Test
    void getAddressTransactions() {
        when(blockHeightProvider.getBlockHeight(BTC)).thenReturn(LAST_CHECKED_AT_BLOCK_HEIGHT);
        TransactionsRequestKey request = new TransactionsRequestKey(ADDRESS, BTC, LAST_CHECKED_AT_BLOCK_HEIGHT);
        when(transactionAddressClient.get(request)).thenReturn(Optional.of(ADDRESS_TRANSACTIONS));

        AddressTransactions addressTransactions = addressTransactionsService.getTransactions(ADDRESS, BTC);

        assertThat(addressTransactions).isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getAddressTransactions_from_persistence() {
        TransactionsRequestKey request = new TransactionsRequestKey(ADDRESS, BTC, LAST_CHECKED_AT_BLOCK_HEIGHT);
        when(transactionAddressClient.get(request)).thenReturn(Optional.of(ADDRESS_TRANSACTIONS));

        AddressTransactions addressTransactions1 = addressTransactionsService.getTransactions(ADDRESS, BTC);
        AddressTransactions addressTransactions2 = addressTransactionsService.getTransactions(ADDRESS, BTC);

        assertThat(addressTransactions1).isEqualTo(addressTransactions2);
        Optional<AddressTransactions> spotbugsWorkaround = verify(transactionAddressClient, atMostOnce()).get(any());
        assertThat(spotbugsWorkaround).isNull();
    }

    @Test
    void requestTransactionsInBackground() {
        TransactionsRequestKey request = new TransactionsRequestKey(ADDRESS, BTC, LAST_CHECKED_AT_BLOCK_HEIGHT);
        when(transactionAddressClient.get(request)).then(invocation -> {
            Thread.sleep(1_000);
            return Optional.empty();
        });
        await().atMost(200, TimeUnit.MILLISECONDS).until(() -> {
            addressTransactionsService.requestTransactionsInBackground(ADDRESS, BTC);
            return true;
        });
    }
}
