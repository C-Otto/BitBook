package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.lnd.features.PoolLeasesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.lnd.model.PoolLeaseFixtures.POOL_LEASE;
import static de.cotto.bitbook.lnd.model.PoolLeaseFixtures.POOL_LEASE_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PoolServiceTest {
    private PoolService poolService;

    @Mock
    private PoolLeasesService poolLeasesService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper =
                new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        poolService = new PoolService(objectMapper, poolLeasesService);
    }

    @Nested
    class AddFromLeases {
        @Test
        void empty_json() {
            assertFailure("");
        }

        @Test
        void not_json() {
            assertFailure("-x--");
        }

        @Test
        void empty_json_object() {
            assertFailure("{}");
        }

        @Test
        void no_leases() {
            assertFailure("{\"hello\": 2}");
        }

        @Test
        void empty_array() {
            String json = "{\"leases\":[]}";
            assertFailure(json);
        }

        @Test
        void success() {
            when(poolLeasesService.addFromLeases(Set.of(POOL_LEASE, POOL_LEASE_2))).thenReturn(2L);
            String json = "{\"leases\":[" +
                          "{" +
                          "\"channel_point\": \"" + TRANSACTION_HASH + ":0\", " +
                          "\"channel_node_key\": \"pubkey\", " +
                          "\"premium_sat\": 1500, " +
                          "\"execution_fee_sat\": 150, " +
                          "\"chain_fee_sat\": 114, " +
                          "\"purchased\": false" +
                          "}," +
                          "{" +
                          "\"channel_point\": \"" + TRANSACTION_HASH_2 + ":1\", " +
                          "\"channel_node_key\": \"pubkey\", " +
                          "\"premium_sat\": 1000, " +
                          "\"execution_fee_sat\": 200, " +
                          "\"chain_fee_sat\": 50, " +
                          "\"purchased\": false" +
                          "}" +
                          "]}";
            assertThat(poolService.addFromLeases(json)).isEqualTo(2);
        }

        @Test
        void ignores_bid_leases() {
            String json = "{\"leases\":[" +
                          "{" +
                          "\"channel_point\": \"" + TRANSACTION_HASH_2 + ":1\", " +
                          "\"channel_node_key\": \"pubkey\", " +
                          "\"premium_sat\": 1000, " +
                          "\"execution_fee_sat\": 200, " +
                          "\"chain_fee_sat\": 50, " +
                          "\"purchased\": true" +
                          "}" +
                          "]}";
            poolService.addFromLeases(json);
            verify(poolLeasesService).addFromLeases(Set.of());
        }

        private void assertFailure(String json) {
            assertThat(poolService.addFromLeases(json)).isEqualTo(0);
        }
    }
}