package com.bookworm.controller.impl;

import com.bookworm.controller.CatalogController;
import com.bookworm.dto.response.BookDetailDto;
import com.bookworm.dto.response.CategoryDto;
import com.bookworm.dto.response.FeaturedBooksResponse;
import com.bookworm.dto.response.PageBookDto;
import com.bookworm.service.CatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
public class CatalogControllerImpl implements CatalogController {

    private final CatalogService catalogService;

    public CatalogControllerImpl(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public ResponseEntity<List<CategoryDto>> getCategories() {
        List<CategoryDto> categories = catalogService.getCategories();
        return ResponseEntity.ok(categories);
    }

    @Override
    public ResponseEntity<PageBookDto> getBooks(
        String query,
        UUID categoryId,
        String format,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String sortBy,
        int page,
        int size
    ) {
        PageBookDto books = catalogService.getBooks(query, categoryId, format, minPrice, maxPrice, sortBy, page, size);
        return ResponseEntity.ok(books);
    }

    @Override
    public ResponseEntity<BookDetailDto> getBookById(UUID id) {
        BookDetailDto book = catalogService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @Override
    public ResponseEntity<FeaturedBooksResponse> getFeaturedBooks() {
        FeaturedBooksResponse featured = catalogService.getFeaturedBooks();
        return ResponseEntity.ok(featured);
    }
}
