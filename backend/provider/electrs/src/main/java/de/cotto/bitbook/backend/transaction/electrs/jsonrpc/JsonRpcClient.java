package de.cotto.bitbook.backend.transaction.electrs.jsonrpc;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

public class JsonRpcClient {
    private static final long CONNECT_TIMEOUT = 500;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String host;
    private final int port;

    public JsonRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void sendMessages(JsonRpcMessage... messages) {
        sendMessages(Arrays.asList(messages));
    }

    public void sendMessages(List<JsonRpcMessage> messages) {
        NioSocketConnector connector = new NioSocketConnector();
        connector.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new CodecFactory()));
        connector.setConnectTimeoutMillis(CONNECT_TIMEOUT);
        if (messages.isEmpty()) {
            return;
        }
        connector.setHandler(new ClientSessionHandler(messages));
        try {
            ConnectFuture future = connector.connect(new InetSocketAddress(host, port));
            future.awaitUninterruptibly();
            IoSession session = future.getSession();
            session.getCloseFuture().awaitUninterruptibly();
        } catch (RuntimeIoException exception) {
            logger.debug("Failed to connect: ", exception);
        }
        connector.dispose();
    }

}
