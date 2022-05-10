package com.example.tennis.fx;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class UIUtils {
    public static void logToTextArea(TextArea textArea, String log){
        Platform.runLater(() -> {
            textArea.setText(textArea.getText() + "\n" + log);
        });
    }
}
