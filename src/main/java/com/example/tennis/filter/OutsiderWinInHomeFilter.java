package com.example.tennis.filter;

import com.example.tennis.entity.TennisGame;

public class OutsiderWinInHomeFilter extends Filter{
    private final boolean outsiderWinInHome;

    public OutsiderWinInHomeFilter(boolean outsiderWinInHome) {
        this.outsiderWinInHome = outsiderWinInHome;
    }

    @Override
    public boolean check(TennisGame game) {
        boolean b = checkOutsiderWinInHome(game);
        if (!b){
            return false;
        }
        return checkNext(game);
    }

    public boolean checkOutsiderWinInHome(TennisGame game){
        if (outsiderWinInHome) {
            int winner = game.getScore1() > game.getScore2() ? 1 : 2;
            int favorite = game.getFavorite();
            boolean isOutsiderWin = (favorite == 1 && winner == 2) ||
                    (favorite == 2 && winner == 1);
            String winnerCountry = winner == 1 ? game.getCountry1() : game.getCountry2();
            boolean isWinnerInHome = winnerCountry.equals(game.getStadium());
            if (isOutsiderWin && isWinnerInHome) return false;
        }
        return true;
    }
}
