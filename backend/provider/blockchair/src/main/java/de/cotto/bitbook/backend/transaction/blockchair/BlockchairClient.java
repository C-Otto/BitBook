package de.cotto.bitbook.backend.transaction.blockchair;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.TransactionHash;
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
    Optional<BlockchairTransactionDto> getTransaction(@PathVariable TransactionHash transactionHash);

    @GetMapping("/bitcoin/dashboards/address/{address}")
    Optional<BlockchairAddressTransactionsDto> getAddressDetails(@PathVariable Address address);

    @GetMapping("/{chainName}/stats")
    Optional<BlockchairBlockHeightDto> getBlockHeight(@PathVariable String chainName);
}
