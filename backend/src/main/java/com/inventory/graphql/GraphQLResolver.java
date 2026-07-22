package com.inventory.graphql;

import com.inventory.entities.Order;
import com.inventory.entities.Product;
import com.inventory.entities.Supplier;
import com.inventory.services.OrderService;
import com.inventory.services.ProductService;
import com.inventory.services.SupplierService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Controller
@Transactional
public class GraphQLResolver {

    private final ProductService productService;
    private final SupplierService supplierService;
    private final OrderService orderService;

    public GraphQLResolver(
            ProductService productService,
            SupplierService supplierService,
            OrderService orderService) {
        this.productService = productService;
        this.supplierService = supplierService;
        this.orderService = orderService;
    }

    @QueryMapping
    public List<Product> products() {
        System.out.println("products() called");
        return productService.getAllProducts();
    }

    @QueryMapping
    public Product product(@Argument Long id) {
        return productService.getProductById(id);
    }

    @QueryMapping
    public Product productBySku(@Argument String sku) {
        return productService.getProductBySku(sku);
    }

    @QueryMapping
    public List<Product> productsByCategory(@Argument String category) {
        return productService.getProductsByCategory(category);
    }

    @QueryMapping
    public List<Product> lowStockProducts() {
        return productService.getLowStockProducts();
    }

    @QueryMapping
    public List<Product> searchProducts(@Argument String keyword) {
        return productService.searchProducts(keyword);
    }

    @QueryMapping
    public List<Supplier> suppliers() {
        return supplierService.getAllSuppliers();
    }

    @QueryMapping
    public Supplier supplier(@Argument Long id) {
        return supplierService.getSupplierById(id);
    }

    @QueryMapping
    public List<Order> orders() {
        return orderService.getAllOrders();
    }

    @QueryMapping
    public Order order(@Argument Long id) {
        return orderService.getOrderById(id);
    }

    @MutationMapping
    public Product updateStock(@Argument Long id, @Argument Integer stockLevel) {
        return productService.updateStock(id, stockLevel);
    }
}
