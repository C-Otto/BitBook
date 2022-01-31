package de.cotto.bitbook.backend.price.persistence;

import de.cotto.bitbook.backend.price.PriceDao;
import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceContext;
import de.cotto.bitbook.backend.price.model.PriceWithContext;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Component
@Transactional
public class PriceDaoImpl implements PriceDao {
    private final PriceRepository priceRepository;

    public PriceDaoImpl(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    @Override
    public Optional<Price> getPrice(PriceContext priceContext) {
        return priceRepository.findById(PriceWithContextId.fromModel(priceContext))
                .map(PriceWithContextJpaDto::toModel)
                .map(PriceWithContext::getPrice);
    }

    @Override
    public void savePrices(Collection<PriceWithContext> prices) {
        List<PriceWithContextJpaDto> dtos = prices.stream().map(PriceWithContextJpaDto::fromModel).collect(toList());
        priceRepository.saveAll(dtos);
    }
}
