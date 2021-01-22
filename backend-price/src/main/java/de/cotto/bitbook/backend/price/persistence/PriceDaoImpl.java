package de.cotto.bitbook.backend.price.persistence;

import de.cotto.bitbook.backend.price.PriceDao;
import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceWithDate;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDate;
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
    public Optional<Price> getPrice(LocalDate date) {
        return priceRepository.findById(date)
                .map(PriceWithDateJpaDto::toModel)
                .map(PriceWithDate::getPrice);
    }

    @Override
    public void savePrices(Collection<PriceWithDate> prices) {
        List<PriceWithDateJpaDto> dtos = prices.stream().map(PriceWithDateJpaDto::fromModel).collect(toList());
        priceRepository.saveAll(dtos);
    }
}
