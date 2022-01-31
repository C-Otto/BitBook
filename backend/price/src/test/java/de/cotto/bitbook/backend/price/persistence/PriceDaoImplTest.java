package de.cotto.bitbook.backend.price.persistence;

import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceContext;
import de.cotto.bitbook.backend.price.model.PriceWithContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PriceDaoImplTest {
    private static final LocalDate DATE = LocalDate.of(2010, 1, 1);
    private static final PriceContext PRICE_CONTEXT = new PriceContext(DATE, BTC);

    @InjectMocks
    private PriceDaoImpl priceDao;

    @Mock
    private PriceRepository priceRepository;

    @Test
    void getPrice() {
        Price price = Price.of(123);
        PriceWithContext priceWithContext = new PriceWithContext(price, PRICE_CONTEXT);
        doReturn(Optional.of(PriceWithContextJpaDto.fromModel(priceWithContext)))
                .when(priceRepository).findById(PriceWithContextId.fromModel(PRICE_CONTEXT));
        assertThat(priceDao.getPrice(PRICE_CONTEXT)).contains(price);
    }

    @Test
    void savePrices() {
        Price price1 = Price.of(1);
        Price price2 = Price.of(21);
        Price price3 = Price.of(42);
        LocalDate date1 = DATE;
        LocalDate date2 = DATE.plusDays(1);
        LocalDate date3 = DATE.plusWeeks(5);
        Collection<PriceWithContext> pricesWithContexts = List.of(
                new PriceWithContext(price1, new PriceContext(date1, BTC)),
                new PriceWithContext(price2, new PriceContext(date2, BTC)),
                new PriceWithContext(price3, new PriceContext(date3, BTC))
        );

        priceDao.savePrices(pricesWithContexts);

        PriceWithContextJpaDto dto1 = dto(price1, date1);
        PriceWithContextJpaDto dto2 = dto(price2, date2);
        PriceWithContextJpaDto dto3 = dto(price3, date3);
        verify(priceRepository).saveAll(iterableWithDtos(dto1, dto2, dto3));
    }

    private List<PriceWithContextJpaDto> iterableWithDtos(PriceWithContextJpaDto... dtos) {
        return argThat(iterable -> {
            Set<PriceWithContext> fromIterable = iterable.stream()
                    .map(PriceWithContextJpaDto::toModel)
                    .collect(toSet());
            Set<PriceWithContext> expected = Arrays.stream(dtos)
                    .map(PriceWithContextJpaDto::toModel)
                    .collect(toSet());
            return fromIterable.equals(expected);
        });
    }

    private PriceWithContextJpaDto dto(Price price, LocalDate date) {
        PriceWithContextJpaDto dto = new PriceWithContextJpaDto();
        dto.setDate(date);
        dto.setPrice(PriceJpaDto.fromModel(price));
        dto.setChain(BTC.toString());
        return dto;
    }
}