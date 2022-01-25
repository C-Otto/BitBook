package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.lnd.model.CloseType;
import de.cotto.bitbook.lnd.model.ClosedChannel;
import de.cotto.bitbook.lnd.model.Initiator;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import de.cotto.bitbook.ownership.OwnershipStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.AMBIGUOUS_SETTLEMENT_ADDRESS;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.SWEEP_TRANSACTION_HASH;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.WITH_RESOLUTION;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.WITH_RESOLUTION_BLANK_HASH;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.WITH_RESOLUTION_CLAIMED_OUTGOING_HTLC;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.WITH_RESOLUTION_TIMEOUT_INCOMING_HTLC;
import static de.cotto.bitbook.lnd.model.Initiator.LOCAL;
import static de.cotto.bitbook.lnd.model.Initiator.REMOTE;
import static de.cotto.bitbook.ownership.OwnershipStatus.OWNED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClosedChannelsServiceTest {
    private static final String DEFAULT_DESCRIPTION = "lnd";

    @InjectMocks
    private ClosedChannelsService closedChannelsService;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Mock
    private TransactionDescriptionService transactionDescriptionService;

    @Mock
    private AddressOwnershipService addressOwnershipService;

    @Mock
    private SweepTransactionsService sweepTransactionsService;

    @Test
    void no_channel() {
        assertThat(closedChannelsService.addFromClosedChannels(Set.of())).isEqualTo(0);
    }

    @Test
    void two_channels() {
        Set<ClosedChannel> channels = Set.of(CLOSED_CHANNEL, AMBIGUOUS_SETTLEMENT_ADDRESS);
        assertThat(closedChannelsService.addFromClosedChannels(channels)).isEqualTo(2);
    }

    @Test
    void marks_channel_address_as_foreign_for_initiator_remote() {
        load(CLOSED_CHANNEL.toBuilder().withOpenInitiator(REMOTE).build());
        verify(addressOwnershipService).setAddressAsForeign(CLOSED_CHANNEL.getChannelAddress());
    }

    @Test
    void does_not_change_channel_address_ownership_if_already_marked_as_owned() {
        when(addressOwnershipService.getOwnershipStatus(any())).thenReturn(OwnershipStatus.UNKNOWN);
        when(addressOwnershipService.getOwnershipStatus(CLOSED_CHANNEL.getChannelAddress())).thenReturn(OWNED);
        load(CLOSED_CHANNEL.toBuilder().withOpenInitiator(REMOTE).build());
        verify(addressOwnershipService, never()).setAddressAsForeign(CLOSED_CHANNEL.getChannelAddress());
    }

    @Test
    void marks_channel_address_as_owned_for_initiator_local() {
        load(CLOSED_CHANNEL.toBuilder().withOpenInitiator(LOCAL).build());
        verify(addressOwnershipService).setAddressAsOwned(CLOSED_CHANNEL.getChannelAddress());
    }

    @Test
    void does_not_set_channel_ownership_for_initiator_unknown() {
        load(CLOSED_CHANNEL.toBuilder().withOpenInitiator(Initiator.UNKNOWN).build());
        verify(addressOwnershipService, never()).setAddressAsOwned(CLOSED_CHANNEL.getChannelAddress());
        verify(addressOwnershipService, never()).setAddressAsForeign(CLOSED_CHANNEL.getChannelAddress());
    }

    @Test
    void includes_pubkey_in_closing_transaction_description() {
        String remotePubKey = "foobar";
        ClosedChannel closedChannel = CLOSED_CHANNEL.toBuilder()
                .withCloseType(CloseType.COOPERATIVE_REMOTE)
                .withRemotePubkey(remotePubKey)
                .build();

        load(closedChannel);

        verify(transactionDescriptionService).set(
                CLOSED_CHANNEL.getClosingTransaction().getHash(),
                "Closing Channel with %s (cooperative remote)".formatted(remotePubKey)
        );
    }

    @Test
    void includes_type_in_closing_transaction_description() {
        load(CLOSED_CHANNEL.toBuilder().withCloseType(CloseType.COOPERATIVE).build());
        verify(transactionDescriptionService).set(
                CLOSED_CHANNEL.getClosingTransaction().getHash(),
                "Closing Channel with pubkey (cooperative)"
        );
    }

    @Test
    void sets_channel_address_description() {
        load(CLOSED_CHANNEL);
        String remotePubKey = CLOSED_CHANNEL.getRemotePubkey();
        verify(addressDescriptionService).set(
                CLOSED_CHANNEL.getChannelAddress(),
                "Lightning-Channel with %s".formatted(remotePubKey)
        );
    }

    @Test
    void sets_opening_transaction_description_with_initiator() {
        load(CLOSED_CHANNEL.toBuilder().withOpenInitiator(REMOTE).build());
        verify(transactionDescriptionService).set(
                CLOSED_CHANNEL.getOpeningTransaction().getHash(),
                "Opening Channel with pubkey (remote)"
        );
    }

    @Test
    void marks_settlement_address_as_owned() {
        load(CLOSED_CHANNEL);
        verify(addressOwnershipService).setAddressAsOwned(CLOSED_CHANNEL.getSettlementAddress().orElseThrow());
    }

    @Test
    void for_cooperative_close_marks_other_output_address_as_foreign() {
        load(CLOSED_CHANNEL);
        verify(addressOwnershipService).setAddressAsForeign(OUTPUT_ADDRESS_2);
    }

    @Test
    void for_force_close_does_not_mark_other_output_address_as_foreign() {
        load(CLOSED_CHANNEL.toBuilder().withCloseType(CloseType.FORCE_REMOTE).build());
        verify(addressOwnershipService, never()).setAddressAsForeign(OUTPUT_ADDRESS_2);
    }

    @Test
    void for_cooperative_close_does_not_mark_other_output_address_as_foreign_if_already_marked_as_owned() {
        when(addressOwnershipService.getOwnershipStatus(OUTPUT_ADDRESS_2)).thenReturn(OWNED);
        load(CLOSED_CHANNEL);
        verify(addressOwnershipService, never()).setAddressAsForeign(OUTPUT_ADDRESS_2);
    }

    @Test
    void adds_description_for_settlement_address() {
        load(CLOSED_CHANNEL);
        verify(addressDescriptionService).set(CLOSED_CHANNEL.getSettlementAddress().orElseThrow(), DEFAULT_DESCRIPTION);
    }

    @Test
    void does_not_add_description_for_ambiguous_settlement_address() {
        load(AMBIGUOUS_SETTLEMENT_ADDRESS);
        verify(addressDescriptionService, never()).set(any(), eq(DEFAULT_DESCRIPTION));
    }

    @Test
    void does_not_mark_settlement_address_as_owned_if_not_unique() {
        load(CLOSED_CHANNEL.toBuilder().withClosingTransaction(TRANSACTION).build());
        verify(addressOwnershipService, never()).setAddressAsOwned(any());
    }

    @Test
    void does_not_set_description_for_settled_balance_receive_address_if_not_unique() {
        load(CLOSED_CHANNEL.toBuilder().withClosingTransaction(TRANSACTION).build());
        verify(addressDescriptionService, never()).set(any(), any());
    }

    @Test
    void adds_from_sweep_transactions_in_htlc_resolutions() {
        load(WITH_RESOLUTION);
        verify(sweepTransactionsService).addFromSweeps(Set.of(SWEEP_TRANSACTION_HASH));
    }

    @Test
    void ignores_blank_sweep_transaction_in_htlc_resolutions() {
        load(WITH_RESOLUTION_BLANK_HASH);
        verify(sweepTransactionsService).addFromSweeps(Set.of());
    }

    @Test
    void ignores_htlc_resolutions_with_claimed_outgoing_htlc() {
        load(WITH_RESOLUTION_CLAIMED_OUTGOING_HTLC);
        verify(sweepTransactionsService).addFromSweeps(Set.of());
    }

    @Test
    void ignores_htlc_resolutions_with_timed_out_incoming_htlc() {
        load(WITH_RESOLUTION_TIMEOUT_INCOMING_HTLC);
        verify(sweepTransactionsService).addFromSweeps(Set.of());
    }

    private void load(ClosedChannel closedChannel) {
        closedChannelsService.addFromClosedChannels(Set.of(closedChannel));
    }
}
