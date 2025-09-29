package com.library;

import com.library.controller.BookController;
import com.library.dao.BookDAO;
import com.library.dao.impl.GoogleSheetsBookDAO;
import com.library.service.BookService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        BookDAO bookDAO = new GoogleSheetsBookDAO("sheet_id");
        BookService bookService = new BookService(bookDAO);

        // Загрузка FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/book-view.fxml"));
        Parent root = loader.load();

        // Получаем контроллер и передаем ему сервис
        BookController controller = loader.getController();
        controller.setBookService(bookService);

        primaryStage.setTitle("Библиотека книг - PostgreSQL");
        primaryStage.setScene(new Scene(root, 1000, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}