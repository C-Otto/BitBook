package de.cotto.bitbook.backend;

import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.model.TransactionWithDescription;

public interface TransactionWithDescriptionDao extends DescriptionDao<TransactionHash, TransactionWithDescription> {
}
