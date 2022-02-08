package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.price.PriceService;
import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.transaction.BalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.TransactionFixtures.ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressCommandsTest {
    @InjectMocks
    private AddressCommands addressCommands;

    @Mock
    private BalanceService balanceService;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Mock
    private PriceService priceService;

    @Mock
    private PriceFormatter priceFormatter;

    @Mock
    private SelectedChain selectedChain;

    @BeforeEach
    void setUp() {
        lenient().when(selectedChain.getChain()).thenReturn(BTC);
    }

    @Test
    void getBalanceForAddress() {
        Coins expectedCoins = Coins.ofSatoshis(456);
        Price price = Price.of(200);
        when(priceService.getCurrentPrice(BTC)).thenReturn(price);
        when(priceFormatter.format(expectedCoins, price)).thenReturn("formattedPrice");
        when(balanceService.getBalance(ADDRESS)).thenReturn(expectedCoins);
        String currentBalanceForAddress = addressCommands.getBalanceForAddress(new CliAddress(ADDRESS));
        assertThat(currentBalanceForAddress).isEqualTo(expectedCoins + " [formattedPrice]");
    }

    @Test
    void getBalanceForAddress_wrong_address() {
        String currentBalanceForAddress = addressCommands.getBalanceForAddress(new CliAddress("foo"));
        assertThat(currentBalanceForAddress).isEqualTo(CliAddress.ERROR_MESSAGE);
    }

    @Test
    void setAddressDescription() {
        assertThat(addressCommands.setAddressDescription(new CliAddress(ADDRESS), "b")).isEqualTo("OK");
        verify(addressDescriptionService).set(ADDRESS, "b");
    }

    @Test
    void setAddressDescription_wrong_address() {
        assertThat(addressCommands.setAddressDescription(new CliAddress("a"), "b"))
                .isEqualTo(CliAddress.ERROR_MESSAGE);
        verifyNoInteractions(addressDescriptionService);
    }

    @Test
    void removeAddressDescription() {
        assertThat(addressCommands.removeAddressDescription(new CliAddress(ADDRESS))).isEqualTo("OK");
        verify(addressDescriptionService).remove(ADDRESS);
    }

    @Test
    void removeAddressDescription_wrong_address() {
        assertThat(addressCommands.removeAddressDescription(new CliAddress("a")))
                .isEqualTo(CliAddress.ERROR_MESSAGE);
        verifyNoInteractions(addressDescriptionService);
    }
}