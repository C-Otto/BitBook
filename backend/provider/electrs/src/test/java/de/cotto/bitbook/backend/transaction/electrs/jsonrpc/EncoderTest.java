package de.cotto.bitbook.backend.transaction.electrs.jsonrpc;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class EncoderTest {
    private static final CharsetDecoder DECODER = UTF_8.newDecoder();

    @InjectMocks
    private Encoder encoder;

    @Mock
    private IoSession ioSession;

    @Mock
    private ProtocolEncoderOutput out;

    @Test
    void encodes_message_via_toString() throws Exception {
        JsonRpcMessage message = new JsonRpcMessage("m");
        encoder.encode(ioSession, message, out);
        assertThat(output()).startsWith(message.toString());
    }

    @Test
    void encode_terminates_with_newline() throws Exception {
        encoder.encode(ioSession, new JsonRpcMessage("m"), out);
        assertThat(output()).endsWith("\n");
    }

    @Test
    void encode() throws Exception {
        encoder.encode(ioSession, new JsonRpcMessage("x", "1", "2"), out);
        assertThat(output())
                .matches("\\[\\{\"method\":\"x\",\"params\":\\[\"1\",\"2\"],\"id\":\\d+,\"jsonrpc\":\"2\\.0\"}]\n");
    }

    private String output() throws Exception {
        ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);
        verify(out).write(argument.capture());
        return getStringInBuffer(argument.getValue());
    }

    private String getStringInBuffer(Object argument) throws CharacterCodingException {
        if (argument instanceof IoBuffer buffer) {
            return buffer.getString(DECODER);
        }
        return "not a buffer: " + argument;
    }

    @Test
    void dispose_does_nothing() {
        encoder.dispose(ioSession);
        verifyNoMoreInteractions(ioSession, out);
    }
}