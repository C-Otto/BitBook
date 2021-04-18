package de.cotto.bitbook.lnd;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChannelPointParserTest {

    @Test
    void getTransactionHash() {
        assertThat(ChannelPointParser.getTransactionHash("abc:123")).isEqualTo("abc");
    }

    @Test
    void getOutputIndex() {
        assertThat(ChannelPointParser.getOutputIndex("abc:123")).isEqualTo(123);
    }
}