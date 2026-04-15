package com.electrodab.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.electrodab.api.api.ApiResponse;
import com.electrodab.api.model.Product;
import com.electrodab.api.service.ProductService;

import java.util.List;

import com.electrodab.api.model.ProductStockValue;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.success(productService.getAllProducts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody Product product) {
        Product created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Product updated = productService.updateProduct(id, product);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<Product>>> filterProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minQuantity) {
        return ResponseEntity.ok(ApiResponse.success(productService.filterProducts(category, minPrice, maxPrice, minQuantity)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Product>>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success(productService.searchProducts(keyword)));
        }
        return ResponseEntity.ok(ApiResponse.success(productService.searchProductsByName(name)));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(productService.getUniqueCategories()));
    }

    @GetMapping("/sort")
    public ResponseEntity<ApiResponse<List<Product>>> sortProducts(
            @RequestParam(required = false, defaultValue = "name") String by,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        return ResponseEntity.ok(ApiResponse.success(productService.sortProducts(by, direction)));
    }

    @GetMapping("/page")
    public ResponseEntity<ApiResponse<List<Product>>> getProductsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductsPage(page, size)));
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<ApiResponse<List<Product>>> getOutOfStockProducts() {
        return ResponseEntity.ok(ApiResponse.success(productService.getOutOfStockProducts()));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<Product>>> getLowStockProducts() {
        return ResponseEntity.ok(ApiResponse.success(productService.getLowStockProducts()));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Product>>> getAvailableProducts() {
        return ResponseEntity.ok(ApiResponse.success(productService.getAvailableProducts()));
    }

    @GetMapping("/stock-value")
    public ResponseEntity<ApiResponse<Double>> getStockValue() {
        return ResponseEntity.ok(ApiResponse.success(productService.getStockValue()));
    }

    @GetMapping("/stock-value/details")
    public ResponseEntity<ApiResponse<List<ProductStockValue>>> getProductStockValues() {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductStockValues()));
    }
}

