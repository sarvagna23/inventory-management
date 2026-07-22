package com.inventory.repositories;

import com.inventory.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Derived query — Spring generates SQL from method name
    List<Product> findByCategory(String category);

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    // Custom JPQL query — products where stock is at or below reorder threshold
    @Query("SELECT p FROM Product p WHERE p.stockLevel <= p.reorderThreshold")
    List<Product> findLowStockProducts();

    // Find by supplier
    List<Product> findBySupplierId(Long supplierId);

    // Search by name containing keyword (case insensitive)
    List<Product> findByNameContainingIgnoreCase(String keyword);
}