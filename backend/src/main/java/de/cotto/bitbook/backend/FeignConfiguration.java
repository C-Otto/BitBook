package de.cotto.bitbook.backend;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients
public class FeignConfiguration {
    public FeignConfiguration() {
        // default constructor
    }
}
