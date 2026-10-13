package com.bookworm.service.impl;

import com.bookworm.dto.response.*;
import com.bookworm.entity.Book;
import com.bookworm.entity.Category;
import com.bookworm.entity.Review;
import com.bookworm.entity.enums.BookFormat;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.BookRepository;
import com.bookworm.repository.CategoryRepository;
import com.bookworm.repository.ReviewRepository;
import com.bookworm.service.CatalogService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CatalogServiceImpl implements CatalogService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    private static final DateTimeFormatter DELIVERY_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH);

    public CatalogServiceImpl(
        CategoryRepository categoryRepository,
        BookRepository bookRepository,
        ReviewRepository reviewRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public List<CategoryDto> getCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
            .stream()
            .map(this::toCategoryDto)
            .toList();
    }

    @Override
    public PageBookDto getBooks(
        String query,
        UUID categoryId,
        String format,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String sortBy,
        int page,
        int size
    ) {
        Sort sort = resolveSort(sortBy);
        int pageNumber = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 50));
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Specification<Book> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                String pattern = "%" + query.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate authorMatch = cb.like(cb.lower(root.get("authorName")), pattern);
                predicates.add(cb.or(titleMatch, authorMatch));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (format != null && !format.trim().isEmpty()) {
                try {
                    BookFormat bookFormat = BookFormat.valueOf(format.trim());
                    predicates.add(cb.equal(root.get("format"), bookFormat));
                } catch (IllegalArgumentException ignored) {
                    // Ignore invalid format filter
                }
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Book> bookPage = bookRepository.findAll(spec, pageable);
        List<BookSummaryDto> content = bookPage.getContent().stream()
            .map(this::toSummaryDto)
            .toList();

        PageBookDto pageBookDto = new PageBookDto();
        pageBookDto.setContent(content);
        pageBookDto.setPage(bookPage.getNumber());
        pageBookDto.setSize(bookPage.getSize());
        pageBookDto.setTotalElements(bookPage.getTotalElements());
        pageBookDto.setTotalPages(bookPage.getTotalPages());

        return pageBookDto;
    }

    @Override
    public BookDetailDto getBookById(UUID id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));

        List<ReviewDto> reviews = reviewRepository.findByBookIdOrderByCreatedAtDesc(id)
            .stream()
            .map(this::toReviewDto)
            .toList();

        List<BookSummaryDto> relatedBooks = List.of();
        if (book.getCategory() != null) {
            relatedBooks = bookRepository.findTop4ByCategoryIdAndIdNot(book.getCategory().getId(), id)
                .stream()
                .map(this::toSummaryDto)
                .toList();
        }

        BookDetailDto detailDto = new BookDetailDto();
        detailDto.setId(book.getId());
        detailDto.setTitle(book.getTitle());
        detailDto.setIsbn(book.getIsbn());
        detailDto.setDescription(book.getDescription());
        if (book.getCategory() != null) {
            detailDto.setCategoryId(book.getCategory().getId());
            detailDto.setCategoryName(book.getCategory().getName());
        }
        detailDto.setAuthorName(book.getAuthorName());
        detailDto.setAuthorBio(book.getAuthorBio());
        detailDto.setAuthorAvatarUrl(book.getAuthorAvatarUrl());
        detailDto.setPublisherName(book.getPublisherName());
        detailDto.setFormat(book.getFormat() != null ? book.getFormat().name() : BookFormat.Paperback.name());
        detailDto.setLanguage(book.getLanguage());
        detailDto.setPrice(book.getPrice());
        detailDto.setStockQuantity(book.getStockQuantity());
        detailDto.setSalesCount(book.getSalesCount());
        detailDto.setRating(book.getRating());
        detailDto.setFrontCoverUrl(book.getFrontCoverUrl());
        detailDto.setBackCoverUrl(book.getBackCoverUrl());
        detailDto.setEstimatedDelivery("Delivery by " + LocalDate.now().plusDays(5).format(DELIVERY_DATE_FORMAT));
        detailDto.setReviews(reviews);
        detailDto.setRelatedBooks(relatedBooks);

        return detailDto;
    }

    @Override
    public FeaturedBooksResponse getFeaturedBooks() {
        List<BookSummaryDto> recommended = bookRepository.findTop4ByOrderByRatingDesc()
            .stream()
            .map(this::toSummaryDto)
            .toList();

        List<BookSummaryDto> bestsellers = bookRepository.findByIsBestsellerTrue()
            .stream()
            .map(this::toSummaryDto)
            .toList();

        List<BookSummaryDto> newLaunches = bookRepository.findByIsNewLaunchTrue()
            .stream()
            .map(this::toSummaryDto)
            .toList();

        FeaturedBooksResponse response = new FeaturedBooksResponse();
        response.setRecommended(recommended);
        response.setBestsellers(bestsellers);
        response.setNewLaunches(newLaunches);
        return response;
    }

    private CategoryDto toCategoryDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        return dto;
    }

    private BookSummaryDto toSummaryDto(Book book) {
        BookSummaryDto dto = new BookSummaryDto();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthorName(book.getAuthorName());
        dto.setPrice(book.getPrice());
        dto.setFormat(book.getFormat() != null ? book.getFormat().name() : BookFormat.Paperback.name());
        if (book.getCategory() != null) {
            dto.setCategoryName(book.getCategory().getName());
        }
        dto.setRating(book.getRating());
        dto.setFrontCoverUrl(book.getFrontCoverUrl());
        dto.setEstimatedDelivery("Delivery by " + LocalDate.now().plusDays(5).format(DELIVERY_DATE_FORMAT));
        return dto;
    }

    private ReviewDto toReviewDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setUserName(review.getUserName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }

    private Sort resolveSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "salesCount");
        }
        return switch (sortBy) {
            case "priceAsc" -> Sort.by(Sort.Direction.ASC, "price");
            case "priceDesc" -> Sort.by(Sort.Direction.DESC, "price");
            case "rating" -> Sort.by(Sort.Direction.DESC, "rating");
            case "newest" -> Sort.by(Sort.Direction.DESC, "publishedDate");
            default -> Sort.by(Sort.Direction.DESC, "salesCount");
        };
    }
}
