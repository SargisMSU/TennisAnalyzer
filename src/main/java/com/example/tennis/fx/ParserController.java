package com.example.tennis.fx;

import com.example.tennis.db.DBService;
import com.example.tennis.parser.GamesParser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ParserController {

    public TextArea logsTA;
    public TextField linkTA;
    public Button onParseButton;
    private FilterController filterViewController;

    @FXML
    public void initialize() {
    }

    public void onParseButtonClick(ActionEvent event) {
        logsTA.setText("");
        String inputLink = linkTA.getText();
        Thread thread = new Thread(() -> {
            GamesParser gamesParser = new GamesParser();
            gamesParser.parseAndSaveMatchesByLink(DBService.getInstance().getLeagues(), inputLink, logsTA);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                onParseButton.setDisable(false);
                linkTA.setDisable(false);
                filterViewController.updatePage();
            });
        });
        linkTA.setDisable(true);
        onParseButton.setDisable(true);
        thread.start();
    }

    public void setFilterController(FilterController filterViewController) {
        this.filterViewController = filterViewController;
    }
}
