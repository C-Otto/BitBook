package de.cotto.bitbook.backend.transaction.blockchaininfo;

import de.cotto.bitbook.backend.model.TransactionHash;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(value = "blockchainInfo", url = "https://blockchain.info/")
@RateLimiter(name = "blockchainInfo")
@CircuitBreaker(name = "blockchainInfo")
public interface BlockchainInfoClient {
    @GetMapping("/q/getblockcount")
    String getBlockHeight();

    @GetMapping("/rawtx/{transactionHash}")
    Optional<BlockchainInfoTransactionDto> getTransaction(@PathVariable TransactionHash transactionHash);
}
