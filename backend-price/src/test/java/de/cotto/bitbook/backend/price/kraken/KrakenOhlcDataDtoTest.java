package de.cotto.bitbook.backend.price.kraken;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.price.model.Price;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KrakenOhlcDataDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void no_trade() throws Exception {
        String json = "{\"error\":[],\"result\":{\"XXBTZEUR\":[],\"last\":1611360000}}";
        KrakenOhlcDataDto krakenTradesDtoOhlcEntries = objectMapper.readValue(json, KrakenOhlcDataDto.class);
        assertThat(krakenTradesDtoOhlcEntries.getOhlcEntries()).isEmpty();
    }

    @Test
    void error() throws Exception {
        String json = "{\"error\":[\"bla\"]}";
        KrakenOhlcDataDto krakenTradesDtoOhlcEntries = objectMapper.readValue(json, KrakenOhlcDataDto.class);
        assertThat(krakenTradesDtoOhlcEntries.getOhlcEntries()).isEmpty();
    }

    @Test
    void three_trades() throws Exception {
        String entry1 = "[1234,\"500.1234\",\"9045.0\",\"8825.0\",\"9006.9\",\"8928.0\",\"4129.60810615\",22656]";
        String entry2 = "[1235,\"600.1234\",\"9045.0\",\"8825.0\",\"9006.9\",\"8928.0\",\"4129.60810615\",22656]";
        String entry3 = "[1236,\"700.1234\",\"9045.0\",\"8825.0\",\"9006.9\",\"8928.0\",\"4129.60810615\",22656]";
        KrakenOhlcDataDto.OhlcEntry expectedEntry1 = new KrakenOhlcDataDto.OhlcEntry(1234L, Price.of(500.1234));
        KrakenOhlcDataDto.OhlcEntry expectedEntry2 = new KrakenOhlcDataDto.OhlcEntry(1235L, Price.of(600.1234));
        KrakenOhlcDataDto.OhlcEntry expectedEntry3 = new KrakenOhlcDataDto.OhlcEntry(1236L, Price.of(700.1234));
        String json = "{\"error\":[],\"result\":{\"XXBTZEUR\":[" +
                entry1 + "," +
                entry2 + "," +
                entry3 +
                "],\"last\":1611360000}}";
        KrakenOhlcDataDto krakenOhlcDataDto = objectMapper.readValue(json, KrakenOhlcDataDto.class);
        assertThat(krakenOhlcDataDto.getOhlcEntries()).containsExactly(expectedEntry1, expectedEntry2, expectedEntry3);
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    class OhlcEntryTest {
        @Test
        void testEquals() {
            EqualsVerifier.forClass(KrakenOhlcDataDto.OhlcEntry.class).usingGetClass().verify();
        }

        @Test
        void getTimestamp() {
            long timestamp = 1234L;
            KrakenOhlcDataDto.OhlcEntry entry = new KrakenOhlcDataDto.OhlcEntry(timestamp, Price.of(500.1234));
            assertThat(entry.getTimestamp()).isEqualTo(timestamp);
        }

        @Test
        void getOpenPrice() {
            Price openPrice = Price.of(500.1234);
            KrakenOhlcDataDto.OhlcEntry entry = new KrakenOhlcDataDto.OhlcEntry(1234L, openPrice);
            assertThat(entry.getOpenPrice()).isEqualTo(openPrice);
        }
    }
}