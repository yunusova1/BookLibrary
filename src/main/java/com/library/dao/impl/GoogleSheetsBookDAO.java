package com.library.dao.impl;

import com.library.dao.BookDAO;
import com.library.model.Book;
import com.library.model.BookStatus;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class GoogleSheetsBookDAO implements BookDAO {
    private final String spreadsheetId;
    private final List<Book> books = new ArrayList<>();
    private Long nextId = 1L;

    public GoogleSheetsBookDAO(String spreadsheetId) {
        this.spreadsheetId = spreadsheetId;
        System.out.println("Google Sheets DAO инициализирован (режим эмуляции)");
    }

    @Override
    public Long addBook(Book book) {
        book.setId(nextId++);
        books.add(book);
        return book.getId();
    }

    @Override
    public boolean updateBook(Book book) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId().equals(book.getId())) {
                books.set(i, book);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean deleteBook(Long id) {
        return books.removeIf(book -> book.getId().equals(id));
    }

    @Override
    public Optional<Book> getBookById(Long id) {
        return books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    @Override
    public List<Book> searchBooks(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return getAllBooks().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(lowerKeyword) ||
                        book.getAuthor().toLowerCase().contains(lowerKeyword) ||
                        (book.getIsbn() != null && book.getIsbn().toLowerCase().contains(lowerKeyword)) ||
                        (book.getGenre() != null && book.getGenre().toLowerCase().contains(lowerKeyword)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> filterByStatus(BookStatus status) {
        return getAllBooks().stream()
                .filter(book -> book.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> filterByGenre(String genre) {
        return getAllBooks().stream()
                .filter(book -> book.getGenre() != null && book.getGenre().equalsIgnoreCase(genre))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> sortByTitle() {
        return getAllBooks().stream()
                .sorted(Comparator.comparing(com.library.model.Book::getTitle))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> sortByAuthor() {
        return getAllBooks().stream()
                .sorted(Comparator.comparing(com.library.model.Book::getAuthor))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> sortByDueDate() {
        return getAllBooks().stream()
                .sorted(Comparator.comparing(book ->
                        book.getDueDate() != null ? book.getDueDate() : LocalDate.MAX))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> sortByPriority() {
        return getAllBooks().stream()
                .sorted(Comparator.comparing(com.library.model.Book::getPriority).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateStatus(Long id, BookStatus status) {
        Optional<Book> bookOpt = getBookById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setStatus(status);
            return updateBook(book);
        }
        return false;
    }

    @Override
    public List<Book> getOverdueBooks() {
        LocalDate today = LocalDate.now();
        return getAllBooks().stream()
                .filter(book -> book.getDueDate() != null &&
                        book.getDueDate().isBefore(today) &&
                        book.getStatus() != com.library.model.BookStatus.COMPLETED)
                .collect(Collectors.toList());
    }
}