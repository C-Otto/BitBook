package de.cotto.bitbook.backend.request;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Score implements Comparable<Score> {
    public static final Score DEFAULT = new Score(0);
    private static final int MAX_SIZE = 5;

    private final List<Long> list = new ArrayList<>();

    public Score(long value) {
        list.add(value);
    }

    private Score(List<Long> list) {
        this.list.addAll(list);
    }

    @Override
    public int compareTo(@Nonnull Score other) {
        return Long.compare(sum(), other.sum());
    }

    public Score add(ScoreUpdate addend) {
        Score result = new Score(list);
        result.list.add(addend.getValue());
        while (result.list.size() > MAX_SIZE) {
            result.list.remove(0);
        }
        return result;
    }

    private long sum() {
        return list.stream().mapToLong(i -> i).sum();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Score score = (Score) other;
        return Objects.equals(list, score.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list);
    }

    @Override
    public String toString() {
        return "Score{" +
                "list=" + list +
                '}';
    }
}
