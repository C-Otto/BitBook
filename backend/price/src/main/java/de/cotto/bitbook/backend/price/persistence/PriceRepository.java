package de.cotto.bitbook.backend.price.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

interface PriceRepository extends JpaRepository<PriceWithDateJpaDto, LocalDate> {
}
