package com.voice.shopping.repository;

import com.voice.shopping.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    // Text search across name, brand, and description
    @Query("{ $text: { $search: ?0 } }")
    List<Product> findByTextSearch(String searchTerm);

    // Search with pagination
    @Query("{ $text: { $search: ?0 } }")
    Page<Product> findByTextSearch(String searchTerm, Pageable pageable);

    // Search by category
    List<Product> findByCategoryIgnoreCase(String category);

    // Search by brand
    List<Product> findByBrandIgnoreCase(String brand);

    // Search with price filter
    @Query("{ $text: { $search: ?0 }, price: { $lte: ?1 } }")
    List<Product> findByTextSearchAndPriceLessThanEqual(String searchTerm, Double maxPrice);

    // Complex search query
    @Query("{ $and: [ " +
            "{ $or: [ " +
            "  { $text: { $search: ?0 } }, " +
            "  { name: { $regex: ?0, $options: 'i' } }, " +
            "  { brand: { $regex: ?0, $options: 'i' } } " +
            "] }, " +
            "{ $expr: { $cond: { if: { $ne: [?1, null] }, then: { $eq: ['$brand', ?1] }, else: true } } }, " +
            "{ $expr: { $cond: { if: { $ne: [?2, null] }, then: { $lte: ['$price', ?2] }, else: true } } } " +
            "] }")
    List<Product> findBySearchCriteria(String query, String brand, Double maxPrice);

    // Find products in stock
    List<Product> findByInStockTrue();

    // Find by name containing (for suggestions)
    @Query("{ name: { $regex: ?0, $options: 'i' }, inStock: true }")
    List<Product> findByNameContainingIgnoreCaseAndInStockTrue(String name);
}
