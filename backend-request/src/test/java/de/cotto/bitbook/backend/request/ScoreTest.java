package de.cotto.bitbook.backend.request;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreTest {
    @Test
    void add() {
        Score scoreOne = new Score(100).add(new ScoreUpdate(200));
        Score scoreTwo = new Score(100).add(new ScoreUpdate(200));
        assertThat(scoreOne).isEqualTo(scoreTwo);
    }

    @Test
    void add_same_sum_different_list() {
        Score scoreOne = new Score(100).add(new ScoreUpdate(200));
        Score scoreTwo = new Score(200).add(new ScoreUpdate(100));
        assertThat(scoreOne).isNotEqualTo(scoreTwo);
    }

    @Test
    void add_four_values() {
        Score fiveAdditions = new Score(100)
                .add(new ScoreUpdate(1))
                .add(new ScoreUpdate(1))
                .add(new ScoreUpdate(1))
                .add(new ScoreUpdate(1));
        assertThat(fiveAdditions.compareTo(new Score(104))).isEqualTo(0);
    }

    @Test
    void add_five_values_drops_first() {
        Score fiveAdditions = new Score(100)
                .add(new ScoreUpdate(1))
                .add(new ScoreUpdate(1))
                .add(new ScoreUpdate(1))
                .add(new ScoreUpdate(1))
                .add(new ScoreUpdate(1));
        assertThat(fiveAdditions.compareTo(new Score(5))).isEqualTo(0);
    }

    @Test
    void compare_lower_is_better() {
        assertThat(new Score(100).compareTo(new Score(200))).isLessThan(0);
    }

    @Test
    void compare_order_does_not_matter() {
        assertThat(new Score(100).compareTo(new Score(40).add(new ScoreUpdate(60)))).isEqualTo(0);
    }

    @Test
    void testToString() {
        assertThat(new Score(100)).hasToString("Score{list=[100]}");
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Score.class).usingGetClass().verify();
    }
}