package com.example.controlpedidos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class SelectFileApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SelectFileApplication.class.getResource("selection-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 750, 450);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
        stage.setMinHeight(350);
        stage.setMinWidth(800);
        stage.setMaxHeight(600);
        stage.setMaxWidth(1000);
        stage.setTitle("Control de Pedidos 1.0");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}