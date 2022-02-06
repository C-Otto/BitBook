package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.Provider;

public interface BlockHeightProvider extends Provider<Chain, Integer> {
}
