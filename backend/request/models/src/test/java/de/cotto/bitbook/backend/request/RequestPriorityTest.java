package de.cotto.bitbook.backend.request;

import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.MEDIUM;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;

class RequestPriorityTest {
    @Test
    void asInteger_standard_smaller_than_lowest() {
        assertThat(STANDARD.getIntegerForComparison()).isLessThan(LOWEST.getIntegerForComparison());
    }

    @Test
    void asInteger_standard_smaller_than_medium() {
        assertThat(STANDARD.getIntegerForComparison()).isLessThan(MEDIUM.getIntegerForComparison());
    }

    @Test
    void asInteger_medium_smaller_than_lowest() {
        assertThat(MEDIUM.getIntegerForComparison()).isLessThan(LOWEST.getIntegerForComparison());
    }

    @Test
    void isAtLeast_standard_standard() {
        assertThat(STANDARD.isAtLeast(STANDARD)).isTrue();
    }

    @Test
    void isAtLeast_standard_medium() {
        assertThat(STANDARD.isAtLeast(MEDIUM)).isTrue();
    }

    @Test
    void isAtLeast_medium_lowest() {
        assertThat(MEDIUM.isAtLeast(LOWEST)).isTrue();
    }

    @Test
    void isAtLeast_lowest_lowest() {
        assertThat(LOWEST.isAtLeast(LOWEST)).isTrue();
    }

    @Test
    void isAtLeast_standard_is_at_least_lowest() {
        assertThat(STANDARD.isAtLeast(LOWEST)).isTrue();
    }

    @Test
    void isAtLeast_false() {
        assertThat(LOWEST.isAtLeast(STANDARD)).isFalse();
    }

    @Test
    void getHighestPriority_this() {
        assertThat(STANDARD.getHighestPriority(LOWEST)).isEqualTo(STANDARD);
    }

    @Test
    void getHighestPriority_other() {
        assertThat(LOWEST.getHighestPriority(STANDARD)).isEqualTo(STANDARD);
    }

    @Test
    void getHighestPriority_medium_vs_lowest() {
        assertThat(MEDIUM.getHighestPriority(LOWEST)).isEqualTo(MEDIUM);
    }

    @Test
    void getHighestPriority_same() {
        assertThat(LOWEST.getHighestPriority(LOWEST)).isEqualTo(LOWEST);
    }
}