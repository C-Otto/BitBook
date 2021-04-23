package de.cotto.bitbook.ownership.cli;

import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.price.PriceService;
import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.transaction.BalanceService;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.cli.CliAddress;
import de.cotto.bitbook.cli.PriceFormatter;
import de.cotto.bitbook.cli.TransactionFormatter;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
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
    private TransactionFormatter transactionFormatter;

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

    @Nested
    class GetNeighbourTransactions {
        @BeforeEach
        void setUp() {
            when(transactionFormatter.formatSingleLineForValue(any(), any()))
                    .then(invocation -> invocation.getArgument(0) + "/" + invocation.getArgument(1));
        }

        @Test
        void uses_formatter() {
            Coins coins1 = Coins.ofSatoshis(1);
            Coins coins2 = Coins.ofSatoshis(2);

            when(addressOwnershipService.getNeighbourTransactions()).thenReturn(Map.of(
                    TRANSACTION, coins1,
                    TRANSACTION_2, coins2
            ));
            assertThat(ownershipCommands.getNeighbourTransactions())
                    .isEqualTo(TRANSACTION + "/" + coins1 + "\n" + TRANSACTION_2 + "/" + coins2);
        }

        @Test
        void sorted_by_hash() {
            Coins coins = Coins.ofSatoshis(1);
            when(addressOwnershipService.getNeighbourTransactions()).thenReturn(Map.of(
                    TRANSACTION, coins,
                    TRANSACTION_2, coins,
                    TRANSACTION_3, coins
            ));
            assertThat(ownershipCommands.getNeighbourTransactions()).isEqualTo(
                    TRANSACTION_3 + "/" + coins + "\n"
                    + TRANSACTION_2 + "/" + coins + "\n"
                    + TRANSACTION + "/" + coins
            );
        }

        @Test
        void does_not_show_zero_values() {
            when(addressOwnershipService.getNeighbourTransactions()).thenReturn(Map.of(
                    TRANSACTION, Coins.NONE,
                    TRANSACTION_2, Coins.ofSatoshis(1)
            ));
            assertThat(ownershipCommands.getNeighbourTransactions())
                    .isEqualTo(TRANSACTION_2 + "/" + Coins.ofSatoshis(1));
        }

        @Test
        void preloads_prices() {
            when(addressOwnershipService.getNeighbourTransactions()).thenReturn(Map.of(
                    TRANSACTION, Coins.NONE,
                    TRANSACTION_2, Coins.ofSatoshis(1)
            ));
            ownershipCommands.getNeighbourTransactions();

            InOrder inOrder = inOrder(priceService, transactionFormatter);
            inOrder.verify(priceService).getPrices(Set.of(TRANSACTION.getTime(), TRANSACTION_2.getTime()));
            inOrder.verify(transactionFormatter).formatSingleLineForValue(any(), any());
        }
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