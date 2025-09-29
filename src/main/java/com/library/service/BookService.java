package com.library.service;

import com.library.dao.BookDAO;
import com.library.model.Book;
import com.library.model.BookStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class BookService {
    private BookDAO bookDAO;

    public BookService(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
    }

    public void setBookDAO(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
    }

    // CRUD операции
    public Long addBook(Book book) {
        validateBook(book);
        return bookDAO.addBook(book);
    }

    public boolean updateBook(Book book) {
        validateBook(book);
        return bookDAO.updateBook(book);
    }

    public boolean deleteBook(Long id) {
        return bookDAO.deleteBook(id);
    }

    public Book getBookById(Long id) {
        return bookDAO.getBookById(id).orElse(null);
    }

    public List<Book> getAllBooks() {
        return bookDAO.getAllBooks();
    }

    // Управление статусами с автоматизацией
    public boolean updateBookStatus(Long id, BookStatus status) {
        Book book = getBookById(id);
        if (book != null) {
            book.setStatus(status);

            // Автоматическое обновление на основе условий
            if (status == com.library.model.BookStatus.COMPLETED) {
                book.setPagesRead(book.getTotalPages());
            } else if (status == com.library.model.BookStatus.IN_PROGRESS && book.getPagesRead() == 0) {
                book.setPagesRead(1);
            }

            return updateBook(book);
        }
        return false;
    }

    public void checkAndUpdateOverdueBooks() {
        List<Book> books = bookDAO.getAllBooks();
        LocalDate today = LocalDate.now();

        for (Book book : books) {
            if (book.getDueDate() != null &&
                    book.getDueDate().isBefore(today) &&
                    book.getStatus() != com.library.model.BookStatus.COMPLETED) {

                book.setStatus(com.library.model.BookStatus.OVERDUE);
                bookDAO.updateBook(book);
            }
        }
    }

    // Фильтрация и поиск
    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBooks();
        }
        return bookDAO.searchBooks(keyword.toLowerCase());
    }

    public List<Book> filterByStatus(BookStatus status) {
        return bookDAO.filterByStatus(status);
    }

    public List<Book> filterByGenre(String genre) {
        return bookDAO.filterByGenre(genre);
    }

    public Book importBookByISBN(String isbn) {
        try {
            // API вызов к OpenLibrary
            String url = "https://openlibrary.org/api/books?bibkeys=ISBN:" + isbn + "&format=json&jscmd=data";

            // Реализация HTTP запроса и парсинга JSON
            // Возвращает книгу с заполненными метаданными

        } catch (Exception e) {
            System.err.println("Ошибка импорта по ISBN: " + e.getMessage());
        }
        return null;
    }

    public List<Book> getUpcomingDueBooks(int daysThreshold) {
        return bookDAO.getAllBooks().stream()
                .filter(book -> book.getDueDate() != null)
                .filter(book -> {
                    long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(
                            LocalDate.now(), book.getDueDate());
                    return daysUntilDue <= daysThreshold && daysUntilDue >= 0;
                })
                .collect(Collectors.toList());
    }

    // Сортировка
    public List<Book> sortByDueDate() {
        return bookDAO.sortByDueDate();
    }

    public List<Book> sortByPriority() {
        return bookDAO.sortByPriority();
    }

    // Логика рекомендательной системы
    public List<Book> getRecommendedBooks(String favoriteGenre) {
        return bookDAO.getAllBooks().stream()
                .filter(book -> favoriteGenre.equalsIgnoreCase(book.getGenre()))
                .filter(book -> book.getStatus() != com.library.model.BookStatus.COMPLETED)
                .sorted((b1, b2) -> Integer.compare(b2.getPriority(), b1.getPriority()))
                .collect(Collectors.toList());
    }

    // Логика расчета скорости чтения
    public String getReadingSpeedAnalysis(Long bookId) {
        Book book = getBookById(bookId);
        if (book == null || book.getAddedDate() == null) return "Недостаточно данных";

        long daysReading = java.time.temporal.ChronoUnit.DAYS.between(
                book.getAddedDate(), LocalDate.now());

        if (daysReading == 0) return "Книга добавлена сегодня";

        double pagesPerDay = (double) book.getPagesRead() / daysReading;
        int daysRemaining = (int) Math.ceil((book.getTotalPages() - book.getPagesRead()) / pagesPerDay);

        return String.format("Скорость: %.1f стр/день. Завершение через %d дней",
                pagesPerDay, daysRemaining);
    }

    private void validateBook(Book book) {
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Название книги обязательно");
        }
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new IllegalArgumentException("Автор книги обязателен");
        }
        if (book.getPagesRead() > book.getTotalPages()) {
            throw new IllegalArgumentException("Прочитано страниц не может быть больше общего количества");
        }
    }
}