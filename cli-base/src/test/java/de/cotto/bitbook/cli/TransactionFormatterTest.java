package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.price.PriceService;
import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Input;
import de.cotto.bitbook.backend.transaction.model.InputOutput;
import de.cotto.bitbook.backend.transaction.model.Output;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import de.cotto.bitbook.backend.transaction.model.TransactionFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_1;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_2;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_VALUE_1;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_VALUE_2;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_2;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_VALUE_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_VALUE_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionFormatterTest {
    @InjectMocks
    private TransactionFormatter transactionFormatter;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Mock
    private AddressFormatter addressFormatter;

    @Mock
    private PriceService priceService;

    @Mock
    private PriceFormatter priceFormatter;

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
        mockPrice(Price.UNKNOWN);
        when(priceService.getPrice(TRANSACTION.getTime())).thenReturn(price);
        when(addressFormatter.getFormattedOwnershipStatus(any())).thenReturn("?");
        when(addressDescriptionService.get(any()))
                .then(invocation -> new AddressWithDescription(invocation.getArgument(0)));

        String formattedInputs = formattedInputOutput(INPUT_2, price) + "\n" + formattedInputOutput(INPUT_1, price);
        String formattedOutputs =
                formattedInputOutput(OUTPUT_1, price) + "\n" + formattedInputOutput(OUTPUT_2, price);
        String expected = """
                Transaction:\t%s
                Height:\t\t%d (%s)
                Fees:\t\t%s
                Inputs:%n%s
                Outputs:%n%s
                """.formatted(
                TRANSACTION.getHash(),
                TRANSACTION.getBlockHeight(),
                DATE_TIME.format(DateTimeFormatter.ISO_DATE_TIME),
                formattedCoinsWithPrice(TRANSACTION.getFees(), price),
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
                    TransactionFixtures.TRANSACTION_HASH,
                    TransactionFixtures.BLOCK_HEIGHT,
                    DATE_TIME,
                    sumOfInputs,
                    List.of(new Input(INPUT_VALUE_1, INPUT_ADDRESS_1), new Input(INPUT_VALUE_2, INPUT_ADDRESS_1)),
                    List.of()
            );
            String details = formattedCoinsWithPrice(sumOfInputs, price);
            assertThat(transactionFormatter.format(transaction))
                    .contains("bc1xxxn59nfqcw2la4ms7zsphqllm5789syhrgcupw ? " + details);
        }

        @Test
        void format_aggregates_outputs_per_address() {
            Coins sumOfOutputs = OUTPUT_VALUE_1.add(OUTPUT_VALUE_2);
            Transaction transaction = new Transaction(
                    TransactionFixtures.TRANSACTION_HASH,
                    TransactionFixtures.BLOCK_HEIGHT,
                    DATE_TIME,
                    Coins.NONE,
                    List.of(new Input(sumOfOutputs, INPUT_ADDRESS_1)),
                    List.of(new Output(OUTPUT_VALUE_1, OUTPUT_ADDRESS_1), new Output(OUTPUT_VALUE_2, OUTPUT_ADDRESS_1))
            );
            String details = formattedCoinsWithPrice(sumOfOutputs, price);
            assertThat(transactionFormatter.format(transaction))
                    .contains("bc1qt9n59nfqcw2la4ms7zsphqllm5789syhrgcupw ? " + details);
        }
    }

    @Test
    void formatSingleLineForAddress() {
        Price price = mockPrice(Price.of(123));
        Coins coins = Coins.ofSatoshis(-2_147_483_646);
        String formattedTime = TRANSACTION.getTime().format(DateTimeFormatter.ISO_DATE_TIME);
        String expected = "%s: %s (block height %d, %s)".formatted(
                TRANSACTION.getHash(),
                formattedCoinsWithPrice(coins, price),
                TRANSACTION.getBlockHeight(),
                formattedTime
        );
        assertThat(transactionFormatter.formatSingleLineForAddress(TRANSACTION, INPUT_ADDRESS_2)).isEqualTo(expected);
    }

    @Test
    void formatSingleLineForAddress_without_price() {
        mockPrice(Price.UNKNOWN);
        Coins coins = Coins.ofSatoshis(-2_147_483_646);
        String formattedTime = TRANSACTION.getTime().format(DateTimeFormatter.ISO_DATE_TIME);
        String expected = "%s: %s (block height %d, %s)".formatted(
                TRANSACTION.getHash(),
                formattedCoinsWithPrice(coins, Price.UNKNOWN),
                TRANSACTION.getBlockHeight(),
                formattedTime
        );
        assertThat(transactionFormatter.formatSingleLineForAddress(TRANSACTION, INPUT_ADDRESS_2)).isEqualTo(expected);
    }

    private Price mockPrice(Price price) {
        when(priceFormatter.format(any(), any()))
                .then(invocation -> invocation.getArgument(0) + "/" + invocation.getArgument(1));
        when(priceService.getPrice(TRANSACTION.getTime())).thenReturn(price);
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