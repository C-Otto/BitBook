package de.cotto.bitbook.backend.price.kraken;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.price.model.Price;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class KrakenTradesDtoTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void no_trade() throws Exception {
        String json = "{\"error\":[],\"result\":{\"XXBTZEUR\":[],\"last\":\"1577845610442700000\"}}";
        KrakenTradesDto krakenTradesDto = objectMapper.readValue(json, KrakenTradesDto.class);
        assertThat(krakenTradesDto.getTrades()).isEmpty();
    }

    @Test
    void error() throws Exception {
        String json = "{\"error\":[\"bla\"]}";
        KrakenTradesDto krakenTradesDto = objectMapper.readValue(json, KrakenTradesDto.class);
        assertThat(krakenTradesDto.getTrades()).isEmpty();
    }

    @Test
    void three_trades() throws Exception {
        String trade1 = "[\"100.500\",\"0.357\",1577836801.631,\"b\",\"m\",\"\"]";
        String trade2 = "[\"200.500\",\"1.357\",1577836802.631,\"b\",\"m\",\"\"]";
        String trade3 = "[\"300.500\",\"2.357\",1577836803.631,\"b\",\"m\",\"\"]";
        KrakenTradesDto.Trade expectedTrade1 =
                new KrakenTradesDto.Trade(Price.of(100.500), BigDecimal.valueOf(0.357), 1_577_836_801L);
        KrakenTradesDto.Trade expectedTrade2 =
                new KrakenTradesDto.Trade(Price.of(200.500), BigDecimal.valueOf(1.357), 1_577_836_802L);
        KrakenTradesDto.Trade expectedTrade3 =
                new KrakenTradesDto.Trade(Price.of(300.500), BigDecimal.valueOf(2.357), 1_577_836_803L);
        String json = "{\"error\":[],\"result\":{\"XXBTZEUR\":[" +
                trade1 + "," +
                trade2 + "," +
                trade3 +
                "],\"last\":\"1577845610442700000\"}}";
        KrakenTradesDto krakenTradesDto = objectMapper.readValue(json, KrakenTradesDto.class);
        assertThat(krakenTradesDto.getTrades()).usingRecursiveFieldByFieldElementComparator()
                .containsExactly(expectedTrade1, expectedTrade2, expectedTrade3);
    }

    @Test
    void trade_getPrice() {
        Price price = Price.of(3000.5);
        KrakenTradesDto.Trade trade = new KrakenTradesDto.Trade(price, BigDecimal.ONE, 1L);
        assertThat(trade.getPrice()).isEqualTo(price);
    }

    @Test
    void trade_getVolume() {
        KrakenTradesDto.Trade trade = new KrakenTradesDto.Trade(Price.of(1), BigDecimal.valueOf(2.003), 1L);
        assertThat(trade.getVolume()).isEqualTo(BigDecimal.valueOf(2.003));
    }

    @Test
    void trade_getTimestamp() {
        KrakenTradesDto.Trade trade = new KrakenTradesDto.Trade(Price.of(1), BigDecimal.ONE, 123L);
        assertThat(trade.getTimestamp()).isEqualTo(123L);
    }
}