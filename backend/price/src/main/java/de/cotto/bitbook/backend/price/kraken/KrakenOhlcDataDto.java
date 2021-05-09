package de.cotto.bitbook.backend.price.kraken;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cotto.bitbook.backend.price.model.Price;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@JsonDeserialize(using = KrakenOhlcDataDto.KrakenOhlDataDeserializer.class)
public class KrakenOhlcDataDto {
    private final List<OhlcEntry> ohlcEntries;

    public KrakenOhlcDataDto(List<OhlcEntry> ohlcEntries) {
        this.ohlcEntries = ohlcEntries;
    }

    public static class OhlcEntry {
        private final long timestamp;
        private final Price openPrice;

        public OhlcEntry(long timestamp, Price openPrice) {
            this.timestamp = timestamp;
            this.openPrice = openPrice;
        }

        public OhlcEntry(long timestamp, double openPrice) {
            this(timestamp, Price.of(openPrice));
        }

        public long getTimestamp() {
            return timestamp;
        }

        public Price getOpenPrice() {
            return openPrice;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            OhlcEntry ohlcEntry = (OhlcEntry) other;
            return timestamp == ohlcEntry.timestamp && Objects.equals(openPrice, ohlcEntry.openPrice);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timestamp, openPrice);
        }
    }

    public List<OhlcEntry> getOhlcEntries() {
        return ohlcEntries;
    }

    static class KrakenOhlDataDeserializer extends JsonDeserializer<KrakenOhlcDataDto> {
        @Override
        public KrakenOhlcDataDto deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext
        ) throws IOException {
            JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
            if (!rootNode.get("error").isEmpty()) {
                return new KrakenOhlcDataDto(List.of());
            }
            JsonNode euroEntries = rootNode.get("result").get("XXBTZEUR");

            List<OhlcEntry> ohlcEntries = StreamSupport.stream(euroEntries.spliterator(), false)
                    .map(euroEntry -> new OhlcEntry(euroEntry.get(0).asLong(), euroEntry.get(1).asDouble()))
                    .collect(Collectors.toList());
            return new KrakenOhlcDataDto(ohlcEntries);
        }
    }
}
