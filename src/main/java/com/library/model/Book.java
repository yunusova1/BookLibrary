package com.library.model;

import java.time.LocalDate;

public class Book {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private BookStatus status;
    private LocalDate addedDate;
    private LocalDate dueDate;
    private Integer priority;
    private Integer totalPages;
    private Integer pagesRead;

    public Book() {
    }

    public Book(Long id, String title, String author, String isbn, String genre, BookStatus status, LocalDate addedDate, LocalDate dueDate, Integer priority, Integer totalPages, Integer pagesRead) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
        this.status = status;
        this.addedDate = addedDate;
        this.dueDate = dueDate;
        this.priority = priority;
        this.totalPages = totalPages;
        this.pagesRead = pagesRead;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public BookStatus getStatus() { return status; }
    public void setStatus(BookStatus status) { this.status = status; }

    public LocalDate getAddedDate() { return addedDate; }
    public void setAddedDate(LocalDate addedDate) { this.addedDate = addedDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }

    public Integer getPagesRead() { return pagesRead; }
    public void setPagesRead(Integer pagesRead) { this.pagesRead = pagesRead; }

    // Дополнительные методы
    public Double getReadingProgress() {
        if (totalPages == null || totalPages == 0) return 0.0;
        return (double) pagesRead / totalPages * 100;
    }

    public LocalDate getEstimatedFinishDate() {
        if (pagesRead == null || totalPages == null || pagesRead >= totalPages)
            return LocalDate.now();

        int pagesLeft = totalPages - pagesRead;
        long daysLeft = (long) Math.ceil(pagesLeft / 10.0); // 10 страниц в день
        return LocalDate.now().plusDays(daysLeft);
    }
}