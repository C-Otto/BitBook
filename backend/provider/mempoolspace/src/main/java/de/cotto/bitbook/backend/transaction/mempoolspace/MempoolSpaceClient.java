package de.cotto.bitbook.backend.transaction.mempoolspace;

import de.cotto.bitbook.backend.model.Address;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(value = "mempoolspace", url = "https://mempool.space/api")
@RateLimiter(name = "mempoolspace")
@CircuitBreaker(name = "mempoolspace")
public interface MempoolSpaceClient {
    @GetMapping("/address/{address}/txs/chain")
    Optional<MempoolSpaceAddressTransactionsDto> getAddressDetails(@PathVariable Address address);
}
