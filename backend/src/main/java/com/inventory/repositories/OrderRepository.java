package com.inventory.repositories;

import com.inventory.entities.Order;
import com.inventory.entities.Order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCustomerEmail(String email);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Total revenue from all delivered orders
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal getTotalRevenue();

    // Count orders by status
    long countByStatus(OrderStatus status);
}