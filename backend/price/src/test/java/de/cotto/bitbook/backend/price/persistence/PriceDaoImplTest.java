package de.cotto.bitbook.backend.price.persistence;

import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceWithDate;
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

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PriceDaoImplTest {
    private static final LocalDate DATE = LocalDate.of(2010, 1, 1);

    @InjectMocks
    private PriceDaoImpl priceDao;

    @Mock
    private PriceRepository priceRepository;

    @Test
    void getPrice() {
        Price price = Price.of(123);
        PriceWithDate priceWithDate = new PriceWithDate(price, DATE);
        doReturn(Optional.of(PriceWithDateJpaDto.fromModel(priceWithDate))).when(priceRepository).findById(DATE);
        assertThat(priceDao.getPrice(DATE)).contains(price);
    }

    @Test
    void savePrices() {
        Price price1 = Price.of(1);
        Price price2 = Price.of(21);
        Price price3 = Price.of(42);
        LocalDate date1 = DATE;
        LocalDate date2 = DATE.plusDays(1);
        LocalDate date3 = DATE.plusWeeks(5);
        Collection<PriceWithDate> pricesWithDates = List.of(
                new PriceWithDate(price1, date1),
                new PriceWithDate(price2, date2),
                new PriceWithDate(price3, date3)
        );

        priceDao.savePrices(pricesWithDates);

        PriceWithDateJpaDto dto1 = dto(price1, date1);
        PriceWithDateJpaDto dto2 = dto(price2, date2);
        PriceWithDateJpaDto dto3 = dto(price3, date3);
        verify(priceRepository).saveAll(iterableWithDtos(dto1, dto2, dto3));
    }

    private List<PriceWithDateJpaDto> iterableWithDtos(PriceWithDateJpaDto... dtos) {
        return argThat(iterable -> {
            Set<PriceWithDate> fromIterable = iterable.stream().map(PriceWithDateJpaDto::toModel).collect(toSet());
            Set<PriceWithDate> expected = Arrays.stream(dtos).map(PriceWithDateJpaDto::toModel).collect(toSet());
            return fromIterable.equals(expected);
        });
    }

    private PriceWithDateJpaDto dto(Price price, LocalDate date) {
        PriceWithDateJpaDto dto = new PriceWithDateJpaDto();
        dto.setDate(date);
        dto.setPrice(PriceJpaDto.fromModel(price));
        return dto;
    }
}