package de.cotto.bitbook.backend.transaction.electrs.jsonrpc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.nio.charset.CharacterCodingException;

class Decoder extends CumulativeProtocolDecoder {
    private static final String DECODER_STATE_KEY = Decoder.class.getName() + ".STATE";
    private static final String NEWLINE = "\n";

    public Decoder() {
        super();
    }

    @Override
    protected boolean doDecode(
            IoSession session,
            IoBuffer ioBuffer,
            ProtocolDecoderOutput out
    ) throws CharacterCodingException {
        if (ioBuffer.remaining() <= 0) {
            return false;
        }
        String string = ioBuffer.getString(Charsets.UTF_8.newDecoder());
        DecoderState decoderState = addDecoderState(session);
        decoderState.append(string);
        if (string.endsWith(NEWLINE)) {
            out.write(decoderState.toString());
            decoderState.reset();
            return true;
        }
        return false;
    }

    private DecoderState addDecoderState(IoSession session) {
        DecoderState decoderState = (DecoderState) session.getAttribute(DECODER_STATE_KEY);
        if (decoderState == null) {
            decoderState = new DecoderState();
            session.setAttribute(DECODER_STATE_KEY, decoderState);
        }
        return decoderState;
    }

    @VisibleForTesting
    static class DecoderState {
        @SuppressWarnings("PMD.AvoidStringBufferField")
        private final StringBuilder stringBuilder = new StringBuilder();

        public void append(String string) {
            stringBuilder.append(string);
        }

        @Override
        public String toString() {
            return stringBuilder.toString();
        }

        public void reset() {
            stringBuilder.delete(0, stringBuilder.length());
        }
    }
}