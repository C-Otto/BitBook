package de.cotto.bitbook.backend.price.kraken;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(value = "kraken", url = "https://api.kraken.com/")
@RateLimiter(name = "kraken")
@CircuitBreaker(name = "kraken")
public interface KrakenClient {
    @GetMapping("/0/public/Trades?pair=BTCEUR&since={sinceEpochSeconds}")
    Optional<KrakenTradesDto> getTrades(@PathVariable long sinceEpochSeconds);

    @GetMapping("/0/public/OHLC?pair=BTCEUR&interval=1440")
    Optional<KrakenOhlcDataDto> getOhlcData();
}
