package com.library.dao.impl;

import com.library.dao.BookDAO;
import com.library.model.Book;
import com.library.model.BookStatus;
import com.library.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresBookDAO implements BookDAO {
    private final Connection connection;

    public PostgresBookDAO() {
        this.connection = DatabaseConnection.getConnection();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS books (
                id SERIAL PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                author VARCHAR(255) NOT NULL,
                isbn VARCHAR(20),
                genre VARCHAR(100),
                status VARCHAR(20) NOT NULL,
                added_date DATE DEFAULT CURRENT_DATE,
                due_date DATE,
                priority INTEGER DEFAULT 1,
                total_pages INTEGER,
                pages_read INTEGER DEFAULT 0
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Таблица books создана или уже существует");
        } catch (SQLException e) {
            System.err.println("Ошибка создания таблицы: " + e.getMessage());
        }
    }

    @Override
    public Long addBook(Book book) {
        String sql = """
            INSERT INTO books (title, author, isbn, genre, status, added_date, due_date, priority, total_pages, pages_read)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setString(4, book.getGenre());
            stmt.setString(5, book.getStatus().name());
            stmt.setDate(6, Date.valueOf(book.getAddedDate()));
            stmt.setDate(7, book.getDueDate() != null ? Date.valueOf(book.getDueDate()) : null);
            stmt.setInt(8, book.getPriority());
            stmt.setInt(9, book.getTotalPages());
            stmt.setInt(10, book.getPagesRead());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка добавления книги: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean updateBook(Book book) {
        String sql = """
            UPDATE books SET title=?, author=?, isbn=?, genre=?, status=?, due_date=?, 
            priority=?, total_pages=?, pages_read=? WHERE id=?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setString(4, book.getGenre());
            stmt.setString(5, book.getStatus().name());
            stmt.setDate(6, book.getDueDate() != null ? Date.valueOf(book.getDueDate()) : null);
            stmt.setInt(7, book.getPriority());
            stmt.setInt(8, book.getTotalPages());
            stmt.setInt(9, book.getPagesRead());
            stmt.setLong(10, book.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка обновления книги: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteBook(Long id) {
        String sql = "DELETE FROM books WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка удаления книги: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<Book> getBookById(Long id) {
        String sql = "SELECT * FROM books WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(resultSetToBook(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения книги по ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                books.add(resultSetToBook(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения книг: " + e.getMessage());
        }
        return books;
    }

    @Override
    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = """
            SELECT * FROM books WHERE 
            LOWER(title) LIKE LOWER(?) OR 
            LOWER(author) LIKE LOWER(?) OR 
            LOWER(isbn) LIKE LOWER(?) OR 
            LOWER(genre) LIKE LOWER(?)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                books.add(resultSetToBook(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка поиска книг: " + e.getMessage());
        }
        return books;
    }

    @Override
    public List<Book> filterByStatus(BookStatus status) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE status = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(resultSetToBook(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка фильтрации по статусу: " + e.getMessage());
        }
        return books;
    }

    @Override
    public List<Book> filterByGenre(String genre) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE LOWER(genre) = LOWER(?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, genre);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(resultSetToBook(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка фильтрации по жанру: " + e.getMessage());
        }
        return books;
    }

    @Override
    public List<Book> sortByTitle() {
        List<Book> books = getAllBooks();
        books.sort((b1, b2) -> b1.getTitle().compareToIgnoreCase(b2.getTitle()));
        return books;
    }

    @Override
    public List<Book> sortByAuthor() {
        List<Book> books = getAllBooks();
        books.sort((b1, b2) -> b1.getAuthor().compareToIgnoreCase(b2.getAuthor()));
        return books;
    }

    @Override
    public List<Book> sortByDueDate() {
        List<Book> books = getAllBooks();
        books.sort((b1, b2) -> {
            LocalDate date1 = b1.getDueDate() != null ? b1.getDueDate() : LocalDate.MAX;
            LocalDate date2 = b2.getDueDate() != null ? b2.getDueDate() : LocalDate.MAX;
            return date1.compareTo(date2);
        });
        return books;
    }

    @Override
    public List<Book> sortByPriority() {
        List<Book> books = getAllBooks();
        books.sort((b1, b2) -> Integer.compare(b2.getPriority(), b1.getPriority()));
        return books;
    }

    @Override
    public boolean updateStatus(Long id, BookStatus status) {
        String sql = "UPDATE books SET status = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка обновления статуса: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Book> getOverdueBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE due_date < CURRENT_DATE AND status != 'COMPLETED'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                books.add(resultSetToBook(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения просроченных книг: " + e.getMessage());
        }
        return books;
    }

    private Book resultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getLong("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setGenre(rs.getString("genre"));
        book.setStatus(com.library.model.BookStatus.valueOf(rs.getString("status")));
        book.setAddedDate(rs.getDate("added_date").toLocalDate());

        Date dueDate = rs.getDate("due_date");
        if (dueDate != null) book.setDueDate(dueDate.toLocalDate());

        book.setPriority(rs.getInt("priority"));
        book.setTotalPages(rs.getInt("total_pages"));
        book.setPagesRead(rs.getInt("pages_read"));

        return book;
    }
}