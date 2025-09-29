package com.library.dao.impl;

import com.library.dao.BookDAO;
import com.library.model.Book;
import com.library.model.BookStatus;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class CSVBookDAO implements BookDAO {
    private final String csvFile;
    private Long nextId = 1L;

    public CSVBookDAO(String csvFile) {
        this.csvFile = csvFile;
        this.nextId = calculateNextId();
    }

    private Long calculateNextId() {
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            List<String[]> allData = reader.readAll();
            return allData.stream()
                    .skip(1) // Пропускаем заголовок
                    .mapToLong(row -> Long.parseLong(row[0]))
                    .max()
                    .orElse(0L) + 1;
        } catch (IOException | CsvException e) {
            return 1L;
        }
    }

    @Override
    public Long addBook(Book book) {
        List<String[]> allData = readAllData();

        String[] newRecord = {
                String.valueOf(nextId++),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getGenre(),
                book.getStatus().name(),
                book.getAddedDate().toString(),
                book.getDueDate() != null ? book.getDueDate().toString() : "",
                String.valueOf(book.getPriority()),
                String.valueOf(book.getTotalPages()),
                String.valueOf(book.getPagesRead())
        };

        allData.add(newRecord);

        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile))) {
            writer.writeAll(allData);
        } catch (IOException e) {
            System.err.println("Ошибка записи в CSV: " + e.getMessage());
            return null;
        }

        return nextId - 1;
    }

    @Override
    public boolean updateBook(Book book) {
        List<String[]> allData = readAllData();
        boolean found = false;

        for (int i = 1; i < allData.size(); i++) {
            String[] row = allData.get(i);
            if (row.length > 0 && row[0].equals(String.valueOf(book.getId()))) {
                allData.set(i, new String[]{
                        String.valueOf(book.getId()),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getIsbn(),
                        book.getGenre(),
                        book.getStatus().name(),
                        book.getAddedDate().toString(),
                        book.getDueDate() != null ? book.getDueDate().toString() : "",
                        String.valueOf(book.getPriority()),
                        String.valueOf(book.getTotalPages()),
                        String.valueOf(book.getPagesRead())
                });
                found = true;
                break;
            }
        }

        if (found) {
            return writeAllData(allData);
        }
        return false;
    }

    @Override
    public boolean deleteBook(Long id) {
        List<String[]> allData = readAllData();
        boolean removed = allData.removeIf(row ->
                row.length > 0 && row[0].equals(String.valueOf(id)) && !row[0].equals("id")
        );

        if (removed) {
            return writeAllData(allData);
        }
        return false;
    }

    @Override
    public Optional<Book> getBookById(Long id) {
        List<String[]> allData = readAllData();

        for (int i = 1; i < allData.size(); i++) {
            String[] row = allData.get(i);
            if (row.length > 0 && row[0].equals(String.valueOf(id))) {
                return Optional.of(convertRowToBook(row));
            }
        }

        return Optional.empty();
    }

    @Override
    public List<Book> getAllBooks() {
        List<String[]> allData = readAllData();
        List<Book> books = new ArrayList<>();

        for (int i = 1; i < allData.size(); i++) {
            books.add(convertRowToBook(allData.get(i)));
        }

        return books;
    }

    @Override
    public List<Book> searchBooks(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return getAllBooks().stream()
                .filter(book ->
                        book.getTitle().toLowerCase().contains(lowerKeyword) ||
                                book.getAuthor().toLowerCase().contains(lowerKeyword) ||
                                (book.getIsbn() != null && book.getIsbn().toLowerCase().contains(lowerKeyword)) ||
                                (book.getGenre() != null && book.getGenre().toLowerCase().contains(lowerKeyword))
                )
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

    // Вспомогательные методы

    private List<String[]> readAllData() {
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            return reader.readAll();
        } catch (IOException | CsvException e) {
            // Если файл не существует, создаем заголовок
            List<String[]> header = new ArrayList<>();
            header.add(new String[]{"id", "title", "author", "isbn", "genre", "status",
                    "added_date", "due_date", "priority", "total_pages", "pages_read"});
            return header;
        }
    }

    private boolean writeAllData(List<String[]> allData) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile))) {
            writer.writeAll(allData);
            return true;
        } catch (IOException e) {
            System.err.println("Ошибка записи в CSV: " + e.getMessage());
            return false;
        }
    }

    private Book convertRowToBook(String[] row) {
        if (row.length < 11) {
            throw new IllegalArgumentException("Неверный формат строки CSV");
        }

        Book book = new Book();
        book.setId(Long.parseLong(row[0]));
        book.setTitle(row[1]);
        book.setAuthor(row[2]);
        book.setIsbn(row[3].isEmpty() ? null : row[3]);
        book.setGenre(row[4].isEmpty() ? null : row[4]);
        book.setStatus(com.library.model.BookStatus.valueOf(row[5]));
        book.setAddedDate(LocalDate.parse(row[6]));
        book.setDueDate(row[7].isEmpty() ? null : LocalDate.parse(row[7]));
        book.setPriority(Integer.parseInt(row[8]));
        book.setTotalPages(Integer.parseInt(row[9]));
        book.setPagesRead(Integer.parseInt(row[10]));

        return book;
    }
}