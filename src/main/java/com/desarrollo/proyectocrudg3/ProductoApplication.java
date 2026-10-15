package com.desarrollo.proyectocrudg3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ProductoApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                ProductoApplication.class.getResource("producto-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 880, 823);
        stage.setTitle("Gestión de productos");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}