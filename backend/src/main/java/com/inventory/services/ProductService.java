package com.inventory.services;

import com.inventory.entities.Product;
import com.inventory.entities.Supplier;
import com.inventory.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final SupplierService supplierService;

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.anon-key:}")
    private String supabaseKey;

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    @Transactional
    public Product createProduct(Product product, Long supplierId) {
        if (productRepository.existsBySku(product.getSku())) {
            throw new RuntimeException("Product with SKU already exists: " + product.getSku());
        }
        if (supplierId != null) {
            Supplier supplier = supplierService.getSupplierById(supplierId);
            product.setSupplier(supplier);
        }
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
        Product existing = getProductById(id);
        existing.setName(updatedProduct.getName());
        existing.setCategory(updatedProduct.getCategory());
        existing.setDescription(updatedProduct.getDescription());
        existing.setPrice(updatedProduct.getPrice());
        existing.setReorderThreshold(updatedProduct.getReorderThreshold());
        return productRepository.save(existing);
    }

    @Transactional
    public Product updateStock(Long id, Integer newStockLevel) {
        Product product = getProductById(id);
        product.setStockLevel(newStockLevel);
        Product saved = productRepository.save(product);

        if (saved.isLowStock()) {
            System.out.println("LOW STOCK ALERT: " + saved.getName() +
                " — stock: " + saved.getStockLevel() +
                " threshold: " + saved.getReorderThreshold());
        }

        syncToSupabase(saved);
        return saved;
    }

    private void syncToSupabase(Product product) {
        try {
            if (supabaseUrl == null || supabaseUrl.isEmpty()) return;

            String json = String.format(
                "{\"id\":%d,\"name\":\"%s\",\"sku\":\"%s\",\"category\":\"%s\",\"stock_level\":%d,\"reorder_threshold\":%d,\"low_stock\":%b}",
                product.getId(), product.getName(), product.getSku(),
                product.getCategory(), product.getStockLevel(),
                product.getReorderThreshold(), product.isLowStock()
            );

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(supabaseUrl + "/rest/v1/products"))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .header("Content-Type", "application/json")
                .header("Prefer", "resolution=merge-duplicates")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                .build();

            java.net.http.HttpResponse<String> response = client.send(
                request, java.net.http.HttpResponse.BodyHandlers.ofString());
            System.out.println("Synced to Supabase: " + product.getName() + " status: " + response.statusCode());
        } catch (Exception e) {
            System.out.println("Supabase sync failed (non-critical): " + e.getMessage());
        }
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }
}
