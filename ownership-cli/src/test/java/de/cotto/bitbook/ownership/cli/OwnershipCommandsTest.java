package de.cotto.bitbook.ownership.cli;

import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.model.TransactionWithDescription;
import de.cotto.bitbook.backend.price.PriceService;
import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.transaction.BalanceService;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.cli.CliAddress;
import de.cotto.bitbook.cli.PriceFormatter;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_3;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnershipCommandsTest {
    private static final String INVALID_ADDRESS = "-";

    @InjectMocks
    private OwnershipCommands ownershipCommands;

    @Mock
    private AddressOwnershipService addressOwnershipService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private PriceService priceService;

    @Mock
    private PriceFormatter priceFormatter;

    @Mock
    private TransactionDescriptionService transactionDescriptionService;

    @Test
    void getBalance() {
        Coins coins = Coins.ofSatoshis(123);
        Price price = Price.of(456);
        when(priceService.getCurrentPrice()).thenReturn(price);
        when(priceFormatter.format(coins, price)).thenReturn("formattedPrice");
        String expected = coins + " [formattedPrice]";
        when(addressOwnershipService.getBalance()).thenReturn(coins);
        assertThat(ownershipCommands.getBalance()).isEqualTo(expected);
    }

    @Test
    void listOwnedAddresses_with_balance() {
        String address1 = "abc";
        String address2 = "def";
        when(balanceService.getBalance(address1)).thenReturn(Coins.ofSatoshis(123));
        when(balanceService.getBalance(address2)).thenReturn(Coins.ofSatoshis(234));
        when(addressOwnershipService.getOwnedAddressesWithDescription()).thenReturn(Set.of(
                new AddressWithDescription(address1),
                new AddressWithDescription(address2)
        ));
        assertThat(ownershipCommands.listOwnedAddresses()).matches(
                ".*" + address1 + ".*123.*\n" +
                ".*" + address2 + ".*234.*"
        );
    }

    @Test
    void listOwnedAddresses_sorted_by_value() {
        AddressWithDescription address1 = new AddressWithDescription("xxx", "b-DESCRIPTION");
        AddressWithDescription address2 = new AddressWithDescription("yyy", "a-DESCRIPTION");
        AddressWithDescription address3 = new AddressWithDescription("zzz", "c-DESCRIPTION");
        when(balanceService.getBalance(address1.getAddress())).thenReturn(Coins.ofSatoshis(123));
        when(balanceService.getBalance(address2.getAddress())).thenReturn(Coins.ofSatoshis(100));
        when(balanceService.getBalance(address3.getAddress())).thenReturn(Coins.ofSatoshis(1000));
        Set<AddressWithDescription> addresses = Set.of(address1, address2, address3);
        when(addressOwnershipService.getOwnedAddressesWithDescription()).thenReturn(addresses);
        assertThat(ownershipCommands.listOwnedAddresses())
                .matches(".*a-DESCRIPTION.*\n.*b-DESCRIPTION.*\n.*c-DESCRIPTION.*");
    }

    @Test
    void getNeighbourTransactions_with_price() {
        Coins coins1 = Coins.ofSatoshis(100);
        Coins coins2 = Coins.ofSatoshis(123);
        Price price1 = Price.of(200);
        Price price2 = Price.of(50);
        when(priceService.getPrice(TRANSACTION.getTime())).thenReturn(price1);
        when(priceService.getPrice(TRANSACTION_2.getTime())).thenReturn(price2);
        when(priceFormatter.format(coins1, price1)).thenReturn("p1");
        when(priceFormatter.format(coins2, price2)).thenReturn("p2");
        when(transactionDescriptionService.get(any()))
                .then(invocation -> new TransactionWithDescription(invocation.getArgument(0)));
        when(addressOwnershipService.getNeighbourTransactions()).thenReturn(Map.of(
                TRANSACTION, coins1,
                TRANSACTION_2, coins2
        ));
        String line1 = TRANSACTION.getHash() + ": " + coins1 + " [p1]";
        String line2 = TRANSACTION_2.getHash() + ": " + coins2 + " [p2]";
        assertThat(ownershipCommands.getNeighbourTransactions())
                .isEqualTo(String.join("\n", List.of(line1, line2)));
    }

    @Test
    void getNeighbourTransactions_with_description() {
        TransactionWithDescription transactionWithDescription = new TransactionWithDescription(TRANSACTION_HASH, "foo");
        TransactionWithDescription transactionWithoutDescription = new TransactionWithDescription(TRANSACTION_HASH_2);
        when(transactionDescriptionService.get(TRANSACTION_HASH)).thenReturn(transactionWithDescription);
        when(transactionDescriptionService.get(TRANSACTION_HASH_2)).thenReturn(transactionWithoutDescription);
        String formattedDescription = transactionWithDescription.getFormattedDescription();

        Coins coins = Coins.ofSatoshis(1);
        when(priceService.getPrice(any(LocalDateTime.class))).thenReturn(Price.of(1));
        when(priceFormatter.format(any(), any())).thenReturn("p");
        when(addressOwnershipService.getNeighbourTransactions()).thenReturn(Map.of(
                TRANSACTION, coins,
                TRANSACTION_2, coins
        ));
        String line1 = TRANSACTION_2.getHash() + ": " + coins + " [p]";
        String line2 = TRANSACTION.getHash() + ": " + coins + " [p] " + formattedDescription;
        assertThat(ownershipCommands.getNeighbourTransactions())
                .isEqualTo(String.join("\n", List.of(line1, line2)));
    }

    @Test
    void getNeighbourTransactions_sorted_by_hash() {
        when(transactionDescriptionService.get(any()))
                .then(invocation -> new TransactionWithDescription(invocation.getArgument(0)));
        when(addressOwnershipService.getNeighbourTransactions()).thenReturn(Map.of(
                TRANSACTION, Coins.ofSatoshis(1),
                TRANSACTION_2, Coins.ofSatoshis(1),
                TRANSACTION_3, Coins.ofSatoshis(1)
        ));
        assertThat(ownershipCommands.getNeighbourTransactions())
                .matches(TRANSACTION_HASH_3 + ".*\n" + TRANSACTION_HASH_2 + ".*\n" + TRANSACTION_HASH + ".*");
    }

    @Test
    void getNeighbourTransactions_does_not_show_zero_values() {
        when(transactionDescriptionService.get(any()))
                .then(invocation -> new TransactionWithDescription(invocation.getArgument(0)));
        when(addressOwnershipService.getNeighbourTransactions()).thenReturn(Map.of(
                TRANSACTION, Coins.NONE,
                TRANSACTION_2, Coins.ofSatoshis(1)
        ));
        assertThat(ownershipCommands.getNeighbourTransactions())
                .isEqualTo(TRANSACTION_2.getHash() + ": " + Coins.ofSatoshis(1) + " [null]");
    }

    @Test
    void markAddressAsOwned() {
        String description = "description";
        assertThat(ownershipCommands.markAddressAsOwned(new CliAddress(ADDRESS), description)).isEqualTo("OK");
        verify(addressOwnershipService).setAddressAsOwned(ADDRESS, description);
    }

    @Test
    void markAddressAsOwned_wrong_address() {
        assertThat(ownershipCommands.markAddressAsOwned(new CliAddress(INVALID_ADDRESS), ""))
                .isEqualTo(CliAddress.ERROR_MESSAGE);
        verifyNoInteractions(addressOwnershipService);
    }

    @Test
    void markAddressAsForeign() {
        String description = "description";
        assertThat(ownershipCommands.markAddressAsForeign(new CliAddress(ADDRESS), description)).isEqualTo("OK");
        verify(addressOwnershipService).setAddressAsForeign(ADDRESS, description);
    }

    @Test
    void markAddressAsForeign_wrong_address() {
        assertThat(ownershipCommands.markAddressAsForeign(new CliAddress(INVALID_ADDRESS), ""))
                .isEqualTo(CliAddress.ERROR_MESSAGE);
        verifyNoInteractions(addressOwnershipService);
    }

    @Test
    void resetOwnership() {
        assertThat(ownershipCommands.resetOwnership(new CliAddress(ADDRESS))).isEqualTo("OK");
        verify(addressOwnershipService).resetOwnership(ADDRESS);
    }

    @Test
    void resetOwnership_wrong_address() {
        assertThat(ownershipCommands.resetOwnership(new CliAddress(INVALID_ADDRESS)))
                .isEqualTo(CliAddress.ERROR_MESSAGE);
        verifyNoInteractions(addressOwnershipService);
    }
}