package com.inventorylite.supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @Query("select s from Supplier s where lower(s.name) like :pattern escape '\\'")
    Page<Supplier> search(String pattern, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    // Queried here rather than through ProductService, which already depends on SupplierService.
    @Query("select count(p) > 0 from Product p where p.supplier.id = :supplierId")
    boolean hasProducts(Long supplierId);
}
