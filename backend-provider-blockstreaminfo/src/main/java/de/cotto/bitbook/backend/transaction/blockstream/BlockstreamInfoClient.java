package de.cotto.bitbook.backend.transaction.blockstream;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(value = "blockstreaminfo", url = "https://blockstream.info/api")
@RateLimiter(name = "blockstreaminfo")
@CircuitBreaker(name = "blockstreaminfo")
public interface BlockstreamInfoClient {
    @GetMapping("/address/{address}/txs")
    Optional<BlockstreamAddressTransactionsDto> getAddressDetails(@PathVariable String address);
}
