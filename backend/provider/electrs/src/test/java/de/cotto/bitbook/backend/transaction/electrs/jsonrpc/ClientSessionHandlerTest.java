package de.cotto.bitbook.backend.transaction.electrs.jsonrpc;

import org.apache.mina.core.session.IoSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.CheckForNull;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClientSessionHandlerTest {
    private static final String NOT_JSON = "[\"{\n";
    @CheckForNull
    private ClientSessionHandler clientSessionHandler;
    private JsonRpcMessage message1;
    private JsonRpcMessage message2;

    @Mock
    private IoSession session;

    @BeforeEach
    void setUp() {
        message1 = new JsonRpcMessage("a");
        message2 = new JsonRpcMessage("b");
    }

    @Test
    void writes_messages_on_session_opened() {
        clientSessionHandler = new ClientSessionHandler(List.of(message1, message2));
        clientSessionHandler.sessionOpened(session);
        verify(session, times(2)).write(any());
    }

    @Test
    void writes_messages_using_to_string() {
        clientSessionHandler = new ClientSessionHandler(List.of(message1));
        clientSessionHandler.sessionOpened(session);
        verify(session).write(argThat(arg -> arg.toString().equals(message1.toString())));
    }

    @Test
    void messageReceived() {
        int messageId = message1.getMessageId();
        clientSessionHandler = new ClientSessionHandler(List.of(message1));
        clientSessionHandler.messageReceived(session, "[{\"id\":%d,\"x\":\"y\"}]\n".formatted(messageId));
        assertThat(message1.getFuture().getNow(null))
                .hasToString("{\"id\":%d,\"x\":\"y\"}".formatted(messageId));
    }

    @Test
    void messageReceived_two_messages_in_one_line() {
        int messageId1 = message1.getMessageId();
        int messageId2 = message2.getMessageId();
        clientSessionHandler = new ClientSessionHandler(List.of(message1, message2));
        clientSessionHandler.messageReceived(session, "[{\"id\":%d},{\"id\":%d}]\n".formatted(messageId1, messageId2));
        assertHasResponseWithId(message1, messageId1);
        assertHasResponseWithId(message2, messageId2);
    }

    @Test
    void messageReceived_two_messages_in_two_lines() {
        int messageId1 = message1.getMessageId();
        int messageId2 = message2.getMessageId();
        clientSessionHandler = new ClientSessionHandler(List.of(message1, message2));
        clientSessionHandler.messageReceived(
                session,
                "[{\"id\":%d}]\n[{\"id\":%d}]\n".formatted(messageId1, messageId2)
        );
        assertHasResponseWithId(message1, messageId1);
        assertHasResponseWithId(message2, messageId2);
    }

    @Test
    void messageReceived_closes_session() {
        clientSessionHandler = new ClientSessionHandler(List.of(message1));
        clientSessionHandler.messageReceived(session, "[\"{\"id\":%d}}\"]\n".formatted(message1.getMessageId()));
        verify(session).closeNow();
    }

    @Test
    void messageReceived_does_not_close_session_if_message_has_no_response() {
        clientSessionHandler = new ClientSessionHandler(List.of(message1, message2));
        clientSessionHandler.messageReceived(session, "[{\"id\":%d}]\n".formatted(message1.getMessageId()));
        verify(session, never()).closeNow();
    }

    @Test
    void messageReceived_closes_session_on_parse_failure() {
        clientSessionHandler = new ClientSessionHandler(List.of(message1));
        clientSessionHandler.messageReceived(session, NOT_JSON);
        verify(session).closeNow();
    }

    @Test
    void sessionClosed_cancels_messages() {
        clientSessionHandler = new ClientSessionHandler(List.of(message1, message2));
        clientSessionHandler.sessionClosed(session);
        assertThat(message1.isDone()).isTrue();
        assertThat(message2.isDone()).isTrue();
    }

    @Test
    void exceptionCaught_closes_session() {
        clientSessionHandler = new ClientSessionHandler(List.of(message1));
        clientSessionHandler.exceptionCaught(session, new NullPointerException());
        verify(session).closeNow();
    }

    private void assertHasResponseWithId(JsonRpcMessage message, int expectedId) {
        assertThat(message.getFuture().getNow(null)).hasToString("{\"id\":%d}".formatted(expectedId));
    }

}