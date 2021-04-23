package de.cotto.bitbook.backend.request;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class ResultFutureTest {
    private static final String RESULT = "xxx";
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    @Nullable
    private String resultConsumerResult;

    @Test
    void provideResult_then_getResult() {
        ResultFuture<String> resultFuture = new ResultFuture<>(this::resultConsumer);
        resultFuture.provideResult("a");
        assertThat(resultFuture.getResult()).contains("a");
    }

    @Test
    void stopWithoutResult_then_getResult() {
        ResultFuture<String> resultFuture = new ResultFuture<>(this::resultConsumer);
        resultFuture.stopWithoutResult();
        assertThat(resultFuture.getResult()).isEmpty();
    }

    @Test
    void getResult_then_provideResult() {
        ResultFuture<String> resultFuture = new ResultFuture<>(this::resultConsumer);
        executor.execute(() -> resultFuture.provideResult("x"));
        await().atMost(1, SECONDS).untilAsserted(
                () -> assertThat(resultFuture.getResult()).contains("x")
        );
    }

    @Test
    void getResult_then_stopWithoutResult() {
        ResultFuture<String> resultFuture = new ResultFuture<>(this::resultConsumer);
        executor.execute(resultFuture::stopWithoutResult);
        await().atMost(1, SECONDS).untilAsserted(
                () -> assertThat(resultFuture.getResult()).isEmpty()
        );
    }

    @Test
    void getFuture() throws Exception {
        ResultFuture<String> resultFuture = new ResultFuture<>(this::resultConsumer);
        executor.execute(() -> resultFuture.provideResult("x"));
        assertThat(resultFuture.getFuture().get(1, SECONDS)).contains("x");
    }

    @Test
    void getFuture_stopped() {
        ResultFuture<String> resultFuture = new ResultFuture<>(this::resultConsumer);
        executor.execute(resultFuture::stopWithoutResult);
        await().atMost(1, SECONDS).untilAsserted(
                () -> assertThat(resultFuture.getFuture()).isCancelled()
        );
    }

    @Test
    void getFuture_is_copy_of_original_future_with_result() {
        ResultFuture<String> resultFuture = new ResultFuture<>(this::resultConsumer);
        resultFuture.getFuture().complete("z");
        resultFuture.provideResult("y");
        assertThat(resultFuture.getResult()).contains("y");
    }

    @Test
    void getFuture_is_copy_of_original_future_copy_is_cancelled() {
        ResultFuture<String> resultFuture = new ResultFuture<>(this::resultConsumer);
        resultFuture.getFuture().cancel(true);
        resultFuture.provideResult("y");
        assertThat(resultFuture.getResult()).contains("y");
    }

    @Test
    void getFuture_is_copy_of_original_future_original_is_cancelled() {
        ResultFuture<String> resultFuture = new ResultFuture<>(this::resultConsumer);
        resultFuture.getFuture().complete("z");
        resultFuture.stopWithoutResult();
        assertThat(resultFuture.getResult()).isEmpty();
    }

    @Test
    void withResultConsumer() {
        new ResultFuture<>(this::resultConsumer).provideResult(RESULT);
        assertThat(resultConsumerResult).isEqualTo(RESULT);
    }

    @Test
    void addResultListener_forwards_result_to_consumer_of_given_listener() {
        ResultFuture<String> resultFuture = new ResultFuture<>(r -> {});
        resultFuture.addResultListener(new ResultFuture<>(this::resultConsumer));

        resultFuture.provideResult(RESULT);

        assertThat(resultConsumerResult).isEqualTo(RESULT);
    }

    @Test
    void addResultListener_forwards_result_to_future_of_given_listener() {
        ResultFuture<String> resultFuture = new ResultFuture<>(this::resultConsumer);
        ResultFuture<String> secondResultFuture = new ResultFuture<>(this::resultConsumer);
        resultFuture.addResultListener(secondResultFuture);

        resultFuture.provideResult(RESULT);

        assertThat(secondResultFuture.getResult()).contains(RESULT);
    }

    @Test
    void addResultListener_forwards_result_to_existing_future() {
        ResultFuture<String> resultFuture = new ResultFuture<>(this::resultConsumer);
        ResultFuture<String> secondResultFuture = new ResultFuture<>(this::resultConsumer);
        resultFuture.addResultListener(secondResultFuture);

        resultFuture.provideResult(RESULT);

        assertThat(resultFuture.getResult()).contains(RESULT);
    }

    @Test
    void addResultListener_forwards_result_to_existing_consumer() {
        ResultFuture<String> resultFuture = new ResultFuture<>(this::resultConsumer);
        ResultFuture<String> secondResultFuture = new ResultFuture<>(r -> {});
        resultFuture.addResultListener(secondResultFuture);

        resultFuture.provideResult(RESULT);

        assertThat(resultConsumerResult).isEqualTo(RESULT);
    }

    @Test
    void addResultListener_forwards_failure_to_listener() {
        ResultFuture<String> resultFuture = new ResultFuture<>(this::resultConsumer);
        ResultFuture<String> secondResultFuture = new ResultFuture<>(this::resultConsumer);
        resultFuture.addResultListener(secondResultFuture);

        resultFuture.stopWithoutResult();

        assertThat(secondResultFuture.getResult()).isEmpty();
    }

    @Test
    void addResultListener_forwards_failure_to_existing_future() {
        ResultFuture<String> resultFuture = new ResultFuture<>(this::resultConsumer);
        ResultFuture<String> secondResultFuture = new ResultFuture<>(this::resultConsumer);
        resultFuture.addResultListener(secondResultFuture);

        resultFuture.stopWithoutResult();

        assertThat(resultFuture.getResult()).isEmpty();
    }

    private void resultConsumer(String result) {
        resultConsumerResult = result;
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(ResultFuture.class).usingGetClass().verify();
    }
}