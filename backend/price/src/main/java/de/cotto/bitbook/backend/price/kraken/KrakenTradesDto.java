package de.cotto.bitbook.backend.price.kraken;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.annotations.VisibleForTesting;
import de.cotto.bitbook.backend.price.model.Price;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@JsonDeserialize(using = KrakenTradesDto.KrakenTradesDtoDeserializer.class)
public class KrakenTradesDto {
    private final List<Trade> trades;

    public KrakenTradesDto(List<Trade> trades) {
        this.trades = trades;
    }

    public static class Trade {
        private final Price price;
        private final BigDecimal volume;
        private final long timestamp;

        @VisibleForTesting
        public Trade(Price price, BigDecimal volume, long timestamp) {
            this.price = price;
            this.volume = volume;
            this.timestamp = timestamp;
        }

        Trade(double price, double volume, long timestamp) {
            this(Price.of(price), BigDecimal.valueOf(volume), timestamp);
        }

        public Price getPrice() {
            return price;
        }

        public BigDecimal getVolume() {
            return volume;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    public List<Trade> getTrades() {
        return trades;
    }

    static class KrakenTradesDtoDeserializer extends JsonDeserializer<KrakenTradesDto> {
        @Override
        public KrakenTradesDto deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext
        ) throws IOException {
            JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
            if (!rootNode.get("error").isEmpty()) {
                return new KrakenTradesDto(List.of());
            }
            JsonNode euroTrades = rootNode.get("result").get("XXBTZEUR");

            List<Trade> trades = StreamSupport.stream(euroTrades.spliterator(), false)
                    .map(euroEntry -> new Trade(
                            euroEntry.get(0).asDouble(),
                            euroEntry.get(1).asDouble(),
                            euroEntry.get(2).asLong()))
                    .collect(Collectors.toList());
            return new KrakenTradesDto(trades);
        }
    }
}
