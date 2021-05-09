package de.cotto.bitbook.backend.transaction.blockchair;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(value = "blockchair", url = "https://api.blockchair.com")
@RateLimiter(name = "blockchair")
@CircuitBreaker(name = "blockchair")
public interface BlockchairClient {
    @GetMapping("/bitcoin/dashboards/transaction/{transactionHash}")
    Optional<BlockchairTransactionDto> getTransaction(@PathVariable String transactionHash);

    @GetMapping("/bitcoin/dashboards/address/{address}")
    Optional<BlockchairAddressTransactionsDto> getAddressDetails(@PathVariable String address);
}
