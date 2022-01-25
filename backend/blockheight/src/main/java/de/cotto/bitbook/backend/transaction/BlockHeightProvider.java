package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.Provider;
import de.cotto.bitbook.backend.model.Chain;

public interface BlockHeightProvider extends Provider<Chain, Integer> {
}
