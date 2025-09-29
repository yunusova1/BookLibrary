package com.library.dao;

import com.library.model.Book;
import com.library.model.BookStatus;

import java.util.List;
import java.util.Optional;

public interface BookDAO {
    // CRUD операции
    Long addBook(Book book);
    boolean updateBook(Book book);
    boolean deleteBook(Long id);
    Optional<Book> getBookById(Long id);
    List<Book> getAllBooks();

    // Поиск и фильтрация
    List<Book> searchBooks(String keyword);
    List<Book> filterByStatus(BookStatus status);
    List<Book> filterByGenre(String genre);

    // Сортировка
    List<Book> sortByTitle();
    List<Book> sortByAuthor();
    List<Book> sortByDueDate();
    List<Book> sortByPriority();

    // Управление статусами
    boolean updateStatus(Long id, BookStatus status);
    List<Book> getOverdueBooks();
}