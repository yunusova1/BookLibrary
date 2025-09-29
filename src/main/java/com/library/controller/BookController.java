package com.library.controller;

import com.library.model.Book;
import com.library.model.BookStatus;
import com.library.service.BookService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.Optional;

public class BookController {
    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, Long> idColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> genreColumn;
    @FXML private TableColumn<Book, String> statusColumn;
    @FXML private TableColumn<Book, String> pagesColumn;

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField isbnField;
    @FXML private TextField genreField;
    @FXML private ComboBox<BookStatus> statusComboBox;
    @FXML private DatePicker dueDatePicker;
    @FXML private Spinner<Integer> prioritySpinner;
    @FXML private Spinner<Integer> totalPagesSpinner;
    @FXML private Spinner<Integer> pagesReadSpinner;
    @FXML private TextField searchField;

    private BookService bookService;
    private ObservableList<Book> booksData = FXCollections.observableArrayList();

    public void setBookService(BookService bookService) {
        this.bookService = bookService;
        refreshBooksTable();
        initializeStatusComboBox();
    }

    @FXML
    private void initialize() {
        setupTableColumns();
        setupSpinners();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));

        // Колонка жанра
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));

        // Колонка статуса с русскими названиями
        statusColumn.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    book.getStatus().getDisplayName()
            );
        });

        // Колонка для отображения информации о страницах
        pagesColumn.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            String pagesInfo;
            if (book.getTotalPages() != null && book.getPagesRead() != null) {
                pagesInfo = book.getPagesRead() + "/" + book.getTotalPages();
            } else if (book.getTotalPages() != null) {
                pagesInfo = "0/" + book.getTotalPages();
            } else {
                pagesInfo = "0/0";
            }
            return new javafx.beans.property.SimpleStringProperty(pagesInfo);
        });

        booksTable.setItems(booksData);

        // Обработчик выбора книги в таблице
        booksTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showBookDetails(newValue));
    }

    private void setupSpinners() {
        prioritySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 5));
        totalPagesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5000, 300));
        pagesReadSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5000, 0));
    }

    private void initializeStatusComboBox() {
        // Создаем список для ComboBox с русскими названиями
        ObservableList<BookStatus> statusList = FXCollections.observableArrayList(BookStatus.values());
        statusComboBox.setItems(statusList);

        // Устанавливаем кастомный отображатель для ComboBox
        statusComboBox.setCellFactory(param -> new ListCell<BookStatus>() {
            @Override
            protected void updateItem(BookStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        // Устанавливаем кастомный отображатель для выбранного значения
        statusComboBox.setButtonCell(new ListCell<BookStatus>() {
            @Override
            protected void updateItem(BookStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        statusComboBox.setValue(BookStatus.ACTIVE);
    }

    @FXML
    private void handleAddBook() {
        try {
            Book book = new Book();
            book.setTitle(titleField.getText());
            book.setAuthor(authorField.getText());
            book.setIsbn(isbnField.getText());
            book.setGenre(genreField.getText());
            book.setStatus(statusComboBox.getValue());
            book.setAddedDate(LocalDate.now());
            book.setDueDate(dueDatePicker.getValue());
            book.setPriority(prioritySpinner.getValue());
            book.setTotalPages(totalPagesSpinner.getValue());
            book.setPagesRead(pagesReadSpinner.getValue());

            Long newId = bookService.addBook(book);
            if (newId != null) {
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Книга добавлена с ID: " + newId);
                clearFields();
                refreshBooksTable();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            // Подтверждение удаления
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Удаление книги");
            alert.setContentText("Вы уверены, что хотите удалить книгу: " + selectedBook.getTitle() + "?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // Удаление из базы данных
                    boolean deleted = bookService.deleteBook(selectedBook.getId());

                    if (deleted) {
                        // Удаление из таблицы
                        booksTable.getItems().remove(selectedBook);

                        // Очистка полей
                        clearFields();

                        showAlert(Alert.AlertType.INFORMATION, "Успех", "Книга успешно удалена!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить книгу из базы данных.");
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить книгу: " + e.getMessage());
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Пожалуйста, выберите книгу для удаления.");
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            refreshBooksTable();
        } else {
            booksData.setAll(bookService.searchBooks(keyword));
        }
    }

    @FXML
    private void handleSortByDueDate() {
        booksData.setAll(bookService.sortByDueDate());
    }

    @FXML
    private void handleGetRecommendations() {
        String favoriteGenre = genreField.getText();
        if (favoriteGenre != null && !favoriteGenre.trim().isEmpty()) {
            booksData.setAll(bookService.getRecommendedBooks(favoriteGenre));
        }
    }

    private void showBookDetails(Book book) {
        if (book != null) {
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            isbnField.setText(book.getIsbn());
            genreField.setText(book.getGenre());
            statusComboBox.setValue(book.getStatus());
            dueDatePicker.setValue(book.getDueDate());
            prioritySpinner.getValueFactory().setValue(book.getPriority());
            totalPagesSpinner.getValueFactory().setValue(book.getTotalPages());
            pagesReadSpinner.getValueFactory().setValue(book.getPagesRead());
        }
    }

    private void refreshBooksTable() {
        booksData.setAll(bookService.getAllBooks());
    }

    @FXML
    private void clearFields() {
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        genreField.clear();
        statusComboBox.setValue(BookStatus.ACTIVE);
        dueDatePicker.setValue(null);
        prioritySpinner.getValueFactory().setValue(5);
        totalPagesSpinner.getValueFactory().setValue(300);
        pagesReadSpinner.getValueFactory().setValue(0);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}