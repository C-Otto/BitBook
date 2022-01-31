package de.cotto.bitbook.backend.price.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface PriceRepository extends JpaRepository<PriceWithContextJpaDto, PriceWithContextId> {
}
