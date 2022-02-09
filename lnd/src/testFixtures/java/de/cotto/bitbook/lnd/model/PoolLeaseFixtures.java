package de.cotto.bitbook.lnd.model;

import de.cotto.bitbook.backend.model.Coins;

import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_2;

public class PoolLeaseFixtures {
    public static final String PUBKEY = "pubkey";
    public static final PoolLease POOL_LEASE = new PoolLease(
            TRANSACTION_HASH,
            0,
            PUBKEY,
            Coins.ofSatoshis(1500),
            Coins.ofSatoshis(150),
            Coins.ofSatoshis(114)
    );
    public static final PoolLease POOL_LEASE_2 = new PoolLease(
            TRANSACTION_HASH_2,
            1,
            PUBKEY,
            Coins.ofSatoshis(1000),
            Coins.ofSatoshis(200),
            Coins.ofSatoshis(50)
    );
}
