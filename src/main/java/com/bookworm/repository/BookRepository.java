package com.bookworm.repository;

import com.bookworm.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID>, JpaSpecificationExecutor<Book> {
    Optional<Book> findByIsbn(String isbn);
    List<Book> findByIsBestsellerTrue();
    List<Book> findByIsNewLaunchTrue();
    List<Book> findTop4ByCategoryIdAndIdNot(UUID categoryId, UUID id);
    List<Book> findTop4ByOrderByRatingDesc();
}
