package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.lnd.model.ClosedChannel;
import de.cotto.bitbook.lnd.model.Initiator;
import de.cotto.bitbook.lnd.model.Resolution;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import de.cotto.bitbook.ownership.OwnershipStatus;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

import static de.cotto.bitbook.ownership.OwnershipStatus.OWNED;

@Component
public class ClosedChannelsService {
    private static final String DEFAULT_ADDRESS_DESCRIPTION = "lnd";

    private final TransactionDescriptionService transactionDescriptionService;
    private final AddressDescriptionService addressDescriptionService;
    private final AddressOwnershipService addressOwnershipService;
    private final SweepTransactionsService sweepTransactionsService;

    public ClosedChannelsService(
            TransactionDescriptionService transactionDescriptionService,
            AddressDescriptionService addressDescriptionService,
            AddressOwnershipService addressOwnershipService,
            SweepTransactionsService sweepTransactionsService
    ) {
        this.transactionDescriptionService = transactionDescriptionService;
        this.addressDescriptionService = addressDescriptionService;
        this.addressOwnershipService = addressOwnershipService;
        this.sweepTransactionsService = sweepTransactionsService;
    }

    public long addFromClosedChannels(Set<ClosedChannel> closedChannels) {
        Set<ClosedChannel> validClosedChannels = closedChannels.stream()
                .filter(ClosedChannel::isValid)
                .collect(Collectors.toSet());
        validClosedChannels.forEach(this::addFromClosedChannel);
        return validClosedChannels.size();
    }

    private void addFromClosedChannel(ClosedChannel closedChannel) {
        Address channelAddress = closedChannel.getChannelAddress();
        String remotePubkey = closedChannel.getRemotePubkey();

        setTransactionDescriptions(closedChannel);
        setForSettlementAddress(closedChannel);
        setOtherOutputAsForeignForCooperativeClose(closedChannel);
        setChannelAddressOwnershipAndDescription(channelAddress, closedChannel.getOpenInitiator(), remotePubkey);
        addFromHtlcSweepTransactions(closedChannel);
    }

    private void setTransactionDescriptions(ClosedChannel closedChannel) {
        String remotePubkey = closedChannel.getRemotePubkey();
        transactionDescriptionService.set(
                closedChannel.getOpeningTransaction().getHash(),
                "Opening Channel with %s (%s)".formatted(remotePubkey, closedChannel.getOpenInitiator())
        );
        transactionDescriptionService.set(
                closedChannel.getClosingTransaction().getHash(),
                "Closing Channel with %s (%s)".formatted(remotePubkey, closedChannel.getCloseType())
        );
    }

    private void setForSettlementAddress(ClosedChannel closedChannel) {
        closedChannel.getSettlementAddress().ifPresent(address -> {
            addressOwnershipService.setAddressAsOwned(address);
            addressDescriptionService.set(address, DEFAULT_ADDRESS_DESCRIPTION);
        });
    }

    private void setOtherOutputAsForeignForCooperativeClose(ClosedChannel closedChannel) {
        if (closedChannel.getCloseType().isCooperative()) {
            closedChannel.getClosingTransaction().getOutputAddresses().stream()
                    .filter(address -> !OWNED.equals(addressOwnershipService.getOwnershipStatus(address)))
                    .forEach(addressOwnershipService::setAddressAsForeign);
        }
    }

    private void setChannelAddressOwnershipAndDescription(
            Address channelAddress,
            Initiator openInitiator,
            String remotePubkey
    ) {
        setChannelAddressDescription(channelAddress, remotePubkey);
        setChannelAddressOwnership(channelAddress, openInitiator);
    }

    private void setChannelAddressDescription(Address channelAddress, String remotePubkey) {
        addressDescriptionService.set(channelAddress, "Lightning-Channel with " + remotePubkey);
    }

    private void setChannelAddressOwnership(Address channelAddress, Initiator openInitiator) {
        if (openInitiator.equals(Initiator.LOCAL)) {
            addressOwnershipService.setAddressAsOwned(channelAddress);
        } else if (openInitiator.equals(Initiator.REMOTE)) {
            OwnershipStatus ownershipStatus = addressOwnershipService.getOwnershipStatus(channelAddress);
            if (!OWNED.equals(ownershipStatus)) {
                addressOwnershipService.setAddressAsForeign(channelAddress);
            }
        }
    }

    private void addFromHtlcSweepTransactions(ClosedChannel closedChannel) {
        Set<TransactionHash> sweepTransactionHashes = closedChannel.getResolutions().stream()
                .filter(Resolution::sweepTransactionClaimsFunds)
                .map(Resolution::sweepTransactionHash)
                .filter(TransactionHash::isValid)
                .collect(Collectors.toSet());
        sweepTransactionsService.addFromSweeps(sweepTransactionHashes);
    }
}
