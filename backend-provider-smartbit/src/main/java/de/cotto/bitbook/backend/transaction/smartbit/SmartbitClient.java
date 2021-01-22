package de.cotto.bitbook.backend.transaction.smartbit;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(value = "smartbit", url = "https://api.smartbit.com.au")
@RateLimiter(name = "smartbit")
@CircuitBreaker(name = "smartbit")
public interface SmartbitClient {
    @GetMapping("/v1/blockchain/tx/{transactionHash}")
    Optional<SmartbitTransactionDto> getTransaction(@PathVariable String transactionHash);

    @GetMapping("/v1/blockchain/address/{address}?limit=1000")
    Optional<SmartbitAddressTransactionsDto> getAddressTransactions(@PathVariable String address);
}
