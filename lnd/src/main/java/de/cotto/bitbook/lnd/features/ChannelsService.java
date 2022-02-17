package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.lnd.model.Channel;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.springframework.stereotype.Component;

import java.util.Set;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.ownership.OwnershipStatus.OWNED;

@Component
public class ChannelsService {
    public static final String ADDRESS_DESCRIPTION_PREFIX = "Lightning-Channel with ";

    private final AddressDescriptionService addressDescriptionService;
    private final TransactionDescriptionService transactionDescriptionService;
    private final AddressOwnershipService addressOwnershipService;

    public ChannelsService(
            AddressDescriptionService addressDescriptionService,
            TransactionDescriptionService transactionDescriptionService,
            AddressOwnershipService addressOwnershipService
    ) {
        this.addressDescriptionService = addressDescriptionService;
        this.transactionDescriptionService = transactionDescriptionService;
        this.addressOwnershipService = addressOwnershipService;
    }

    public long addFromChannels(Set<Channel> channels) {
        for (Channel channel : channels) {
            setTransactionDescription(channel);
            setAddressDescription(channel);
            setChannelOwnership(channel);
        }
        return channels.size();
    }

    private void setTransactionDescription(Channel channel) {
        String localOrRemote;
        if (channel.isInitiator()) {
            localOrRemote = "local";
        } else {
            localOrRemote = "remote";
        }
        transactionDescriptionService.set(
                channel.getOpeningTransaction().getHash(),
                "Opening Channel with %s (%s)".formatted(channel.getRemotePubkey(), localOrRemote)
        );
    }

    private void setAddressDescription(Channel channel) {
        addressDescriptionService.set(
                channel.getChannelAddress(),
                ADDRESS_DESCRIPTION_PREFIX + channel.getRemotePubkey()
        );
    }

    private void setChannelOwnership(Channel channel) {
        Address channelAddress = channel.getChannelAddress();
        if (channel.isInitiator()) {
            addressOwnershipService.setAddressAsOwned(channelAddress, BTC);
        }
        if (!OWNED.equals(addressOwnershipService.getOwnershipStatus(channelAddress))) {
            addressOwnershipService.setAddressAsForeign(channelAddress);
        }
    }
}
