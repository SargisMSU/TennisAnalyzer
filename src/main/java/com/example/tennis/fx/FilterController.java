package com.example.tennis.fx;

import com.example.tennis.db.DBService;
import com.example.tennis.filter.*;
import com.example.tennis.entity.Coefs;
import com.example.tennis.entity.League;
import com.example.tennis.entity.TennisGame;
import com.example.tennis.parser.url.Tennis24UrlService;
import javafx.application.HostServices;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FilterController {

    public CheckBox rankUnderCheckBox, winnerInHomeCheckBox;
    public ChoiceBox<String> countryChoiceBox, tournamentChoiceBox, minDiffTypeChoiceBox, bookmakerChoiceBox;
    public TextField rankLeaderTF, rank2TF, minDiffTF, betTF, coefTF;
    public TextField coefLiderTF, coef2TF, scoreFirstTF, scoreSecondTF;
    public TableView<TennisGame> gamesTableView;
    public TableColumn<TennisGame, String> matchColumn, scoreColumn;
    public TableColumn<TennisGame, Integer> rank1Column, rank2Column;
    public TableColumn<TennisGame, Double> coef1Column, coef2Column;
    public Label countFilteredLabel;
    public Label fullCountLabel;
    public Label resultLabel, percentLabel;
    private DBService dbService;
    private HostServices hostServices;

    @FXML
    public void initialize() {
        this.dbService = DBService.getInstance();
        initTableColumns();
        fullCountLabel.setText(DBService.getInstance().getGames().size() + "");
        bookmakerChoiceBox.setItems(dbService.getBookmakers());
        bookmakerChoiceBox.setValue("");
        initTournamentsAndCountries();
        initTableView();
    }

    private void initTournamentsAndCountries(){
        tournamentChoiceBox.setItems(dbService.getTournaments());
        tournamentChoiceBox.setValue(tournamentChoiceBox.getItems().get(0));
        countryChoiceBox.setItems(dbService.getTournamentTypes());
        countryChoiceBox.setValue(countryChoiceBox.getItems().get(0));
        countryChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
            if ((int) t1 == 0) {
                tournamentChoiceBox.setValue(tournamentChoiceBox.getItems().get(0));
                tournamentChoiceBox.getItems().removeIf(item -> !item.equals(tournamentChoiceBox.getValue()));
                tournamentChoiceBox.getItems().addAll(dbService.getTournaments());
            } else {
                String typeValue = countryChoiceBox.getItems().get((int) t1);
                ArrayList<String> strings = new ArrayList<>();
                strings.add("Все");
                ObservableList<String> stringObservableList = FXCollections.observableList(strings);
                ConcurrentHashMap<League, HashMap<Integer, Integer>> parsedLeagues = dbService.getParsedLeagues();
                for (Map.Entry<League, HashMap<Integer, Integer>> entry : parsedLeagues.entrySet()) {
                    League key = entry.getKey();
                    if (!key.getCountry().equals(typeValue)) continue;
                    HashMap<Integer, Integer> value = entry.getValue();
                    for (Map.Entry<Integer, Integer> val : value.entrySet()) {
                        stringObservableList.add(key.getCountry() + ": " + key.getLeagueName() + " " +
                                val.getKey() + "(" + val.getValue() + ")");
                    }
                }
                tournamentChoiceBox.setItems(stringObservableList);
                tournamentChoiceBox.setValue(tournamentChoiceBox.getItems().get(0));
            }
        });
    }

    private void initTableView(){
        gamesTableView.setOnMouseClicked(mouseEvent -> {
            try {
                TablePosition tablePosition = gamesTableView.getSelectionModel().getSelectedCells().get(0);
                TennisGame game = gamesTableView.getItems().get(tablePosition.getRow());
                hostServices.showDocument(Tennis24UrlService.getInstance().getGameBasePageURL(game.getGameId()));
            } catch (Exception ignored) {
            }
        });
    }

    public void onFilterClick() {
        boolean rankUnder = rankUnderCheckBox.isSelected();
        String rankLeader = rankLeaderTF.getText();
        String rank2 = rank2TF.getText();
        String minDiff = minDiffTF.getText();
        String coefLider = coefLiderTF.getText();
        String coef2 = coef2TF.getText();
        String scoreFirst = scoreFirstTF.getText();
        String scoreSecond = scoreSecondTF.getText();
        String country = countryChoiceBox.getValue();
        String tournament = tournamentChoiceBox.getValue();
        String minDiffType = minDiffTypeChoiceBox.getValue();
        boolean outsiderWinAtHome = winnerInHomeCheckBox.isSelected();
        List<TennisGame> filteredGames = new ArrayList<>(dbService.getGames());
        if (!bookmakerChoiceBox.getValue().equals("")) {
            filteredGames.removeIf(tennisGame -> {
                boolean thereIsCoef = false;
                if (tennisGame.getCoefs() == null) return true;
                for (Coefs coef : tennisGame.getCoefs()) {
                    if (coef.getBookmaker().equals(bookmakerChoiceBox.getValue())) {
                        thereIsCoef = true;
                        break;
                    }
                }
                return !thereIsCoef;
            });
            filteredGames.forEach(tennisGame -> {
                for (Coefs coef : tennisGame.getCoefs()) {
                    if (coef.getBookmaker().equals(bookmakerChoiceBox.getValue())) {
                        tennisGame.setCoef1(coef.getCoef1());
                        tennisGame.setCoef2(coef.getCoef2());
                    }
                }

            });
        }else {
            filteredGames.forEach(tennisGame -> {
                if (tennisGame.getCoefs() != null) {
                    Coefs coefs = tennisGame.getCoefs().get(0);
                    tennisGame.setCoef1(coefs.getCoef1());
                    tennisGame.setCoef2(coefs.getCoef2());
                }
            });
        }
        Filter filter = FiltersChainFactory.createChainOfFilters(tournament, country,
                scoreFirst, scoreSecond, outsiderWinAtHome, coefLider, coef2,
                rankUnder, rankLeader, rank2, minDiff, minDiffType);
        filteredGames = filteredGames.stream().filter(filter::check).collect(Collectors.toList());

        gamesTableView.setItems(FXCollections.observableList(filteredGames));
        gamesTableView.refresh();
        updateCounts(filteredGames);
    }

    private void updateCounts(List<TennisGame> filteredGames){
        countFilteredLabel.setText(filteredGames.size() + "");
        int winCount = (int) filteredGames.parallelStream().filter(game ->
                game.getFavorite() == 1 && game.getScore1() > game.getScore2() ||
                        game.getFavorite() == 2 && game.getScore2() > game.getScore1()).count();
        int lossCount = filteredGames.size() - winCount;
        double percent = (double) winCount * 100 / filteredGames.size();
        double coef = 1.5, bet = 1000;
        try {
            coef = Double.parseDouble(coefTF.getText());
        } catch (Exception ignored) {
        }
        try {
            bet = Double.parseDouble(betTF.getText());
        } catch (Exception ignored) {
        }

        double profit = winCount;
        profit *= (coef - 1) * bet;
        profit -= (lossCount * bet);
        resultLabel.setText(winCount + "(+) " + lossCount + "(-) " +
                String.format("%.2f", profit) + "руб");
        percentLabel.setText(String.format("%.2f", percent) + "% ");
    }

    private void initTableColumns() {
        matchColumn.setCellValueFactory(gameStringCellDataFeatures -> {
            String match = gameStringCellDataFeatures.getValue().getGamer1() + " - " +
                    gameStringCellDataFeatures.getValue().getGamer2();
            return new SimpleStringProperty(match);
        });
        scoreColumn.setCellValueFactory(game -> {
            String scoreStr = game.getValue().getScoreStr();
            return new SimpleStringProperty(game.getValue().getScore1() + "-" +
                    game.getValue().getScore2() + " | " + scoreStr);
        });
        rank1Column.setCellValueFactory(new PropertyValueFactory<>("rank1"));
        rank2Column.setCellValueFactory(new PropertyValueFactory<>("rank2"));
        coef1Column.setCellValueFactory(new PropertyValueFactory<>("coef1"));
        coef2Column.setCellValueFactory(new PropertyValueFactory<>("coef2"));
        gamesTableView.setRowFactory(new Callback<>() {
            @Override
            public TableRow<TennisGame> call(TableView<TennisGame> tennisGameTableView) {
                TableRow<TennisGame> row = new TableRow<>() {
                    @Override
                    protected void updateItem(TennisGame game, boolean b) {
                        super.updateItem(game, b);
                        if (game == null) {
                            setStyle("");
                            return;
                        }
                        int favorite = game.getFavorite();
                        if (favorite == 1 && game.getScore1() > game.getScore2() ||
                                favorite == 2 && game.getScore1() < game.getScore2())
                            setStyle("-fx-background-color: #B3FBA9;");
                        else if (favorite == 1 && game.getScore1() < game.getScore2() ||
                                favorite == 2 && game.getScore1() > game.getScore2())
                            setStyle("-fx-background-color: #F97C7C;");
                        else setStyle("");
                    }
                };
                return row;
            }
        });
    }

    public void updatePage() {
        fullCountLabel.setText(DBService.getInstance().getGames().size() + "");
        tournamentChoiceBox.setValue(tournamentChoiceBox.getItems().get(0));
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }
}
