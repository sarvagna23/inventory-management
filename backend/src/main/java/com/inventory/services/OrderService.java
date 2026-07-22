package com.inventory.services;

import com.inventory.entities.Order;
import com.inventory.entities.Order.OrderStatus;
import com.inventory.entities.OrderItem;
import com.inventory.entities.Product;
import com.inventory.repositories.OrderRepository;
import com.inventory.repositories.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = orderRepository.getTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Transactional
    public Order createOrder(Order order, List<OrderItem> items) {
        // Validate stock availability for all items first
        for (OrderItem item : items) {
            Product product = productService.getProductById(
                item.getProduct().getId()
            );
            if (product.getStockLevel() < item.getQuantity()) {
                throw new RuntimeException(
                    "Insufficient stock for product: " + product.getName() +
                    " — available: " + product.getStockLevel() +
                    " requested: " + item.getQuantity()
                );
            }
        }

        // Save order first to get ID
        Order savedOrder = orderRepository.save(order);

        // Process each item — deduct stock and save
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : items) {
            Product product = productService.getProductById(
                item.getProduct().getId()
            );
            item.setOrder(savedOrder);
            item.setUnitPrice(product.getPrice());
            orderItemRepository.save(item);

            // Deduct stock
            productService.updateStock(
                product.getId(),
                product.getStockLevel() - item.getQuantity()
            );

            totalAmount = totalAmount.add(item.getLineTotal());
        }

        // Update order total
        savedOrder.setTotalAmount(totalAmount);
        return orderRepository.save(savedOrder);
    }

    @Transactional
    public Order updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = getOrderById(id);
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}