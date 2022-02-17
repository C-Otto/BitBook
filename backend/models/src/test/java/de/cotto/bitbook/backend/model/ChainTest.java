package de.cotto.bitbook.backend.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static de.cotto.bitbook.backend.model.Chain.BCD;
import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BSV;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.Chain.BTG;
import static org.assertj.core.api.Assertions.assertThat;

class ChainTest {

    private static final String CLASS_CAN_BE_STATIC = "ClassCanBeStatic";

    @Nested
    @SuppressWarnings(CLASS_CAN_BE_STATIC)
    class Bitcoin {

        private final LocalDate forkDate = LocalDate.of(2009, 1, 3);

        @Test
        void enum_string_value() {
            assertThat(BTC).hasToString("BTC");
            assertThat(Chain.valueOf("BTC")).isEqualTo(BTC);
        }

        @Test
        void getChainForDate_before_fork_date() {
            assertThat(BTC.getChainForDate(forkDate.minusDays(1))).isEqualTo(BTC);
        }

        @Test
        void getChainForDate_at_fork_date() {
            assertThat(BTC.getChainForDate(forkDate)).isEqualTo(BTC);
        }

        @Test
        void getChainForDate_after_fork_date() {
            assertThat(BTC.getChainForDate(forkDate.plusDays(1))).isEqualTo(BTC);
        }

        @Test
        void getChainForDate_future() {
            assertThat(BTC.getChainForDate(forkDate.plusYears(30))).isEqualTo(BTC);
        }
    }

    @Nested
    @SuppressWarnings(CLASS_CAN_BE_STATIC)
    class BitcoinCash {

        private final LocalDate forkDate = LocalDate.of(2017, 8, 1);

        @Test
        void enum_string_value() {
            assertThat(BCH).hasToString("BCH");
            assertThat(Chain.valueOf("BCH")).isEqualTo(BCH);
        }

        @Test
        void getChainForDate_before_fork_date() {
            assertThat(BCH.getChainForDate(forkDate.minusDays(1))).isEqualTo(BTC);
        }

        @Test
        void getChainForDate_at_fork_date() {
            assertThat(BCH.getChainForDate(forkDate)).isEqualTo(BCH);
        }

        @Test
        void getChainForDate_after_fork_date() {
            assertThat(BCH.getChainForDate(forkDate.plusDays(1))).isEqualTo(BCH);
        }
    }

    @Nested
    @SuppressWarnings(CLASS_CAN_BE_STATIC)
    class BitcoinGold {

        private final LocalDate forkDate = LocalDate.of(2017, 10, 24);

        @Test
        void enum_string_value() {
            assertThat(BTG).hasToString("BTG");
            assertThat(Chain.valueOf("BTG")).isEqualTo(BTG);
        }

        @Test
        void getChainForDate_before_fork_date() {
            assertThat(BTG.getChainForDate(forkDate.minusDays(1))).isEqualTo(BTC);
        }

        @Test
        void getChainForDate_at_fork_date() {
            assertThat(BTG.getChainForDate(forkDate)).isEqualTo(BTG);
        }

        @Test
        void getChainForDate_after_fork_date() {
            assertThat(BTG.getChainForDate(forkDate.plusDays(1))).isEqualTo(BTG);
        }
    }

    @Nested
    @SuppressWarnings(CLASS_CAN_BE_STATIC)
    class BitcoinSV {

        private final LocalDate forkDate = LocalDate.of(2018, 11, 15);

        @Test
        void enum_string_value() {
            assertThat(BSV).hasToString("BSV");
            assertThat(Chain.valueOf("BSV")).isEqualTo(BSV);
        }

        @Test
        void getChainForDate_very_early() {
            assertThat(BSV.getChainForDate(LocalDate.of(2013, 12, 24))).isEqualTo(BTC);
        }

        @Test
        void getChainForDate_before_fork_date() {
            assertThat(BSV.getChainForDate(forkDate.minusDays(1))).isEqualTo(BCH);
        }

        @Test
        void getChainForDate_at_fork_date() {
            assertThat(BSV.getChainForDate(forkDate)).isEqualTo(BSV);
        }

        @Test
        void getChainForDate_after_fork_date() {
            assertThat(BSV.getChainForDate(forkDate.plusDays(1))).isEqualTo(BSV);
        }
    }

    @Nested
    @SuppressWarnings(CLASS_CAN_BE_STATIC)
    class BitcoinDiamond {

        private final LocalDate forkDate = LocalDate.of(2017, 11, 24);

        @Test
        void enum_string_value() {
            assertThat(BCD).hasToString("BCD");
            assertThat(Chain.valueOf("BCD")).isEqualTo(BCD);
        }

        @Test
        void getChainForDate_before_fork_date() {
            assertThat(BCD.getChainForDate(forkDate.minusDays(1))).isEqualTo(BTC);
        }

        @Test
        void getChainForDate_at_fork_date() {
            assertThat(BCD.getChainForDate(forkDate)).isEqualTo(BCD);
        }

        @Test
        void getChainForDate_after_fork_date() {
            assertThat(BCD.getChainForDate(forkDate.plusDays(1))).isEqualTo(BCD);
        }
    }
}