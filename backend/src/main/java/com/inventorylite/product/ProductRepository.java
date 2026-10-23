package com.inventorylite.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "supplier")
    @Query("""
            select p from Product p
            where (:supplierId is null or p.supplier.id = :supplierId)
              and (:pattern is null or lower(p.name) like :pattern escape '\\' or lower(p.sku) like :pattern escape '\\')
            """)
    Page<Product> search(String pattern, Long supplierId, Pageable pageable);

    @EntityGraph(attributePaths = "supplier")
    Optional<Product> findWithSupplierById(Long id);

    /** Row lock (SELECT ... FOR UPDATE) so concurrent stock movements on one product are serialized. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findForUpdate(Long id);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    // Queried here rather than through StockMovementService, which already depends on ProductService.
    @Query("select count(m) > 0 from StockMovement m where m.product.id = :productId")
    boolean hasMovements(Long productId);

    @Query("select coalesce(sum(p.quantity * p.unitPrice), 0) from Product p")
    BigDecimal totalStockValue();

    @Query("select count(p) from Product p where p.quantity <= p.reorderLevel")
    long countLowStock();

    @EntityGraph(attributePaths = "supplier")
    @Query("select p from Product p where p.quantity <= p.reorderLevel order by p.quantity asc, p.name asc")
    List<Product> findLowStock(Pageable pageable);
}
