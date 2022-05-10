package com.example.tennis.parser;

import com.example.tennis.db.DBService;
import com.example.tennis.entity.Coefs;
import com.example.tennis.entity.League;
import com.example.tennis.entity.TennisGame;
import com.example.tennis.parser.url.Tennis24UrlService;
import com.example.tennis.parser.url.UrlService;
import javafx.scene.control.TextArea;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.tennis.fx.UIUtils.logToTextArea;

public class GamesParser {
    HttpClientHelper httpClientHelper;
    UrlService urlService;

    public GamesParser() {
        this.httpClientHelper = HttpClientHelper.getInstance();
        this.urlService = Tennis24UrlService.getInstance();
    }

    public void parseAndSaveMatchesByLink(List<League> leagues, String link, TextArea logsTA) {
        link = link.substring(25);
        String[] split = link.split("/");
        String countryCode = split[0];
        String leagueCode = split[1];
        String lastPart = leagueCode.split("-")[leagueCode.split("-").length - 1];
        int season = getSeasonFromLink(lastPart);
        if (season != -1){
            leagueCode = leagueCode.substring(0, leagueCode.indexOf(lastPart) - 1);
        }
        League league = null;
        for (League leagueTemp : leagues) {
            if (leagueTemp.getCountryCode().equals(countryCode) && leagueTemp.getLeagueCode().equals(leagueCode)) {
                league = leagueTemp;
                break;
            }
        }
        if (league == null) {
            logToTextArea(logsTA, "League not found");
        }else {
            if (season == -1)
                season = getLastSeason(league);
            logToTextArea(logsTA, "------" +league.getLeagueName() + ": " + season);
            logToTextArea(logsTA, "Начало парсинга: " + new Date());
            parseAndSaveMatches(league, season, logsTA);
        }
    }

    private int getLastSeason(League league) {
        String archiveURL = urlService.getArchiveURL(league);
        try {
            Document document = Jsoup.connect(archiveURL).get();
            String text = document.
                    getElementsByClass("archive__row").get(0).
                    getElementsByTag("a").get(0).text();
            String[] split = text.split(" ");
            System.out.println("text = " + text);
            return Integer.parseInt(split[split.length - 1]);
        }catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    private void parseAndSaveMatches(League league, int season, TextArea textArea) {
        Set<TennisGame> tempGames = new HashSet<>();
        String resultsPageUrl = urlService.getTournamentResultsURL(
                league.getCountryCode(), league.getLeagueCode(), season);
        Document document = getResultsPageDocument(resultsPageUrl, league, season);
        if (document == null) return;
        Element resultsDiv = document.getElementById("tournament-page-data-results");
        Element prevGamesUrlCode = document.getElementById("tournament-page-season-results");
        String seasonCodeText = prevGamesUrlCode.text().trim();
        int seasonCode = Integer.parseInt(seasonCodeText);
        String lastGameOfSeasonText = resultsDiv.text();
        addAllGamesFromResultsText(lastGameOfSeasonText, league, tempGames, season);
        for (int j = 0; j < 20; j++) {
            String s;
            try {
                s = httpClientHelper.doGetRequest(urlService.getPrevToursURL(league.getMtCode(), seasonCode, j));
                if (s == null || s.length() == 0) break;
                addAllGamesFromResultsText(s, league, tempGames, season);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        logToTextArea(textArea, "Найдено новых матчей: " + tempGames.size());
        tempGames = tempGames.parallelStream().filter(tennisGame ->
                        !DBService.getInstance().getGames().contains(tennisGame)).
                collect(Collectors.toSet());
        logToTextArea(textArea, "Парсинг point by point...");
        setPoints(tempGames);
        logToTextArea(textArea, "Парсинг коэффицентов...");
        setCoefs(tempGames);
        logToTextArea(textArea, "Парсинг рейтинг...");
        setRanks(tempGames);
        DBService.getInstance().getGames().addAll(tempGames);
        DBService.getInstance().saveGames(tempGames);
        logToTextArea(textArea, "Данные записаны в БД: " + new Date());
    }

    private Document getResultsPageDocument(String resultsPageUrl, League league, int season) {
        Document document = null;
        try {
            document = Jsoup.connect(resultsPageUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                resultsPageUrl = urlService.getCupResultsURL(
                        league.getCountryCode(), league.getLeagueCode(), season);
                document = Jsoup.connect(resultsPageUrl).get();
            } catch (IOException r) {
                r.printStackTrace();
            }
        }
        return document;
    }

    private void addAllGamesFromResultsText(String text, League league, Set<TennisGame> games, int season) {
        System.out.println("text = " + text);
        String[] splitByGames = text.split("¬~AA÷");
        int indexOfStadium1 = splitByGames[0].indexOf('(') + 1;
        int indexOfStadium2 = splitByGames[0].indexOf(')', indexOfStadium1);
        String stadium = splitByGames[0].substring(indexOfStadium1, indexOfStadium2);
        for (int j = 1; j < splitByGames.length; j++) {
            String gameStr = splitByGames[j];
            int lastIndexOfGameId = gameStr.indexOf('¬');
            String gameId = gameStr.substring(0, lastIndexOfGameId);
            if (gameStr.contains("¬AI÷y")) { //пропускаем oнлайн матчи
                System.out.println("live: https://www.flashscore.com.ua/match/" + gameId);
                continue;
            }
            int indexOfAd = lastIndexOfGameId + 4;
            int lastIndexOfAd = gameStr.indexOf('¬', indexOfAd);
            int indexOfAE = gameStr.indexOf("¬AE÷") + 4;
            int lastIndoxOfAE = gameStr.indexOf('¬', indexOfAE);
            int indexOfAG = gameStr.indexOf("¬AG÷") + 4;
            int indexOfFU = gameStr.indexOf("¬FU÷") + 4;
            int lastIndexOfFU = gameStr.indexOf('¬', indexOfFU);
            
            int indexOfFV = gameStr.indexOf("¬FV÷") + 4;
            int lastIndexOfFV = gameStr.indexOf('¬', indexOfFV);
            if (indexOfAG == 3) {
                System.out.println("Перенесен: https://www.flashscore.com.ua/match/" + gameId);
                continue;   //пропускем перенесенные матчи
            }
            int lastIndexOfAG = gameStr.indexOf('¬', indexOfAG);
            int indexOfAH = gameStr.indexOf("¬AH÷") + 4;
            int lastIndexOfAH = gameStr.indexOf('¬', indexOfAH);

            int indexOfAF = gameStr.indexOf("¬AF÷") + 4;
            int lastIndexOfAF = getLastIndexOfValue(gameStr, indexOfAF);

            String ad = gameStr.substring(indexOfAd, lastIndexOfAd);
            long dateInSeconds = Long.parseLong(ad);
            String gamer1 = gameStr.substring(indexOfAE, lastIndoxOfAE);
            String gamer2 = gameStr.substring(indexOfAF, lastIndexOfAF);
            int score1 = Integer.parseInt(gameStr.substring(indexOfAG, lastIndexOfAG));
            int score2 = Integer.parseInt(gameStr.substring(indexOfAH, lastIndexOfAH));
            String country1 = gameStr.substring(indexOfFU, lastIndexOfFU);
            String country2 = gameStr.substring(indexOfFV, lastIndexOfFV);

            int indexOfOA = gameStr.indexOf("¬OA÷");
            int indexOfOB = gameStr.indexOf("¬OB÷");
            String firstGamerScores = gameStr.substring(lastIndexOfAG, indexOfOA);
            String secondGamerScores = gameStr.substring(lastIndexOfAH, indexOfOB);
            String[] firstScoreSplits = firstGamerScores.split("[¬÷]");
            String[] secondScoreSplits = secondGamerScores.split("[¬÷]");
            StringJoiner scoreStr = new StringJoiner(";");
            int score = score1 + score2;
            for (int i = 0; i < score; i++) {
                StringJoiner setScore = new StringJoiner("-");
                int index = 2 * (i + 1);
                setScore.add(firstScoreSplits[index])
                        .add(secondScoreSplits[index]);
                scoreStr.add(setScore.toString());
            }
            TennisGame game = new TennisGame(league, gamer1, gamer2, score1, score2, season,
                    scoreStr.toString(), gameId, dateInSeconds, country1, country2, stadium);
            games.add(game);
        }
    }

    private void setPoints(Set<TennisGame> allMatches) {
        for (TennisGame game : allMatches) {
            try {
                String s = httpClientHelper.doGetRequest(urlService.getGamePointByPointResultsURL(game.getGameId()));
                if (s == null || s.length() == 0) continue;
                String[] splitBySets = s.split("HA÷");
                StringJoiner joiner = new StringJoiner("_");
                for (int i = 1; i < splitBySets.length; i++) {
                    String splitBySet = splitBySets[i];
                    int tiebreakIndex = splitBySet.indexOf("Tiebreak");
                    if (tiebreakIndex > -1){
                        splitBySet = splitBySet.substring(0, tiebreakIndex);
                    }
                    String[] splitsByHC = splitBySet.split("¬~HC÷");
                    StringJoiner joinerSet = new StringJoiner(";");
                    for (int j = 1; j < splitsByHC.length; j++) {
                        String score1 = splitsByHC[j].substring(0, getLastIndexOfValue(splitsByHC[j], 0));
                        int indexOfHE = splitsByHC[j].indexOf("¬HE÷") + 4;
                        int lastIndexOfHE = getLastIndexOfValue(splitsByHC[j], indexOfHE);
                        String score2 = splitsByHC[j].substring(indexOfHE, lastIndexOfHE);
                        joinerSet.add(score1 + "-" + score2);
                    }
                    joiner.add(joinerSet.toString());
                }
                game.setScorePointByPoint(joiner.toString());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setCoefs(Set<TennisGame> allMatches) {
        for (TennisGame game : allMatches) {
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                String s = httpClientHelper.doGetRequest(urlService.getGameCoefsURL(game.getGameId()));
                if (s == null || s.length() == 0) {
                    System.out.println("response == null or empty string");
                    continue;
                }
                int indexOfOA = s.indexOf("~OA÷") + 4;
                int lastIndexOfOA = s.indexOf('¬', indexOfOA);
                if (!s.substring(indexOfOA, lastIndexOfOA).equals("H/A")) {
                    System.out.println("oa != H/A");
                    continue;
                }
                String[] obus = s.split("OBU÷");
                if (!obus[1].startsWith("full-time")) {
                    System.out.println("obus[1] != full time");
                    System.out.println("obus.length = " + obus.length);
                    System.out.println("obus = " + Arrays.toString(obus));
                    continue;
                }
                String[] ods = obus[1].split("OD÷");
                ArrayList<Coefs> coefs = new ArrayList<>();
                for (String od : ods) {
                    int lastIndexOfBKName = od.indexOf('¬');
                    String bkName = od.substring(0, lastIndexOfBKName);
                    int indexOfXB = od.indexOf("¬XB÷");
                    int indexOfXC = od.indexOf("¬XC÷");
                    int lastIndexOfXC = od.indexOf('¬', indexOfXC + 4);
                    int potIndexOfXB = od.indexOf(']', indexOfXB) + 1;
                    if (potIndexOfXB >= indexOfXB && potIndexOfXB < indexOfXC) {
                        indexOfXB = potIndexOfXB;
                    } else {
                        indexOfXB += 4;
                    }
                    int potIndexOfXC = od.indexOf(']', indexOfXC) + 1;
                    if (indexOfXB < 0 || indexOfXC < 0) {
                        continue;
                    }
                    double coef1 = Double.parseDouble(od.substring(indexOfXB, indexOfXC));
                    if (potIndexOfXC >= indexOfXC && potIndexOfXC < lastIndexOfXC) {
                        indexOfXC = potIndexOfXC;
                    } else {
                        indexOfXC += 4;
                    }
                    double coef2 = Double.parseDouble(od.substring(indexOfXC, lastIndexOfXC));
                    coefs.add(new Coefs(bkName, coef1, coef2));
                }
                game.setCoefs(coefs);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setRanks(Set<TennisGame> games) {
        for (TennisGame game : games) {
            try {
                Document document = Jsoup.connect(urlService.getGameBasePageURL(game.getGameId())).get();
                Elements scripts = document.getElementsByTag("script");
                for (Element script : scripts) {
                    String html = script.html();
                    if (html.startsWith("window.environment = ")) {
                        try {
                            JSONObject jsonObject = new JSONObject(html.substring(21));
                            jsonObject = jsonObject.getJSONObject("participantsData");
                            JSONObject home = (JSONObject) jsonObject.getJSONArray("home").get(0);
                            JSONObject away = (JSONObject) jsonObject.getJSONArray("away").get(0);
                            int homeRank = 0, awayRank = 0;
                            try {
                                homeRank = Integer.parseInt(home.getJSONArray("rank").get(1).toString());
                            } catch (Exception ignored) {
                            }
                            try {
                                awayRank = Integer.parseInt(away.getJSONArray("rank").get(1).toString());
                            } catch (Exception ignored) {
                            }
                            if (homeRank == 0 && awayRank != 0) homeRank = awayRank + 1;
                            if (homeRank != 0 && awayRank == 0) awayRank = homeRank + 1;
                            game.setRank1(homeRank);
                            game.setRank2(awayRank);
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int getLastIndexOfValue(String s, int fromIndex) {
        return s.indexOf('¬', fromIndex);
    }

    private int getSeasonFromLink(String link){
        try {
            int i = Integer.parseInt(link);
            if (i > 2000)
                return i;
            else
                return -1;
        }catch (Exception e){
            return -1;
        }
    }
}
