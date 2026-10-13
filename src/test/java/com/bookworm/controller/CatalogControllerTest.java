package com.bookworm.controller;

import com.bookworm.controller.impl.CatalogControllerImpl;
import com.bookworm.dto.response.*;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.service.CatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogControllerTest {

    @Mock
    private CatalogService catalogService;

    @InjectMocks
    private CatalogControllerImpl catalogController;

    private UUID bookId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET /api/v1/categories returns 200 OK and category list")
    void testGetCategories_ReturnsOk() {
        CategoryDto dto = new CategoryDto();
        dto.setId(categoryId);
        dto.setName("Self Help");
        dto.setSlug("self-help");

        when(catalogService.getCategories()).thenReturn(List.of(dto));

        ResponseEntity<List<CategoryDto>> response = catalogController.getCategories();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Self Help");
        verify(catalogService, times(1)).getCategories();
    }

    @Test
    @DisplayName("GET /api/v1/books returns 200 OK and paginated books")
    void testGetBooks_ReturnsOk() {
        BookSummaryDto summaryDto = new BookSummaryDto();
        summaryDto.setId(bookId);
        summaryDto.setTitle("Joy of Minimalism");
        summaryDto.setPrice(new BigDecimal("149.00"));

        PageBookDto pageDto = new PageBookDto();
        pageDto.setContent(List.of(summaryDto));
        pageDto.setPage(0);
        pageDto.setSize(12);
        pageDto.setTotalElements(1L);
        pageDto.setTotalPages(1);

        when(catalogService.getBooks(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
            .thenReturn(pageDto);

        ResponseEntity<PageBookDto> response = catalogController.getBooks(null, null, null, null, null, "relevance", 0, 12);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getTitle()).isEqualTo("Joy of Minimalism");
        verify(catalogService, times(1)).getBooks(any(), any(), any(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} returns 200 OK and book details")
    void testGetBookById_ReturnsOk() {
        BookDetailDto detailDto = new BookDetailDto();
        detailDto.setId(bookId);
        detailDto.setTitle("Joy of Minimalism");
        detailDto.setAuthorName("Daniel Reed");

        when(catalogService.getBookById(bookId)).thenReturn(detailDto);

        ResponseEntity<BookDetailDto> response = catalogController.getBookById(bookId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Joy of Minimalism");
        verify(catalogService, times(1)).getBookById(bookId);
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} throws ResourceNotFoundException when not found")
    void testGetBookById_NotFound_ThrowsException() {
        when(catalogService.getBookById(bookId))
            .thenThrow(new ResourceNotFoundException("Book not found with ID: " + bookId));

        assertThrows(ResourceNotFoundException.class, () -> catalogController.getBookById(bookId));
        verify(catalogService, times(1)).getBookById(bookId);
    }
}
