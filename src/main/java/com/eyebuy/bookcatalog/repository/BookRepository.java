package com.eyebuy.bookcatalog.repository;

import com.eyebuy.bookcatalog.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    Optional<Book> findByIsbn(String isbn);

    Optional<Book> findByIsbnAndIdNot(String isbn, Long id);

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByAuthorContainingIgnoreCase(String author);

    List<Book> findByGenre(String genre);

    List<Book> findByActiveTrue();

    @Query("SELECT b FROM Book b WHERE b.active = true AND b.stock > 0 ORDER BY b.updateTime DESC")
    List<Book> findAvailableBooks();

    @Query("SELECT b FROM Book b WHERE " +
            "(:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:genre IS NULL OR b.genre = :genre) " +
            "AND (:active IS NULL OR b.active = :active)")
    Page<Book> searchBooks(@Param("keyword") String keyword,
                           @Param("genre") String genre,
                           @Param("active") Boolean active,
                           Pageable pageable);

    boolean existsByIsbn(String isbn);

    long countByGenre(String genre);

    long countByActiveTrue();
}
