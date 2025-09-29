package com.library.util;

import com.library.dao.BookDAO;
import com.library.dao.impl.PostgresBookDAO;
import com.library.model.Book;
import com.library.model.BookStatus;

import java.time.LocalDate;

public class TestData {
    public static void main(String[] args) {
        BookDAO dao = new PostgresBookDAO();

        // Добавляем тестовые книги
        Book book1 = new Book(null, "Преступление и наказание", "Ф.М. Достоевский",
                "978-5-17-090507-1", "Классика", BookStatus.IN_PROGRESS,
                LocalDate.now(), LocalDate.now().plusDays(30), 8, 672, 150);

        Book book2 = new Book(null, "Мастер и Маргарита", "М.А. Булгаков",
                "978-5-389-07464-5", "Классика", com.library.model.BookStatus.ACTIVE,
                LocalDate.now(), LocalDate.now().plusDays(45), 9, 480, 0);

        dao.addBook(book1);
        dao.addBook(book2);

        System.out.println("Тестовые данные добавлены");
    }
}