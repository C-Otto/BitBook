package de.cotto.bitbook.backend.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AddressWithDescriptionRepository extends JpaRepository<AddressWithDescriptionJpaDto, String> {
    Set<AddressWithDescriptionJpaDto> findByDescriptionContaining(String infix);
}
