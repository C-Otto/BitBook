package de.cotto.bitbook.backend.transaction.electrs.jsonrpc;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecoderTest {
    private static final String STATE_ATTRIBUTE_NAME =
            "de.cotto.bitbook.backend.transaction.electrs.jsonrpc.Decoder.STATE";

    @InjectMocks
    private Decoder decoder;

    @Mock
    private IoSession ioSession;

    @Mock
    private IoBuffer ioBuffer;

    @Mock
    private ProtocolDecoderOutput out;

    @Test
    void no_data() throws Exception {
        when(ioBuffer.remaining()).thenReturn(0);
        assertThat(decoder.doDecode(ioSession, ioBuffer, out)).isFalse();
        verifyNoInteractions(out);
    }

    @Test
    void data_ending_with_newline() throws Exception {
        when(ioBuffer.remaining()).thenReturn(4);
        when(ioBuffer.getString(any())).thenReturn("foo\n");
        assertThat(decoder.doDecode(ioSession, ioBuffer, out)).isTrue();
        verify(out).write("foo\n");
    }

    @Test
    void uses_utf8() throws Exception {
        when(ioBuffer.remaining()).thenReturn(4);
        when(ioBuffer.getString(any())).thenReturn("foo\n");
        decoder.doDecode(ioSession, ioBuffer, out);
        verify(ioBuffer).getString(argThat(decoder -> decoder.charset().equals(UTF_8)));
    }

    @Test
    void incomplete_data() throws Exception {
        when(ioBuffer.remaining()).thenReturn(4);
        when(ioBuffer.getString(any())).thenReturn("foo");
        assertThat(decoder.doDecode(ioSession, ioBuffer, out)).isFalse();
        verifyNoInteractions(out);
    }

    @Test
    void stores_state_for_imcomplete_message() throws Exception {
        when(ioBuffer.remaining()).thenReturn(5);
        when(ioBuffer.getString(any())).thenReturn("(old)");
        assertThat(decoder.doDecode(ioSession, ioBuffer, out)).isFalse();
        verify(ioSession).setAttribute(eq(STATE_ATTRIBUTE_NAME), argThat(state -> "(old)".equals(state.toString())));
        verifyNoInteractions(out);
    }

    @Test
    void reuses_previous_decoder_state() throws Exception {
        Decoder.DecoderState decoderState = new Decoder.DecoderState();
        decoderState.append("(old)");
        when(ioSession.getAttribute(STATE_ATTRIBUTE_NAME)).thenReturn(decoderState);
        when(ioBuffer.remaining()).thenReturn(1);
        when(ioBuffer.getString(any())).thenReturn("\n");
        assertThat(decoder.doDecode(ioSession, ioBuffer, out)).isTrue();
        verify(out).write("(old)\n");
    }

    @Test
    void reuses_previous_decoder_state_only_once() throws Exception {
        Decoder.DecoderState decoderState = new Decoder.DecoderState();
        decoderState.append("AAA");
        when(ioSession.getAttribute(STATE_ATTRIBUTE_NAME)).thenReturn(decoderState);
        when(ioBuffer.remaining()).thenReturn(1).thenReturn(6);
        when(ioBuffer.getString(any())).thenReturn("\n").thenReturn("(new)\n");
        decoder.doDecode(ioSession, ioBuffer, out);
        decoder.doDecode(ioSession, ioBuffer, out);
        verify(out).write("AAA\n");
        verify(out).write("(new)\n");
    }
}