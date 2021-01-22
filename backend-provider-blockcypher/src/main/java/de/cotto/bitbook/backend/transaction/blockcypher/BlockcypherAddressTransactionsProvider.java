package de.cotto.bitbook.backend.transaction.blockcypher;

import de.cotto.bitbook.backend.Provider;
import de.cotto.bitbook.backend.transaction.TransactionsRequestKey;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BlockcypherAddressTransactionsProvider implements Provider<TransactionsRequestKey, AddressTransactions> {
    private final BlockcypherClient blockcypherClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BlockcypherAddressTransactionsProvider(BlockcypherClient blockcypherClient) {
        this.blockcypherClient = blockcypherClient;
    }

    @Override
    public String getName() {
        return "BlockcypherAddressTransactionsProvider";
    }

    @Override
    @RateLimiter(name = "blockcypher2")
    public Optional<AddressTransactions> get(TransactionsRequestKey transactionsRequestKey) {
        if (transactionsRequestKey.hasKnownAddressTransactions()) {
            return Optional.of(getUpdated(transactionsRequestKey));
        }
        List<BlockcypherAddressTransactionsDto> responses =
                getAllTransactionsAfter(transactionsRequestKey.getAddress(), 0);
        return toModel(responses, transactionsRequestKey.getBlockHeight(), transactionsRequestKey.getAddress());
    }

    private AddressTransactions getUpdated(TransactionsRequestKey transactionsRequestKey) {
        AddressTransactions addressTransactions = transactionsRequestKey.getAddressTransactions();
        String address = addressTransactions.getAddress();
        int lastCheckedAtBlockHeight = addressTransactions.getLastCheckedAtBlockHeight();
        List<BlockcypherAddressTransactionsDto> addressDetailsAfter =
                getAllTransactionsAfter(address, lastCheckedAtBlockHeight);
        return toModel(addressDetailsAfter, transactionsRequestKey.getBlockHeight(), address)
                .map(addressTransactions::getCombined)
                .orElse(addressTransactions);
    }

    private List<BlockcypherAddressTransactionsDto> getAllTransactionsAfter(String address, int after) {
        List<BlockcypherAddressTransactionsDto> responses = new ArrayList<>();
        int before = Integer.MAX_VALUE;
        boolean requestMore = true;
        while (requestMore) {
            Optional<BlockcypherAddressTransactionsDto> response = getFromApi(address, after, before);
            if (response.isPresent()) {
                BlockcypherAddressTransactionsDto dto = response.get();
                responses.add(dto);
                if (dto.isIncomplete()) {
                    before = dto.getLowestCompletedBlockHeight();
                } else {
                    requestMore = false;
                }
            } else {
                requestMore = false;
            }
        }
        return responses;
    }

    private Optional<BlockcypherAddressTransactionsDto> getFromApi(String address, int after, int before) {
        logger.debug("Contacting Blockcypher API for transactions for address {}", address);
        if (before == Integer.MAX_VALUE && after == 0) {
            return blockcypherClient.getAllAddressDetails(address);
        }
        int atLeast = after + 1;
        if (before == Integer.MAX_VALUE) {
            logger.debug("(atLeast {})", atLeast);
            return blockcypherClient.getAddressDetailsAfter(address, atLeast);
        }
        if (after == 0) {
            logger.debug("(before {})", before);
            return blockcypherClient.getAddressDetailsBefore(address, before);
        }
        logger.debug("(between {} and {})", atLeast, before);
        return blockcypherClient.getAddressDetailsBetween(address, atLeast, before);
    }

    private Optional<AddressTransactions> toModel(
            List<BlockcypherAddressTransactionsDto> dtos,
            int currentBlockHeight,
            String expectedAddress
    ) {
        return dtos.stream()
                .reduce(BlockcypherAddressTransactionsDto::combine)
                .map(dto -> dto.toModel(currentBlockHeight, expectedAddress));
    }
}
