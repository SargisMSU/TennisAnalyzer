package com.example.tennis;

import com.example.tennis.fx.FilterController;
import com.example.tennis.fx.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Starter extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Starter.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 990, 650);
        ((MainController)fxmlLoader.getController()).setHostServices(getHostServices());
        stage.setTitle("Tennis analyzer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}