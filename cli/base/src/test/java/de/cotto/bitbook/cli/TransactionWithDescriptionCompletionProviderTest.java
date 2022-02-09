package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.TransactionWithDescription;
import de.cotto.bitbook.backend.transaction.TransactionCompletionDao;
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

import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionWithDescriptionCompletionProviderTest {
    private final String[] hints = new String[0];

    @InjectMocks
    private TransactionWithDescriptionCompletionProvider completionProvider;

    @Mock
    private TransactionCompletionDao transactionCompletionDao;

    @Mock
    private TransactionDescriptionService transactionDescriptionService;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private CompletionContext context;

    @Test
    void complete_transaction_only_with_description() {
        String input = "abc";
        String description1 = "my foo bar";
        String description3 = "blub";
        TransactionWithDescription transactionWithDescription1 =
                new TransactionWithDescription(TRANSACTION_HASH, description1);
        TransactionWithDescription transactionWithDescription2 =
                new TransactionWithDescription(TRANSACTION_HASH_2);
        TransactionWithDescription transactionWithDescription3 =
                new TransactionWithDescription(TRANSACTION_HASH_3, description3);
        when(context.currentWordUpToCursor()).thenReturn(input);
        when(transactionDescriptionService.get(TRANSACTION_HASH)).thenReturn(transactionWithDescription1);
        when(transactionDescriptionService.get(TRANSACTION_HASH_2)).thenReturn(transactionWithDescription2);
        when(transactionDescriptionService.get(TRANSACTION_HASH_3)).thenReturn(transactionWithDescription3);
        when(transactionCompletionDao.completeFromTransactionDetails(input))
                .thenReturn(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2, TRANSACTION_HASH_3));

        List<CompletionProposal> complete = completionProvider.complete(methodParameter, context, hints);

        assertThat(complete).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new CompletionProposal(TRANSACTION_HASH_3.toString()).description(description3),
                new CompletionProposal(TRANSACTION_HASH.toString()).description(description1)
        );
    }
}