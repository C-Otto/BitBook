package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.transaction.TransactionDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;

import java.util.List;
import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionHashCompletionProviderTest {
    @InjectMocks
    private TransactionHashCompletionProvider completionProvider;

    @Mock
    private TransactionDao transactionDao;

    private final String[] hints = new String[0];
    @Mock
    private CompletionContext context;
    @Mock
    private MethodParameter methodParameter;

    @Test
    void complete() {
        when(context.currentWordUpToCursor()).thenReturn("abc");
        when(transactionDao.getTransactionHashesStartingWith("abc")).thenReturn(Set.of(TRANSACTION_HASH));

        List<CompletionProposal> complete = completionProvider.complete(methodParameter, context, hints);

        assertThat(complete).usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new CompletionProposal(TRANSACTION_HASH));
    }

    @Test
    void does_not_complete_short_hash() {
        when(context.currentWordUpToCursor()).thenReturn("ab");
        assertThat(completionProvider.complete(methodParameter, context, hints)).isEmpty();
        verifyNoInteractions(transactionDao);
    }
}