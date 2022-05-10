package com.example.tennis.parser.url;

import com.example.tennis.entity.League;

public class Tennis24UrlService implements UrlService {

    private static volatile Tennis24UrlService instance;

    public static Tennis24UrlService getInstance() {
        if (instance == null) {
            instance = new Tennis24UrlService();
        }
        return instance;
    }

    private Tennis24UrlService(){
    }

    public String getHomePage() {
        return "https://www.flashscore.com/tennis/";
    }

    public String urlLeaguesList() {
        return "https://www.flashscore.com/x/req/m_2_";
    }

    public String getUrlOfLeaguesByCountryNumber(int i) {
        return urlLeaguesList() + i;
    }

    public String getTournamentResultsURL(String countryCode, String leagueCode, int season) {
        if (season != -1)
            return "https://www.tennis24.com/" + countryCode +
                    "/" + leagueCode + "-" + season + "-" + (season + 1) + "/results/";
        else
            return "https://www.tennis24.com/" + countryCode +
                    "/" + leagueCode + "/results/";
    }

    public String getCupResultsURL(String countryCode, String leagueCode, int season) {
        if (season != -1)
            return "https://www.tennis24.com/" + countryCode +
                    "/" + leagueCode + "-" + season + "/results/";
        else
            return "https://www.tennis24.com/" + countryCode +
                    "/" + leagueCode + "/results/";
    }

    public String getPrevToursURL(String leagueMTCode, int seasonCode, int pageNumber) {
        return "https://d.tennis24.com/x/feed/tr_" +
                leagueMTCode + "_" + seasonCode + "_" + pageNumber + "_4_ru_1";
    }

    public String getGameBasePageURL(String gameId) {
        return "https://www.tennis24.com/match/" + gameId;
    }

    public String getGameCoefsURL(String gameId) {
        return "https://d.tennis24.com/x/feed/df_od_2_" + gameId;
    }

    public String getGamePointByPointResultsURL(String gameId) {
        return "https://d.tennis24.com/x/feed/df_mh_2_" + gameId;
    }

    public String getArchiveURL(League league) {
        return "https://www.tennis24.com/" +
                league.getCountryCode() + "/" + league.getLeagueCode() + "/archive/";
    }
}
