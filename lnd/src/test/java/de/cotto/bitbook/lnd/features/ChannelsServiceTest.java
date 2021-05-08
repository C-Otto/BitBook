package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.bitbook.lnd.model.ChannelFixtures.CHANNEL;
import static de.cotto.bitbook.lnd.model.ChannelFixtures.CHANNEL_ADDRESS;
import static de.cotto.bitbook.lnd.model.ChannelFixtures.CHANNEL_LOCAL;
import static de.cotto.bitbook.ownership.OwnershipStatus.OWNED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChannelsServiceTest {
    @InjectMocks
    private ChannelsService channelsService;

    @Mock
    private TransactionDescriptionService transactionDescriptionService;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Mock
    private AddressOwnershipService addressOwnershipService;

    @Test
    void returns_number_of_channels() {
        assertThat(channelsService.addFromChannels(Set.of(CHANNEL))).isEqualTo(1);
    }

    @Test
    void sets_opening_transaction_description() {
        channelsService.addFromChannels(Set.of(CHANNEL));
        verify(transactionDescriptionService).set(
                CHANNEL.getOpeningTransaction().getHash(),
                "Opening Channel with %s (remote)".formatted(CHANNEL.getRemotePubkey())
        );
    }

    @Test
    void sets_channel_address_description() {
        channelsService.addFromChannels(Set.of(CHANNEL));
        verify(addressDescriptionService).set(
                CHANNEL.getChannelAddress(),
                "Lightning-Channel with %s".formatted(CHANNEL.getRemotePubkey())
        );
    }

    @Test
    void sets_ownership_to_owned_if_local() {
        channelsService.addFromChannels(Set.of(CHANNEL_LOCAL));
        verify(addressOwnershipService).setAddressAsOwned(CHANNEL_ADDRESS);
    }

    @Test
    void sets_ownership_to_foreign_if_remote_and_unknown_ownership() {
        channelsService.addFromChannels(Set.of(CHANNEL));
        verify(addressOwnershipService).setAddressAsForeign(CHANNEL_ADDRESS);
    }

    @Test
    void does_not_set_ownership_to_foreign_if_remote_and_already_owned() {
        when(addressOwnershipService.getOwnershipStatus(CHANNEL_ADDRESS)).thenReturn(OWNED);
        channelsService.addFromChannels(Set.of(CHANNEL));
        verify(addressOwnershipService, never()).setAddressAsForeign(CHANNEL_ADDRESS);
    }
}
