package de.cotto.bitbook.backend.transaction.blockcypher;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(value = "blockcypher", url = "https://api.blockcypher.com")
@RateLimiter(name = "blockcypher")
@CircuitBreaker(name = "blockcypher")
public interface BlockcypherClient {
    @GetMapping("/v1/btc/main/txs/{transactionHash}")
    Optional<BlockcypherTransactionDto> getTransaction(@PathVariable String transactionHash);

    @GetMapping("/v1/btc/main/addrs/{address}?limit=2000&confirmations=1")
    Optional<BlockcypherAddressTransactionsDto> getAllAddressDetails(@PathVariable String address);

    @GetMapping("/v1/btc/main/addrs/{address}?before={before}&limit=2000")
    Optional<BlockcypherAddressTransactionsDto> getAddressDetailsBefore(
            @PathVariable String address,
            @PathVariable int before
    );

    @GetMapping("/v1/btc/main/addrs/{address}?after={highestBlockHeight}&limit=2000&confirmations=1")
    Optional<BlockcypherAddressTransactionsDto> getAddressDetailsAfter(
            @PathVariable String address,
            @PathVariable int highestBlockHeight
    );

    @GetMapping("/v1/btc/main/addrs/{address}?after={after}&before={before}&limit=2000")
    Optional<BlockcypherAddressTransactionsDto> getAddressDetailsBetween(
            @PathVariable String address,
            @PathVariable int after,
            @PathVariable int before
    );
}
