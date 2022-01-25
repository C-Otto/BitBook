package de.cotto.bitbook.backend.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.Chain.BCD;
import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BSV;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.Chain.BTG;
import static org.assertj.core.api.Assertions.assertThat;

class ChainTest {

    public static final String CLASS_CAN_BE_STATIC = "ClassCanBeStatic";

    @Nested
    @SuppressWarnings(CLASS_CAN_BE_STATIC)
    class Bitcoin {
        @Test
        void enum_string_value() {
            assertThat(BTC).hasToString("BTC");
            assertThat(Chain.valueOf("BTC")).isEqualTo(BTC);
        }

        @Test
        void getName() {
            assertThat(BTC.getName()).isEqualTo("Bitcoin");
        }

        @Test
        void getFirstBlockAfterFork() {
            assertThat(BTC.getFirstBlockAfterFork()).isEqualTo(0);
        }

        @Test
        void getChainForBlockHeight_genesis() {
            assertThat(BTC.getChainForBlockHeight(0)).isEqualTo(BTC);
        }

        @Test
        void getChainForBlockHeight_first_forked_block() {
            assertThat(BTC.getChainForBlockHeight(BTC.getFirstBlockAfterFork())).isEqualTo(BTC);
        }

        @Test
        void getChainForBlockHeight_after_forked_block() {
            assertThat(BTC.getChainForBlockHeight(BTC.getFirstBlockAfterFork() + 1)).isEqualTo(BTC);
        }
    }

    @Nested
    @SuppressWarnings(CLASS_CAN_BE_STATIC)
    class BitcoinCash {
        @Test
        void enum_string_value() {
            assertThat(BCH).hasToString("BCH");
            assertThat(Chain.valueOf("BCH")).isEqualTo(BCH);
        }

        @Test
        void getName() {
            assertThat(BCH.getName()).isEqualTo("Bitcoin Cash");
        }

        @Test
        void getFirstBlockAfterFork() {
            assertThat(BCH.getFirstBlockAfterFork()).isEqualTo(478_559);
        }

        @Test
        void getChainForBlockHeight_genesis() {
            assertThat(BCH.getChainForBlockHeight(0)).isEqualTo(BTC);
        }

        @Test
        void getChainForBlockHeight_before_forked_block() {
            assertThat(BCH.getChainForBlockHeight(BCH.getFirstBlockAfterFork() - 1)).isEqualTo(BTC);
        }

        @Test
        void getChainForBlockHeight_first_forked_block() {
            assertThat(BCH.getChainForBlockHeight(BCH.getFirstBlockAfterFork())).isEqualTo(BCH);
        }

        @Test
        void getChainForBlockHeight_after_forked_block() {
            assertThat(BCH.getChainForBlockHeight(BCH.getFirstBlockAfterFork() + 1)).isEqualTo(BCH);
        }
    }

    @Nested
    @SuppressWarnings(CLASS_CAN_BE_STATIC)
    class BitcoinGold {

        @Test
        void enum_string_value() {
            assertThat(BTG).hasToString("BTG");
            assertThat(Chain.valueOf("BTG")).isEqualTo(BTG);
        }

        @Test
        void getName() {
            assertThat(BTG.getName()).isEqualTo("Bitcoin Gold");
        }

        @Test
        void getFirstBlockAfterFork() {
            assertThat(BTG.getFirstBlockAfterFork()).isEqualTo(491_407);
        }

        @Test
        void getChainForBlockHeight_genesis() {
            assertThat(BTG.getChainForBlockHeight(0)).isEqualTo(BTC);
        }

        @Test
        void getChainForBlockHeight_before_forked_block() {
            assertThat(BTG.getChainForBlockHeight(BTG.getFirstBlockAfterFork() - 1)).isEqualTo(BTC);
        }

        @Test
        void getChainForBlockHeight_first_forked_block() {
            assertThat(BTG.getChainForBlockHeight(BTG.getFirstBlockAfterFork())).isEqualTo(BTG);
        }

        @Test
        void getChainForBlockHeight_after_forked_block() {
            assertThat(BTG.getChainForBlockHeight(BTG.getFirstBlockAfterFork() + 1)).isEqualTo(BTG);
        }
    }

    @Nested
    @SuppressWarnings(CLASS_CAN_BE_STATIC)
    class BitcoinSV {
        @Test
        void enum_string_value() {
            assertThat(BSV).hasToString("BSV");
            assertThat(Chain.valueOf("BSV")).isEqualTo(BSV);
        }

        @Test
        void getName() {
            assertThat(BSV.getName()).isEqualTo("Bitcoin SV");
        }

        @Test
        void getFirstBlockAfterFork() {
            assertThat(BSV.getFirstBlockAfterFork()).isEqualTo(556_767);
        }

        @Test
        void getChainForBlockHeight_genesis() {
            assertThat(BSV.getChainForBlockHeight(0)).isEqualTo(BTC);
        }

        @Test
        void getChainForBlockHeight_before_previous_fork() {
            assertThat(BSV.getChainForBlockHeight(BCH.getFirstBlockAfterFork() - 1)).isEqualTo(BTC);
        }

        @Test
        void getChainForBlockHeight_before_forked_block() {
            assertThat(BSV.getChainForBlockHeight(BSV.getFirstBlockAfterFork() - 1)).isEqualTo(BCH);
        }

        @Test
        void getChainForBlockHeight_first_forked_block() {
            assertThat(BSV.getChainForBlockHeight(BSV.getFirstBlockAfterFork())).isEqualTo(BSV);
        }

        @Test
        void getChainForBlockHeight_after_forked_block() {
            assertThat(BSV.getChainForBlockHeight(BSV.getFirstBlockAfterFork() + 1)).isEqualTo(BSV);
        }
    }

    @Nested
    @SuppressWarnings(CLASS_CAN_BE_STATIC)
    class BitcoinDiamond {
        @Test
        void enum_string_value() {
            assertThat(BCD).hasToString("BCD");
            assertThat(Chain.valueOf("BCD")).isEqualTo(BCD);
        }

        @Test
        void getName() {
            assertThat(BCD.getName()).isEqualTo("Bitcoin Diamond");
        }

        @Test
        void getFirstBlockAfterFork() {
            assertThat(BCD.getFirstBlockAfterFork()).isEqualTo(495_867);
        }

        @Test
        void getChainForBlockHeight_genesis() {
            assertThat(BCD.getChainForBlockHeight(0)).isEqualTo(BTC);
        }

        @Test
        void getChainForBlockHeight_before_forked_block() {
            assertThat(BCD.getChainForBlockHeight(BCD.getFirstBlockAfterFork() - 1)).isEqualTo(BTC);
        }

        @Test
        void getChainForBlockHeight_first_forked_block() {
            assertThat(BCD.getChainForBlockHeight(BCD.getFirstBlockAfterFork())).isEqualTo(BCD);
        }

        @Test
        void getChainForBlockHeight_after_forked_block() {
            assertThat(BCD.getChainForBlockHeight(BCD.getFirstBlockAfterFork() + 1)).isEqualTo(BCD);
        }
    }
}