package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Input;
import de.cotto.bitbook.backend.model.InputOutput;
import de.cotto.bitbook.backend.model.Output;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionWithDescription;
import de.cotto.bitbook.backend.price.PriceService;
import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_1;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_2;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_VALUE_1;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_VALUE_2;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_2;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_VALUE_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_VALUE_2;
import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionFormatterTest {
    @InjectMocks
    private TransactionFormatter transactionFormatter;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Mock
    private TransactionDescriptionService transactionDescriptionService;

    @Mock
    private AddressFormatter addressFormatter;

    @Mock
    private PriceService priceService;

    @Mock
    private PriceFormatter priceFormatter;

    @Mock
    private AddressOwnershipService addressOwnershipService;

    @Mock
    private SelectedChain selectedChain;

    @BeforeEach
    void setUp() {
        lenient().when(selectedChain.getChain()).thenReturn(BTC);
    }

    @Test
    void format() {
        when(priceFormatter.format(any(), any()))
                .then(invocation -> invocation.getArgument(0) + "/" + invocation.getArgument(1));
        testFormatForPrice(Price.of(123));
    }

    @Test
    void format_price_unknown() {
        Price price = Price.UNKNOWN;
        testFormatForPrice(price);
    }

    private void testFormatForPrice(Price price) {
        String transactionDescription = "xxx";
        mockPrice(Price.UNKNOWN);
        when(priceService.getPrice(TRANSACTION.getTime(), BTC)).thenReturn(price);
        when(addressFormatter.getFormattedOwnershipStatus(any())).thenReturn("?");
        when(addressDescriptionService.get(any()))
                .then(invocation -> new AddressWithDescription(invocation.getArgument(0)));
        when(transactionDescriptionService.getDescription(any())).thenReturn(transactionDescription);

        String formattedInputs = formattedInputOutput(INPUT_2, price) + "\n" + formattedInputOutput(INPUT_1, price);
        String formattedOutputs =
                formattedInputOutput(OUTPUT_1, price) + "\n" + formattedInputOutput(OUTPUT_2, price);
        when(addressOwnershipService.getOwnedAddresses()).thenReturn(Set.of(INPUT_ADDRESS_1, OUTPUT_ADDRESS_2));
        Coins contribution = Coins.NONE.subtract(INPUT_VALUE_1).add(OUTPUT_VALUE_2);
        String expected = """
                Transaction:\t%s
                Description:\t%s
                Block:\t\t%d (%s)
                Fees:\t\t%s
                Contribution:\t%s
                Inputs:%n%s
                Outputs:%n%s
                """.formatted(
                TRANSACTION.getHash(),
                transactionDescription,
                TRANSACTION.getBlockHeight(),
                DATE_TIME.format(DateTimeFormatter.ISO_DATE_TIME),
                formattedCoinsWithPrice(TRANSACTION.getFees(), price),
                formattedCoinsWithPrice(contribution, price),
                formattedInputs,
                formattedOutputs
        );
        assertThat(transactionFormatter.format(TRANSACTION)).isEqualTo(expected);
    }

    @Nested
    class Aggregation {
        private Price price;

        @BeforeEach
        void setUp() {
            price = mockPrice(Price.of(123));
            when(addressFormatter.getFormattedOwnershipStatus(any())).thenReturn("?");
            when(addressDescriptionService.get(any()))
                    .then(invocation -> new AddressWithDescription(invocation.getArgument(0)));
        }

        @Test
        void format_aggregates_inputs_per_address() {
            Coins sumOfInputs = INPUT_VALUE_1.add(INPUT_VALUE_2);
            Transaction transaction = new Transaction(
                    TRANSACTION_HASH,
                    BLOCK_HEIGHT,
                    DATE_TIME,
                    sumOfInputs,
                    List.of(new Input(INPUT_VALUE_1, INPUT_ADDRESS_1), new Input(INPUT_VALUE_2, INPUT_ADDRESS_1)),
                    List.of(),
                    BTC
            );
            String details = formattedCoinsWithPrice(sumOfInputs, price);
            assertThat(transactionFormatter.format(transaction))
                    .contains("bc1xxxn59nfqcw2la4ms7zsphqllm5789syhrgcupw ? " + details);
        }

        @Test
        void format_aggregates_outputs_per_address() {
            Coins sumOfOutputs = OUTPUT_VALUE_1.add(OUTPUT_VALUE_2);
            Transaction transaction = new Transaction(
                    TRANSACTION_HASH,
                    BLOCK_HEIGHT,
                    DATE_TIME,
                    Coins.NONE,
                    List.of(new Input(sumOfOutputs, INPUT_ADDRESS_1)),
                    List.of(new Output(OUTPUT_VALUE_1, OUTPUT_ADDRESS_1), new Output(OUTPUT_VALUE_2, OUTPUT_ADDRESS_1)),
                    BTC
            );
            String details = formattedCoinsWithPrice(sumOfOutputs, price);
            assertThat(transactionFormatter.format(transaction))
                    .contains("bc1qt9n59nfqcw2la4ms7zsphqllm5789syhrgcupw ? " + details);
        }
    }

    @Test
    void formatSingleLineForAddress() {
        when(transactionDescriptionService.get(TRANSACTION_HASH))
                .thenReturn(new TransactionWithDescription(TRANSACTION_HASH, "hello"));
        Coins coins = Coins.ofSatoshis(-2_147_483_646);
        assertThat(transactionFormatter.formatSingleLineForAddress(TRANSACTION, INPUT_ADDRESS_2))
                .isEqualTo(transactionFormatter.formatSingleLineForValue(TRANSACTION, coins));
    }

    @Test
    void formatSingleLineForValue_without_description() {
        Price price = mockPrice(Price.of(123));
        Coins value = Coins.ofSatoshis(100);
        String formattedTime = TRANSACTION.getTime().format(DateTimeFormatter.ISO_DATE_TIME);
        when(transactionDescriptionService.get(TRANSACTION_HASH))
                .thenReturn(new TransactionWithDescription(TRANSACTION_HASH));
        String expected = "%s: %s (block %d, %s)".formatted(
                TRANSACTION_HASH,
                formattedCoinsWithPrice(value, price),
                TRANSACTION.getBlockHeight(),
                formattedTime
        );
        assertThat(transactionFormatter.formatSingleLineForValue(TRANSACTION, value))
                .isEqualTo(expected);
    }

    @Test
    void formatSingleLineForValue_with_description() {
        Price price = mockPrice(Price.of(123));
        Coins value = Coins.ofSatoshis(500);
        String formattedTime = TRANSACTION.getTime().format(DateTimeFormatter.ISO_DATE_TIME);
        String description = "xxx";
        TransactionWithDescription transactionWithDescription =
                new TransactionWithDescription(TRANSACTION_HASH, description);
        when(transactionDescriptionService.get(TRANSACTION_HASH))
                .thenReturn(transactionWithDescription);
        String formattedDescription = transactionWithDescription.getFormattedDescription();
        String expected = "%s: %s (block %d, %s) %s".formatted(
                TRANSACTION_HASH,
                formattedCoinsWithPrice(value, price),
                TRANSACTION.getBlockHeight(),
                formattedTime,
                formattedDescription
        );
        assertThat(transactionFormatter.formatSingleLineForValue(TRANSACTION, value)).isEqualTo(expected);
    }

    @Test
    void formatSingleLineForValue_without_price() {
        when(transactionDescriptionService.get(any()))
                .then(invocation -> new TransactionWithDescription(invocation.getArgument(0)));
        mockPrice(Price.UNKNOWN);
        Coins value = Coins.ofSatoshis(123);
        String formattedTime = TRANSACTION.getTime().format(DateTimeFormatter.ISO_DATE_TIME);
        String expected = "%s: %s (block %d, %s)".formatted(
                TRANSACTION.getHash(),
                formattedCoinsWithPrice(value, Price.UNKNOWN),
                TRANSACTION.getBlockHeight(),
                formattedTime
        );
        assertThat(transactionFormatter.formatSingleLineForValue(TRANSACTION, value)).isEqualTo(expected);
    }

    private Price mockPrice(Price price) {
        when(priceFormatter.format(any(), any()))
                .then(invocation -> invocation.getArgument(0) + "/" + invocation.getArgument(1));
        when(priceService.getPrice(TRANSACTION.getTime(), BTC)).thenReturn(price);
        return price;
    }

    private String formattedInputOutput(InputOutput inputOutput, Price price) {
        Coins coins = inputOutput.getValue();
        return new AddressWithDescription(inputOutput.getAddress())
                .getFormattedWithInfix("? " + formattedCoinsWithPrice(coins, price));
    }

    private String formattedCoinsWithPrice(Coins coins, Price price) {
        return coins + " " + getPriceForCoins(coins, price);
    }

    private String getPriceForCoins(Coins coins, Price price) {
        return "[" + coins + "/" + price.toString() + "]";
    }
}