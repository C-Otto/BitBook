package de.cotto.bitbook.backend.transaction.electrs.jsonrpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ClientSessionHandler extends IoHandlerAdapter {
    private final List<JsonRpcMessage> messages;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClientSessionHandler(List<JsonRpcMessage> messages) {
        super();
        this.messages = messages;
    }

    @Override
    public void sessionOpened(IoSession session) {
        messages.forEach(session::write);
    }

    @Override
    public void sessionClosed(IoSession session) {
        messages.forEach(JsonRpcMessage::cancel);
    }

    @Override
    public void messageReceived(IoSession session, Object response) {
        response.toString().lines().forEach(line -> {
            try {
                JsonNode jsonNode = objectMapper.readTree(line);
                for (JsonNode subNode : jsonNode) {
                    messages.forEach(message -> message.responseReceived(subNode));
                }
            } catch (JsonProcessingException exception) {
                logger.warn("received non-JSON message: {}", response);
                session.closeNow();
            }
        });
        if (messages.stream().allMatch(JsonRpcMessage::isDone)) {
            session.closeNow();
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        logger.error("Caught exception, closing session", cause);
        session.closeNow();
    }
}
