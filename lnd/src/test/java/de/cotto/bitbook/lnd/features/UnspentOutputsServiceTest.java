package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UnspentOutputsServiceTest {
    private static final String DEFAULT_DESCRIPTION = "lnd";

    @InjectMocks
    private UnspentOutputsService unspentOutputsService;

    @Mock
    private AddressOwnershipService addressOwnershipService;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Test
    void returnsNumberOfAddresses() {
        String address1 = "bc1qngw83";
        String address2 = "bc1aaaaaa";

        assertThat(unspentOutputsService.addFromUnspentOutputs(Set.of(address1, address2))).isEqualTo(2);
    }

    @Test
    void setsOwnership() {
        String address1 = "bc1qngw83";
        String address2 = "bc1aaaaaa";

        unspentOutputsService.addFromUnspentOutputs(Set.of(address1, address2));

        verify(addressOwnershipService).setAddressAsOwned(address1);
        verify(addressOwnershipService).setAddressAsOwned(address2);
    }

    @Test
    void setsDescriptions() {
        String address1 = "bc1qngw83";
        String address2 = "bc1aaaaaa";

        unspentOutputsService.addFromUnspentOutputs(Set.of(address1, address2));

        verify(addressDescriptionService).set(address1, DEFAULT_DESCRIPTION);
        verify(addressDescriptionService).set(address2, DEFAULT_DESCRIPTION);
    }
}
