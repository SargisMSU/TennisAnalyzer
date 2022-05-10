package com.example.tennis.parser.url;

import com.example.tennis.entity.League;

public class FlashscoreUrlService implements UrlService {
    private static volatile FlashscoreUrlService instance;

    private FlashscoreUrlService(){
    }

    public static FlashscoreUrlService getInstance() {
        if (instance == null) {
            instance = new FlashscoreUrlService();
        }
        return instance;
    }

    public String getHomePage(){
        return "https://www.flashscore.com/tennis/";
    }

    public final String urlLeaguesList(){
        return "https://www.flashscore.com/x/req/m_2_";
    }

    public String getUrlOfLeaguesByCountryNumber(int i) {
        return urlLeaguesList() + i;
    }

    public String getTournamentResultsURL(String countryCode, String leagueCode, int season) {
        if (season != -1)
            return "https://www.flashscore.com/tennis/" + countryCode +
                    "/" + leagueCode + "-" + season + "-" + (season + 1) + "/results/";
        else
            return "https://www.flashscore.com/tennis/" + countryCode +
                    "/" + leagueCode + "/results/";
    }

    public String getCupResultsURL(String countryCode, String leagueCode, int season) {
        if (season != -1)
            return "https://www.flashscore.com/tennis/" + countryCode +
                    "/" + leagueCode + "-" + season + "/results/";
        else
            return "https://www.flashscore.com/tennis/" + countryCode +
                    "/" + leagueCode + "/results/";
    }

    public String getPrevToursURL(String leagueMTCode, int seasonCode, int pageNumber) {
        return "https://d.flashscore.com/x/feed/tr_" +
                leagueMTCode + "_" + seasonCode + "_" + pageNumber + "_4_ru_1";
    }

    public String getGameBasePageURL(String gameId) {
        return "https://www.flashscore.com/match/" + gameId;
    }

    public String getGameCoefsURL(String gameId) {
        return "https://d.flashscore.com/x/feed/df_od_1_" + gameId;
    }

    public String getGamePointByPointResultsURL(String gameId) {
        return "https://d.flashscore.com/x/feed/df_mh_1_" + gameId;
    }

    public String getArchiveURL(League league) {
        return "https://www.flashscore.com/tennis/" +
                league.getCountryCode() + "/" + league.getLeagueCode() + "/archive/";
    }
}
