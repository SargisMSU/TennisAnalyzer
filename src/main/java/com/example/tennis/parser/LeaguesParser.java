package com.example.tennis.parser;

import com.example.tennis.db.DBService;
import com.example.tennis.entity.League;
import com.example.tennis.parser.url.FlashscoreUrlService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LeaguesParser {
    HttpClientHelper httpClientHelper;

    public LeaguesParser() {
        this.httpClientHelper = HttpClientHelper.getInstance();
    }

    public CopyOnWriteArrayList<League> getAllLeaguesFromFile(){
        try {
            String jsonString = Files.readString(Paths.get("leagues.json"));
            Gson gson = new Gson();
            return new CopyOnWriteArrayList<>(Arrays.asList(gson.fromJson(jsonString, League[].class)));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Creating new file.");
            return parseAllLeagues();
        }
    }

    public CopyOnWriteArrayList<League> parseAllLeagues() {
        HashMap<String, String> countryCodeMap = new HashMap<>();
        FlashscoreUrlService flashscoreUrlService = FlashscoreUrlService.getInstance();
        try {
            Document document = Jsoup.connect(flashscoreUrlService.getHomePage()).get();
            Elements left_menu = document.getElementsByClass("left_menu_categories_seo");
            System.out.println("left_menu.size() = " + left_menu.size());
            System.out.println("left_menu.get(0).html() = " + left_menu.get(0).html());

            Elements aElems = left_menu.get(0).getElementsByTag("a");
            for (int i = 0; i < aElems.size(); i++) {
                System.out.println("aElems + " + i +" = " + aElems.get(i));
                String country = aElems.get(i).text();
                String[] hrefParts = aElems.get(i).attr("href").split("/");
                if (hrefParts.length > 3) continue;
                String countryCode = hrefParts[2];
                countryCodeMap.put(countryCode, country);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        CopyOnWriteArrayList<League> leagues = new CopyOnWriteArrayList<>();
        int[] indexes = new int[]{6393, 8430, 10883};
        for (int i = 5724; i < 5744; i++) {
            addLeaguesForCountry(flashscoreUrlService.getUrlOfLeaguesByCountryNumber(i), countryCodeMap, leagues);
        }
        for (int i = 7897; i < 7901; i++) {
            addLeaguesForCountry(flashscoreUrlService.getUrlOfLeaguesByCountryNumber(i), countryCodeMap, leagues);
        }
        for (int i = 0; i < indexes.length; i++) {
            int k = indexes[i];
            addLeaguesForCountry(flashscoreUrlService.getUrlOfLeaguesByCountryNumber(k), countryCodeMap, leagues);
        }
        System.out.println("leagues.size() = " + leagues.size());
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(leagues);
        System.out.println("jsonElement.toString() = " + jsonElement.toString());
        try {
            Files.write(Paths.get("leagues.json"), jsonElement.toString().getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return leagues;
    }

    private void addLeaguesForCountry(String url, HashMap<String, String> countryCodeMap, List<League> leagues){
        try {
            String s = httpClientHelper.doGetRequest(url);
            if (s.length() == 0) return;
            int mlIndex = s.indexOf("ML÷") + 3;
            int mlLastIndex = s.indexOf('¬', mlIndex);
            String countryCode = s.substring(mlIndex, mlLastIndex);
            if (!countryCodeMap.containsKey(countryCode))
                return;
            String[] splitLeagues = s.split("~MN÷");
            for (int i = 1; i < splitLeagues.length; i++) {
                String splitLeague = splitLeagues[i];
                int indexOfLeagueName = 0;
                int lastIndexOfLeagueName = getLastIndexOfValue(splitLeague, indexOfLeagueName);
                String leagueName = splitLeague.substring(indexOfLeagueName, lastIndexOfLeagueName);

                int indexOfLeagueCode = splitLeague.indexOf("MU÷") + 3;
                int lastIndexOfLeagueCode = getLastIndexOfValue(splitLeague, indexOfLeagueCode);
                String leagueCode = splitLeague.substring(indexOfLeagueCode, lastIndexOfLeagueCode);

                int indexOfMtCode = splitLeague.indexOf("MT÷") + 3;
                int lastIndexOfMtCode = getLastIndexOfValue(splitLeague, indexOfMtCode);
                String mtCode = splitLeague.substring(indexOfMtCode, lastIndexOfMtCode);
                leagues.add(new League(mtCode,   countryCodeMap.get(countryCode),
                        countryCode, leagueName, leagueCode));
                System.out.println("League: " + countryCodeMap.get(countryCode) + ", " +
                        countryCode + ", " + leagueName + ", " +  leagueCode + ", " + mtCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int getLastIndexOfValue(String s, int fromIndex){
        return s.indexOf('¬', fromIndex);
    }

    public static void main(String[] args) {
        LeaguesParser leaguesParser = new LeaguesParser();
        CopyOnWriteArrayList<League> allLeaguesFromFile = leaguesParser.getAllLeaguesFromFile();
        DBService.getInstance().saveLeagues(allLeaguesFromFile);
    }
}
