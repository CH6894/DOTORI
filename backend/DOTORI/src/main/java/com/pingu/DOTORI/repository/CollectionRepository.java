package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByUserId(Long userId);
    Optional<Collection> findFirstByUserId(Long userId);
}
