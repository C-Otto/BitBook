package de.cotto.bitbook.backend.transaction.sochain;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(value = "sochain", url = "https://sochain.com/api/v2")
@RateLimiter(name = "sochain")
@CircuitBreaker(name = "sochain")
public interface SoChainClient {
    @GetMapping("/address/bitcoin/{address}")
    Optional<SoChainAddressTransactionsDto> getAddressDetails(@PathVariable String address);
}
