package com.bookworm.controller;

import com.bookworm.dto.response.BookDetailDto;
import com.bookworm.dto.response.CategoryDto;
import com.bookworm.dto.response.FeaturedBooksResponse;
import com.bookworm.dto.response.PageBookDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Tag(name = "Catalog", description = "Book catalog browsing, search, and details")
@RequestMapping("/api/v1")
public interface CatalogController {

    @Operation(summary = "Get all categories", description = "Retrieves the list of all categories for sidebar navigation.")
    @GetMapping("/categories")
    ResponseEntity<List<CategoryDto>> getCategories();

    @Operation(summary = "Search and filter books", description = "Returns a paginated list of books with search query, category, format, and price filters.")
    @GetMapping("/books")
    ResponseEntity<PageBookDto> getBooks(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) UUID categoryId,
        @RequestParam(required = false) String format,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(defaultValue = "relevance") String sortBy,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size
    );

    @Operation(summary = "Get book details by ID", description = "Returns comprehensive details of a book, including front/back covers, author bio, publisher, and reviews.")
    @GetMapping("/books/{id}")
    ResponseEntity<BookDetailDto> getBookById(@PathVariable UUID id);

    @Operation(summary = "Get featured book widgets", description = "Returns lists for Recommended for You, Bestsellers this Month, and New Launches.")
    @GetMapping("/books/featured")
    ResponseEntity<FeaturedBooksResponse> getFeaturedBooks();
}
