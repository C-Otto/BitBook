package de.cotto.bitbook.backend.transaction.electrs.jsonrpc;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;

public class CodecFactory implements ProtocolCodecFactory {
    private final Encoder encoder;
    private final Decoder decoder;

    public CodecFactory() {
        encoder = new Encoder();
        decoder = new Decoder();
    }

    @Override
    public Encoder getEncoder(IoSession ioSession) {
        return encoder;
    }

    @Override
    public Decoder getDecoder(IoSession ioSession) {
        return decoder;
    }
}