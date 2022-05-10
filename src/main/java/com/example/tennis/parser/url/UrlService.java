package com.example.tennis.parser.url;

import com.example.tennis.entity.League;

public interface UrlService {
    public  abstract String getHomePage();

    public abstract String urlLeaguesList();

    public abstract String getUrlOfLeaguesByCountryNumber(int i);

    public abstract String getTournamentResultsURL(String countryCode, String leagueCode, int season);

    public abstract String getCupResultsURL(String countryCode, String leagueCode, int season);

    public abstract String getPrevToursURL(String leagueMTCode, int seasonCode, int pageNumber);

    public abstract String getGameBasePageURL(String gameId);

    public abstract String getGameCoefsURL(String gameId);

    public abstract String getGamePointByPointResultsURL(String gameId);

    public abstract String getArchiveURL(League league);
}
