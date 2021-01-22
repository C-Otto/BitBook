package de.cotto.bitbook.backend.transaction.btccom;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(value = "btccom", url = "https://chain.api.btc.com")
@RateLimiter(name = "btccom")
@CircuitBreaker(name = "btccom")
public interface BtcComClient {
    @GetMapping("/v3/tx/{transactionHash}?verbose=2")
    Optional<BtcComTransactionDto> getTransaction(@PathVariable String transactionHash);

    @GetMapping("/v3/address/{address}/tx")
    Optional<BtcComAddressTransactionsDto> getAddressDetails(@PathVariable String address);
}
