package de.cotto.bitbook.backend.transaction.electrs.jsonrpc;

import com.google.common.base.Charsets;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import javax.annotation.Nonnull;

public class Encoder implements ProtocolEncoder {
    public Encoder() {
        // default constructor
    }

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) {
        byte[] payload = getPayload(message);
        IoBuffer buffer = IoBuffer.allocate(payload.length, false);
        buffer.put(payload, 0, payload.length);
        buffer.flip();
        out.write(buffer);
    }

    @Nonnull
    private byte[] getPayload(Object message) {
        JsonRpcMessage request = (JsonRpcMessage) message;
        String stringToSend = request.toString() + "\n";
        return stringToSend.getBytes(Charsets.UTF_8);
    }

    @Override
    public void dispose(IoSession session) {
        // nothing to dispose
    }
}