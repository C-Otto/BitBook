package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.transaction.TransactionCompletionDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionHashCompletionProviderTest {
    private static final String PREFIX = "abc";

    @InjectMocks
    private TransactionHashCompletionProvider completionProvider;

    @Mock
    private TransactionCompletionDao transactionCompletionDao;

    private final String[] hints = new String[0];

    @Mock
    private CompletionContext context;

    @Mock
    private MethodParameter methodParameter;

    @BeforeEach
    void setUp() {
        when(context.currentWordUpToCursor()).thenReturn(PREFIX);
    }

    @Test
    void complete() {
        when(transactionCompletionDao.completeFromTransactionDetails(PREFIX))
                .thenReturn(Set.of(TRANSACTION_HASH));
        when(transactionCompletionDao.completeFromAddressTransactionHashes(PREFIX))
                .thenReturn(Set.of(TRANSACTION_HASH_2));

        assertProposalsForHashes(TRANSACTION_HASH, TRANSACTION_HASH_2);
    }

    @Test
    void complete_no_duplicates() {
        when(transactionCompletionDao.completeFromTransactionDetails(PREFIX))
                .thenReturn(Set.of(TRANSACTION_HASH));
        when(transactionCompletionDao.completeFromAddressTransactionHashes(PREFIX))
                .thenReturn(Set.of(TRANSACTION_HASH));

        assertProposalsForHashes(TRANSACTION_HASH);
    }

    @Test
    void complete_from_transaction_details() {
        when(transactionCompletionDao.completeFromTransactionDetails(PREFIX)).thenReturn(Set.of(TRANSACTION_HASH));

        assertProposalsForHashes(TRANSACTION_HASH);
    }

    @Test
    void complete_from_address_transaction_hashes() {
        when(context.currentWordUpToCursor()).thenReturn(PREFIX);
        when(transactionCompletionDao.completeFromAddressTransactionHashes(PREFIX))
                .thenReturn(Set.of(TRANSACTION_HASH));

        assertProposalsForHashes(TRANSACTION_HASH);
    }

    @Test
    void does_not_complete_short_hash() {
        when(context.currentWordUpToCursor()).thenReturn("ab");
        assertThat(completionProvider.complete(methodParameter, context, hints)).isEmpty();
        verifyNoInteractions(transactionCompletionDao);
    }

    private void assertProposalsForHashes(String... transactionHashes) {
        List<CompletionProposal> proposals = completionProvider.complete(methodParameter, context, hints);
        List<CompletionProposal> completionProposals =
                Arrays.stream(transactionHashes).map(CompletionProposal::new).collect(Collectors.toList());
        assertThat(proposals).usingRecursiveFieldByFieldElementComparator()
                .isEqualTo(completionProposals);
    }
}