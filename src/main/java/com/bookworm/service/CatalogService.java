package com.bookworm.service;

import com.bookworm.dto.response.BookDetailDto;
import com.bookworm.dto.response.CategoryDto;
import com.bookworm.dto.response.FeaturedBooksResponse;
import com.bookworm.dto.response.PageBookDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CatalogService {
    List<CategoryDto> getCategories();
    PageBookDto getBooks(String query, UUID categoryId, String format, BigDecimal minPrice, BigDecimal maxPrice, String sortBy, int page, int size);
    BookDetailDto getBookById(UUID id);
    FeaturedBooksResponse getFeaturedBooks();
}
