package de.cotto.bitbook.backend.transaction.bitaps;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(value = "bitaps", url = "https://api.bitaps.com")
@RateLimiter(name = "bitaps")
@CircuitBreaker(name = "bitaps")
public interface BitapsClient {
    @GetMapping("/btc/v1/blockchain/transaction/{transactionHash}")
    Optional<BitapsTransactionDto> getTransaction(@PathVariable String transactionHash);

    @GetMapping("/btc/v1/blockchain/block/last")
    Optional<BitapsBlockHeightDto> getBlockHeight();

    @GetMapping("/btc/v1/blockchain/address/transactions/{address}")
    Optional<BitapsAddressTransactionsDto> getAddressTransactions(@PathVariable String address);
}
