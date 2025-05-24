package com.pedrojosesaez.controlpedidos;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class SelectFileApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        showSplash(stage);
    }


    public static void main(String[] args) {
        launch();
    }

    private void showSplash(Stage splashStage) {
        // Cargar imagen desde resources
        Image splashImage = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("logo_control_pedidos.png")));

        ImageView imageView = new ImageView(splashImage);
        // Ajuste de tamaño del splash
        imageView.setFitWidth(1000); // mínimo
        imageView.setFitHeight(500); // mínimo
        imageView.setPreserveRatio(true); // mantiene proporción
        imageView.setSmooth(true);
        imageView.setCache(true);
        StackPane splashLayout = new StackPane(imageView);
        Scene splashScene = new Scene(splashLayout);

        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setScene(splashScene);
        splashStage.show();

        // Simula carga y muestra la ventana principal
        new Thread(() -> {
            try {
                Thread.sleep(2500); // 2.5 segundos de splash
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                splashStage.close();
                try {
                    showMainStage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }

    private void showMainStage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SelectFileApplication.class.getResource("selection-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 750, 450);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());

        Stage mainStage = new Stage();
        mainStage.setMinHeight(350);
        mainStage.setMinWidth(800);
        mainStage.setMaxHeight(600);
        mainStage.setMaxWidth(1000);
        mainStage.setTitle("Control de Pedidos 1.0");
        mainStage.setScene(scene);
        mainStage.show();
    }
}