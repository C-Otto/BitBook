package de.cotto.bitbook.backend.transaction.electrs.jsonrpc;

import org.junit.jupiter.api.Test;

class JsonRpcClientTest {
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String LOCALHOST = "127.0.0.1";

    private final JsonRpcClient jsonRpcClient = new JsonRpcClient(LOCALHOST, 1);

    @Test
    void does_nothing_if_no_message_should_be_sent() {
        jsonRpcClient.sendMessages();
    }
}