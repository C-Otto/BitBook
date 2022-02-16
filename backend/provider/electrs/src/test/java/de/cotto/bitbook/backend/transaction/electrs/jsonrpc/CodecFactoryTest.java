package de.cotto.bitbook.backend.transaction.electrs.jsonrpc;

import org.apache.mina.core.session.IoSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CodecFactoryTest {
    private final CodecFactory codecFactory = new CodecFactory();

    @Mock
    private IoSession ioSession;

    @Test
    void encoder() {
        assertThat(codecFactory.getEncoder(ioSession)).isInstanceOf(Encoder.class);
    }

    @Test
    void decoder() {
        assertThat(codecFactory.getDecoder(ioSession)).isInstanceOf(Decoder.class);
    }
}