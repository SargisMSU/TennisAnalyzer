package com.example.tennis.filter;

import com.example.tennis.entity.TennisGame;

public class LeagueFilter extends Filter {
    String tournament, countryName;

    public LeagueFilter(String tournament, String countryName) {
        this.tournament = tournament;
        this.countryName = countryName;
    }

    @Override
    public boolean check(TennisGame game) {
        boolean b = checkLeague(game, tournament, countryName);
        if (!b)
            return false;
        else
            return checkNext(game);
    }

    private boolean checkLeague(TennisGame game, String tournamentChoiceBoxValue, String countryName) {
        if (tournamentChoiceBoxValue != null && !tournamentChoiceBoxValue.equals("Все")) {
            String tournament = tournamentChoiceBoxValue.substring(
                    tournamentChoiceBoxValue.indexOf(": ") + 2,
                    tournamentChoiceBoxValue.indexOf('('));
            String country = tournamentChoiceBoxValue.substring(0, tournamentChoiceBoxValue.indexOf(": "));
            String leagueName = tournament.substring(0, tournament.lastIndexOf(" "));
            int season = Integer.parseInt(tournament.substring(tournament.lastIndexOf(" ") + 1));
            if (game.getSeason() != season) return false;
            if (!game.getLeague().getLeagueName().equals(leagueName) ||
                    !game.getLeague().getCountry().equals(country)) return false;
        }
        return countryName.equals("Все") || game.getLeague().getCountry().equals(countryName);
    }
}
