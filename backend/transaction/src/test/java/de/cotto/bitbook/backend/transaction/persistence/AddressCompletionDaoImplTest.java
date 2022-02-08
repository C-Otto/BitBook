package de.cotto.bitbook.backend.transaction.persistence;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.transaction.AddressTransactionsDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.persistence.InputJpaDtoFixtures.INPUT_JPA_DTO_1;
import static de.cotto.bitbook.backend.transaction.persistence.OutputJpaDtoFixtures.OUTPUT_JPA_DTO_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressCompletionDaoImplTest {
    private static final String PREFIX = "prefix";

    @InjectMocks
    private AddressCompletionDaoImpl addressCompletionDao;

    @Mock
    private AddressTransactionsDao addressTransactionsDao;

    @Mock
    private InputRepository inputRepository;

    @Mock
    private OutputRepository outputRepository;

    @Test
    void completeFromAddressTransactions() {
        when(addressTransactionsDao.getAddressesStartingWith(PREFIX)).thenReturn(Set.of(ADDRESS, ADDRESS_2));
        assertThat(addressCompletionDao.completeFromAddressTransactions(PREFIX))
                .containsExactlyInAnyOrder(ADDRESS, ADDRESS_2);
    }

    @Test
    void completeFromInputsAndOutputs() {
        when(inputRepository.findBySourceAddressStartingWith(PREFIX)).thenReturn(Set.of(INPUT_JPA_DTO_1));
        when(outputRepository.findByTargetAddressStartingWith(PREFIX)).thenReturn(Set.of(OUTPUT_JPA_DTO_1));
        assertThat(addressCompletionDao.completeFromInputsAndOutputs(PREFIX)).containsExactlyInAnyOrder(
                        new Address(INPUT_JPA_DTO_1.getAddress()),
                        new Address(OUTPUT_JPA_DTO_1.getAddress())
        );
    }
}