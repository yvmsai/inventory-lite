package com.inventorylite.stock;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @EntityGraph(attributePaths = {"product", "createdBy"})
    @Query("select m from StockMovement m where m.product.id = :productId order by m.createdAt desc, m.id desc")
    Page<StockMovement> findHistory(Long productId, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "createdBy"})
    @Query("select m from StockMovement m order by m.createdAt desc, m.id desc")
    List<StockMovement> findRecent(Pageable pageable);
}
