package de.cotto.bitbook.ownership;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.transaction.AddressTransactionsService;
import de.cotto.bitbook.backend.transaction.BalanceService;
import de.cotto.bitbook.backend.transaction.TransactionDao;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.ownership.persistence.AddressOwnershipDaoImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.Chain.BTG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DataJpaTest
@Import({AddressOwnershipDaoImpl.class, AddressOwnershipService.class})
class AddressOwnershipServiceIT {
    @Autowired
    private AddressOwnershipService addressOwnershipService;

    @MockBean
    @SuppressWarnings("unused")
    private BalanceService balanceService;

    @MockBean
    @SuppressWarnings("unused")
    private TransactionDao transactionDao;

    @MockBean
    private AddressDescriptionService addressDescriptionService;

    @MockBean
    private AddressTransactionsService addressTransactionsService;

    @MockBean
    @SuppressWarnings("unused")
    private TransactionService transactionService;

    @Test
    void markAsOwnedAndGet() {
        Address address = new Address("abc");
        addressOwnershipService.setAddressAsOwned(address, BTG, "");
        assertThat(addressOwnershipService.getOwnedAddresses()).containsExactly(address);
        verify(addressTransactionsService).requestTransactionsInBackground(address, BTG);
        verify(addressTransactionsService, never()).requestTransactionsInBackground(address, BTC);
        verify(addressDescriptionService).set(address, "");
    }

}