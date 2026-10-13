package com.bookworm.service;

import com.bookworm.dto.response.*;
import com.bookworm.entity.Book;
import com.bookworm.entity.Category;
import com.bookworm.entity.Review;
import com.bookworm.entity.enums.BookFormat;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.BookRepository;
import com.bookworm.repository.CategoryRepository;
import com.bookworm.repository.ReviewRepository;
import com.bookworm.service.impl.CatalogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private CatalogServiceImpl catalogService;

    private Category testCategory;
    private Book testBook;
    private UUID bookId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        bookId = UUID.randomUUID();

        testCategory = new Category();
        testCategory.setId(categoryId);
        testCategory.setName("Self Help");
        testCategory.setSlug("self-help");

        testBook = new Book();
        testBook.setId(bookId);
        testBook.setTitle("Joy of Minimalism");
        testBook.setIsbn("978-0-123456-78-9");
        testBook.setDescription("Declutter your life.");
        testBook.setCategory(testCategory);
        testBook.setAuthorName("Daniel Reed");
        testBook.setPublisherName("ABC Publishers");
        testBook.setFormat(BookFormat.Paperback);
        testBook.setPrice(new BigDecimal("149.00"));
        testBook.setStockQuantity(85);
        testBook.setSalesCount(145);
        testBook.setRating(new BigDecimal("4.6"));
        testBook.setFrontCoverUrl("https://assets.bookworm.example/covers/joy-front.jpg");
        testBook.setBackCoverUrl("https://assets.bookworm.example/covers/joy-back.jpg");
        testBook.setIsBestseller(false);
        testBook.setIsNewLaunch(true);
        testBook.setPublishedDate(LocalDate.now());
    }

    @Test
    @DisplayName("getCategories should return mapped CategoryDto list sorted by name")
    void testGetCategories_Success() {
        Category cat2 = new Category();
        cat2.setId(UUID.randomUUID());
        cat2.setName("Mystery");
        cat2.setSlug("mystery");

        when(categoryRepository.findAll(any(Sort.class))).thenReturn(List.of(cat2, testCategory));

        List<CategoryDto> result = catalogService.getCategories();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Mystery");
        assertThat(result.get(1).getName()).isEqualTo("Self Help");
        verify(categoryRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("getBooks should return paginated PageBookDto with filter criteria")
    void testGetBooks_WithPagination_Success() {
        Page<Book> bookPage = new PageImpl<>(List.of(testBook));
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(bookPage);

        PageBookDto result = catalogService.getBooks("Minimalism", categoryId, "Paperback", BigDecimal.ZERO, new BigDecimal("500"), "relevance", 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Joy of Minimalism");
        assertThat(result.getContent().get(0).getAuthorName()).isEqualTo("Daniel Reed");
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getPage()).isEqualTo(0);
        verify(bookRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("getBookById should return full BookDetailDto when book exists")
    void testGetBookById_Success() {
        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setBook(testBook);
        review.setUserName("Alice");
        review.setRating(5);
        review.setComment("Excellent book!");
        review.setCreatedAt(Instant.now());

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(testBook));
        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId)).thenReturn(List.of(review));
        when(bookRepository.findTop4ByCategoryIdAndIdNot(categoryId, bookId)).thenReturn(List.of());

        BookDetailDto result = catalogService.getBookById(bookId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(bookId);
        assertThat(result.getTitle()).isEqualTo("Joy of Minimalism");
        assertThat(result.getCategoryName()).isEqualTo("Self Help");
        assertThat(result.getReviews()).hasSize(1);
        assertThat(result.getReviews().get(0).getUserName()).isEqualTo("Alice");
        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    @DisplayName("getBookById should throw ResourceNotFoundException when book does not exist")
    void testGetBookById_NotFound_ThrowsException() {
        UUID unknownId = UUID.randomUUID();
        when(bookRepository.findById(unknownId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> catalogService.getBookById(unknownId));

        assertThat(ex.getMessage()).contains("Book not found with ID");
        verify(bookRepository, times(1)).findById(unknownId);
    }

    @Test
    @DisplayName("getFeaturedBooks should return recommended, bestsellers and new launches")
    void testGetFeaturedBooks_Success() {
        when(bookRepository.findTop4ByOrderByRatingDesc()).thenReturn(List.of(testBook));
        when(bookRepository.findByIsBestsellerTrue()).thenReturn(List.of(testBook));
        when(bookRepository.findByIsNewLaunchTrue()).thenReturn(List.of(testBook));

        FeaturedBooksResponse result = catalogService.getFeaturedBooks();

        assertThat(result).isNotNull();
        assertThat(result.getRecommended()).hasSize(1);
        assertThat(result.getBestsellers()).hasSize(1);
        assertThat(result.getNewLaunches()).hasSize(1);
    }
}
