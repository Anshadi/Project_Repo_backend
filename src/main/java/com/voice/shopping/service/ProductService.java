package com.voice.shopping.service;

import com.voice.shopping.model.Product;
import com.voice.shopping.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Value("${app.search.max-results}")
    private Integer maxResults;

    public List<Product> searchProducts(String query, String brand, Double maxPrice) {
        log.debug("Searching products with query: {}, brand: {}, maxPrice: {}", query, brand, maxPrice);

        List<Product> products;

        if (query == null || query.trim().isEmpty()) {
            // Return all products if no query
            products = productRepository.findByInStockTrue();
        } else {
            // Enhanced intelligent search
            products = performIntelligentSearch(query, brand, maxPrice);
        }

        // Limit results
        return products.stream()
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    private List<Product> performIntelligentSearch(String query, String brand, Double maxPrice) {
        String normalizedQuery = query.toLowerCase().trim();
        
        // Category-based search patterns
        if (normalizedQuery.contains("meat") || normalizedQuery.contains("protein") || 
            normalizedQuery.contains("chicken") || normalizedQuery.contains("beef") || 
            normalizedQuery.contains("pork") || normalizedQuery.contains("fish")) {
            return searchByCategory("Meat", brand, maxPrice);
        } else if (normalizedQuery.contains("dairy") || normalizedQuery.contains("milk") || 
                   normalizedQuery.contains("cheese") || normalizedQuery.contains("yogurt") || 
                   normalizedQuery.contains("butter")) {
            return searchByCategory("Dairy", brand, maxPrice);
        } else if (normalizedQuery.contains("vegetable") || normalizedQuery.contains("veggie") || 
                   normalizedQuery.contains("green") || normalizedQuery.contains("fresh produce")) {
            return searchByCategory("Vegetables", brand, maxPrice);
        } else if (normalizedQuery.contains("fruit") || normalizedQuery.contains("apple") || 
                   normalizedQuery.contains("banana") || normalizedQuery.contains("orange")) {
            return searchByCategory("Fruits", brand, maxPrice);
        } else if (normalizedQuery.contains("bakery") || normalizedQuery.contains("bread") || 
                   normalizedQuery.contains("baked") || normalizedQuery.contains("pastry")) {
            return searchByCategory("Bakery", brand, maxPrice);
        } else if (normalizedQuery.contains("beverage") || normalizedQuery.contains("drink") || 
                   normalizedQuery.contains("juice") || normalizedQuery.contains("soda")) {
            return searchByCategory("Beverages", brand, maxPrice);
        } else if (normalizedQuery.contains("snack") || normalizedQuery.contains("chip") || 
                   normalizedQuery.contains("cookie") || normalizedQuery.contains("candy")) {
            return searchByCategory("Snacks", brand, maxPrice);
        } else if (normalizedQuery.startsWith("show me ") || normalizedQuery.startsWith("find ") || 
                   normalizedQuery.startsWith("get ") || normalizedQuery.startsWith("search ")) {
            // Handle descriptive queries like "show me meat products"
            String cleanQuery = normalizedQuery
                    .replaceFirst("^(show me |find |get |search )", "")
                    .replaceAll("\\s+products?\\s*$", "")
                    .trim();
            return performIntelligentSearch(cleanQuery, brand, maxPrice);
        } else if (brand != null || maxPrice != null) {
            // Use complex search with filters
            return productRepository.findBySearchCriteria(query, brand, maxPrice);
        } else {
            // Enhanced text search - search in name, description, and category
            return productRepository.findByTextSearch(query);
        }
    }

    private List<Product> searchByCategory(String category, String brand, Double maxPrice) {
        if (brand != null || maxPrice != null) {
            return productRepository.findBySearchCriteria(null, brand, maxPrice)
                    .stream()
                    .filter(p -> p.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        } else {
            return productRepository.findByCategoryIgnoreCase(category);
        }
    }

    public List<Product> searchProductsByCategory(String category) {
        log.debug("Searching products by category: {}", category);
        return productRepository.findByCategoryIgnoreCase(category);
    }

    public List<Product> getProductSuggestions(String itemName) {
        log.debug("Getting product suggestions for: {}", itemName);
        
        // First try exact substring match (current logic)
        List<Product> exactMatches = productRepository.findByNameContainingIgnoreCaseAndInStockTrue(itemName);
        
        if (!exactMatches.isEmpty()) {
            return exactMatches.stream().limit(5).collect(Collectors.toList());
        }
        
        // If no exact matches, try reverse matching (product name contains the search term)
        List<Product> allProducts = productRepository.findByInStockTrue();
        List<Product> reverseMatches = allProducts.stream()
                .filter(product -> product.getName().toLowerCase().contains(itemName.toLowerCase()) || 
                                 itemName.toLowerCase().contains(product.getName().toLowerCase()) ||
                                 isRelatedProduct(itemName.toLowerCase(), product.getName().toLowerCase()))
                .limit(5)
                .collect(Collectors.toList());
                
        return reverseMatches;
    }
    
    private boolean isRelatedProduct(String searchTerm, String productName) {
        // Handle common product variations
        String[] searchWords = searchTerm.split("\\s+");
        String[] productWords = productName.toLowerCase().split("\\s+");
        
        // Check if any search word matches any product word
        for (String searchWord : searchWords) {
            for (String productWord : productWords) {
                if (searchWord.equals(productWord) || 
                    productWord.contains(searchWord) || 
                    searchWord.contains(productWord)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    public Product saveProduct(Product product) {
        log.debug("Saving product: {}", product.getName());
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public List<Product> getProductsByNames(List<String> productNames) {
        log.debug("Getting products by names: {}", productNames);
        List<Product> allProducts = productRepository.findByInStockTrue();
        return allProducts.stream()
                .filter(product -> productNames.stream()
                        .anyMatch(name -> product.getName().equalsIgnoreCase(name.trim())))
                .collect(Collectors.toList());
    }

    public Optional<Product> getProductById(String productId) {
        log.debug("Fetching product by ID: {}", productId);
        return productRepository.findById(productId);
    }

    public void initializeSampleProducts() {
        if (productRepository.count() == 0) {
            log.info("Initializing sample products for search functionality...");

            // Dairy products
            productRepository.save(new Product("Whole Milk", "Fresh Farm", 4.99, "Dairy", "Fresh organic whole milk"));
            productRepository.save(new Product("Greek Yogurt", "Chobani", 5.49, "Dairy", "Creamy Greek yogurt"));
            productRepository.save(new Product("Cheddar Cheese", "Kraft", 3.99, "Dairy", "Sharp cheddar cheese"));
            productRepository.save(new Product("Butter", "Land O Lakes", 4.29, "Dairy", "Unsalted butter"));

            // Meat products
            productRepository.save(new Product("Chicken Breast", "Perdue", 8.99, "Meat", "Boneless chicken breast"));
            productRepository.save(new Product("Ground Beef", "Angus", 6.99, "Meat", "Lean ground beef"));
            productRepository.save(new Product("Salmon Fillet", "Wild Catch", 12.99, "Meat", "Fresh Atlantic salmon"));

            // Vegetables
            productRepository.save(new Product("Broccoli", "Green Valley", 2.49, "Vegetables", "Fresh broccoli crowns"));
            productRepository.save(new Product("Carrots", "Garden Fresh", 1.99, "Vegetables", "Organic baby carrots"));
            productRepository.save(new Product("Spinach", "Leafy Greens", 3.49, "Vegetables", "Fresh baby spinach"));
            productRepository.save(new Product("Bell Peppers", "Farm Fresh", 2.99, "Vegetables", "Colorful bell peppers"));

            // Fruits
            productRepository.save(new Product("Bananas", "Tropical", 1.29, "Fruits", "Ripe yellow bananas"));
            productRepository.save(new Product("Apples", "Orchard Best", 3.99, "Fruits", "Honeycrisp apples"));
            productRepository.save(new Product("Oranges", "Citrus Co", 4.49, "Fruits", "Navel oranges"));

            // Bakery
            productRepository.save(new Product("Whole Wheat Bread", "Wonder", 2.99, "Bakery", "Whole grain bread"));
            productRepository.save(new Product("Bagels", "Einstein", 4.99, "Bakery", "Everything bagels"));

            // Beverages
            productRepository.save(new Product("Orange Juice", "Tropicana", 3.99, "Beverages", "100% pure orange juice"));
            productRepository.save(new Product("Coffee", "Starbucks", 7.99, "Beverages", "Ground coffee beans"));

            // Snacks
            productRepository.save(new Product("Potato Chips", "Lays", 2.99, "Snacks", "Classic potato chips"));
            productRepository.save(new Product("Granola Bars", "Nature Valley", 4.99, "Snacks", "Crunchy granola bars"));

            // Pantry items
            productRepository.save(new Product("Rice", "Uncle Ben's", 3.49, "Pantry", "Long grain white rice"));
            productRepository.save(new Product("Pasta", "Barilla", 1.99, "Pantry", "Spaghetti pasta"));
            productRepository.save(new Product("Olive Oil", "Bertolli", 8.99, "Pantry", "Extra virgin olive oil"));

            log.info("Sample products initialized successfully - {} products added", productRepository.count());
        } else {
            log.info("Products already exist in database - skipping initialization");
        }
    }
}
