package de.cotto.bitbook.backend.transaction.fullstackcash;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "fullstackCash", url = "https://api.fullstack.cash/")
@RateLimiter(name = "fullstackCash")
@CircuitBreaker(name = "fullstackCash")
public interface FullstackCashClient {
    @GetMapping("/v5/blockchain/getBlockCount")
    String getBlockHeight();
}
