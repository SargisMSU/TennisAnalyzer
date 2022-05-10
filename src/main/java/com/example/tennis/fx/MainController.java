package com.example.tennis.fx;

import javafx.application.HostServices;
import javafx.fxml.FXML;

public class MainController {
    @FXML
    private FilterController filterViewController;
    @FXML
    private ParserController parserViewController;

    public void setHostServices(HostServices hostServices) {
        filterViewController.setHostServices(hostServices);
        parserViewController.setFilterController(filterViewController);
    }
}