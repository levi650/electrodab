package com.electrodab.api.service;

import org.springframework.stereotype.Service;
import com.electrodab.api.exception.DuplicateIdException;
import com.electrodab.api.exception.ResourceNotFoundException;
import com.electrodab.api.model.Product;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.electrodab.api.model.ProductStockValue;

@Service
public class ProductService {

    private final List<Product> products = new ArrayList<>();

    @PostConstruct
    public void initProducts() {
        products.add(new Product(1L, "Laptop", "Informatique", 5000, 5));
        products.add(new Product(2L, "Souris", "Informatique", 100, 20));
        products.add(new Product(3L, "Chaise", "Mobilier", 800, 10));
        products.add(new Product(4L, "Table", "Mobilier", 1500, 2));
        products.add(new Product(5L, "Casque", "Informatique", 300, 15));
        products.add(new Product(6L, "Stylo", "Bureau", 5, 0));
        products.add(new Product(7L, "Cahier", "Bureau", 15, 4));
        products.add(new Product(8L, "Imprimante", "Electronique", 1200, 3));
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public Product getProductById(Long id) {
        return findProductById(id);
    }

    public Product createProduct(Product product) {
        validateProductPayload(product, true);

        if (containsId(product.getId())) {
            throw new DuplicateIdException("Product id already exists: " + product.getId());
        }

        Product newProduct = new Product(product.getId(), product.getName(), product.getCategory(), product.getPrice(), product.getQuantity());
        products.add(newProduct);
        return newProduct;
    }

    public Product updateProduct(Long id, Product product) {
        Product existing = findProductById(id);
        validateProductPayload(product, false);

        if (product.getId() != null && !product.getId().equals(id)) {
            throw new IllegalArgumentException("Product id in payload must match path id");
        }

        existing.setName(product.getName());
        existing.setCategory(product.getCategory());
        existing.setPrice(product.getPrice());
        existing.setQuantity(product.getQuantity());

        return existing;
    }

    public void deleteProduct(Long id) {
        Product existing = findProductById(id);
        products.remove(existing);
    }

    public List<Product> filterProducts(String category, Double minPrice, Double maxPrice, Integer minQuantity) {
        if (minPrice != null && minPrice < 0) {
            throw new IllegalArgumentException("minPrice must be greater than or equal to 0");
        }
        if (maxPrice != null && maxPrice < 0) {
            throw new IllegalArgumentException("maxPrice must be greater than or equal to 0");
        }
        if (minQuantity != null && minQuantity < 0) {
            throw new IllegalArgumentException("minQuantity must be greater than or equal to 0");
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("minPrice must be less than or equal to maxPrice");
        }

        return products.stream()
                .filter(product -> filterByCategory(product, category))
                .filter(product -> filterByMinPrice(product, minPrice))
                .filter(product -> filterByMaxPrice(product, maxPrice))
                .filter(product -> filterByMinQuantity(product, minQuantity))
                .collect(Collectors.toList());
    }

    public List<Product> searchProductsByName(String name) {
        if (name == null || name.isBlank()) {
            return getAllProducts();
        }

        String lowered = name.toLowerCase(Locale.ROOT);
        return products.stream()
                .filter(product -> product.getName().toLowerCase(Locale.ROOT).contains(lowered))
                .collect(Collectors.toList());
    }

    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllProducts();
        }

        String lowered = keyword.toLowerCase(Locale.ROOT);
        return products.stream()
                .filter(product -> product.getName().toLowerCase(Locale.ROOT).contains(lowered)
                        || product.getCategory().toLowerCase(Locale.ROOT).contains(lowered))
                .collect(Collectors.toList());
    }

    public List<String> getUniqueCategories() {
        return products.stream()
                .map(Product::getCategory)
                .filter(category -> category != null && !category.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    public List<Product> sortProducts(String by, String direction) {
        String sortBy = by == null || by.isBlank() ? "name" : by.toLowerCase(Locale.ROOT);
        String sortDirection = direction == null || direction.isBlank() ? "asc" : direction.toLowerCase(Locale.ROOT);

        Comparator<Product> comparator;
        switch (sortBy) {
            case "price":
                comparator = Comparator.comparingDouble(Product::getPrice);
                break;
            case "quantity":
                comparator = Comparator.comparingInt(Product::getQuantity);
                break;
            case "category":
                comparator = Comparator.comparing(Product::getCategory, String.CASE_INSENSITIVE_ORDER);
                break;
            case "name":
                comparator = Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            default:
                throw new IllegalArgumentException("Invalid sort field: " + by);
        }

        if ("desc".equals(sortDirection)) {
            comparator = comparator.reversed();
        } else if (!"asc".equals(sortDirection)) {
            throw new IllegalArgumentException("Invalid sort direction: " + direction);
        }

        return products.stream().sorted(comparator).collect(Collectors.toList());
    }

    public List<Product> getProductsPage(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be 0 or greater");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }

        int fromIndex = page * size;
        if (fromIndex >= products.size()) {
            return new ArrayList<>();
        }

        int toIndex = Math.min(fromIndex + size, products.size());
        return new ArrayList<>(products.subList(fromIndex, toIndex));
    }

    public List<Product> getOutOfStockProducts() {
        return products.stream()
                .filter(product -> product.getQuantity() == 0)
                .collect(Collectors.toList());
    }

    public List<Product> getLowStockProducts() {
        return products.stream()
                .filter(product -> product.getQuantity() < 5)
                .collect(Collectors.toList());
    }

    public List<Product> getAvailableProducts() {
        return products.stream()
                .filter(product -> product.getQuantity() > 0)
                .collect(Collectors.toList());
    }

    public double getStockValue() {
        return products.stream()
                .mapToDouble(product -> product.getPrice() * product.getQuantity())
                .sum();
    }

    public List<ProductStockValue> getProductStockValues() {
        return products.stream()
                .map(product -> new ProductStockValue(
                        product.getId(),
                        product.getName(),
                        product.getCategory(),
                        product.getPrice(),
                        product.getQuantity(),
                        product.getPrice() * product.getQuantity()))
                .collect(Collectors.toList());
    }

    private Product findProductById(Long id) {
        return products.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    private boolean containsId(Long id) {
        return products.stream().anyMatch(product -> product.getId().equals(id));
    }

    private void validateProductPayload(Product product, boolean requiresId) {
        if (product == null) {
            throw new IllegalArgumentException("Product payload is required");
        }
        if (requiresId && product.getId() == null) {
            throw new IllegalArgumentException("Product id is required");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getCategory() == null || product.getCategory().isBlank()) {
            throw new IllegalArgumentException("Product category is required");
        }
        if (product.getName().length() > 100) {
            throw new IllegalArgumentException("Product name must be 100 characters or fewer");
        }
        validatePriceAndQuantity(product.getPrice(), product.getQuantity());
    }

    private void validatePriceAndQuantity(double price, int quantity) {
        if (price < 0) {
            throw new IllegalArgumentException("price must be greater than or equal to 0");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must be greater than or equal to 0");
        }
    }

    private boolean filterByCategory(Product product, String category) {
        return category == null || category.isBlank() || product.getCategory().equalsIgnoreCase(category);
    }

    private boolean filterByMinPrice(Product product, Double minPrice) {
        return minPrice == null || product.getPrice() >= minPrice;
    }

    private boolean filterByMaxPrice(Product product, Double maxPrice) {
        return maxPrice == null || product.getPrice() <= maxPrice;
    }

    private boolean filterByMinQuantity(Product product, Integer minQuantity) {
        return minQuantity == null || product.getQuantity() >= minQuantity;
    }
}

