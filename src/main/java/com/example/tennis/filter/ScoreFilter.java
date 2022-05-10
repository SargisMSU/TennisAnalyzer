package com.example.tennis.filter;

import com.example.tennis.entity.TennisGame;

public class ScoreFilter extends Filter {
    String scoreFirstText, scoreSecondText;

    public ScoreFilter(String scoreFirstText, String scoreSecondText) {
        this.scoreFirstText = scoreFirstText;
        this.scoreSecondText = scoreSecondText;
    }

    @Override
    public boolean check(TennisGame game) {
        boolean b = checkScores(scoreFirstText, scoreSecondText, game);
        if (!b)
            return false;
        else {
            setFavorite(game);
            return checkNext(game);
        }
    }

    private void setFavorite(TennisGame game) {
        int favorite = game.getFavorite();
        if (favorite == 0 && game.getCoef1() > game.getCoef2()){
            game.setFavorite(2);
        }else if (favorite == 0 && game.getCoef1() < game.getCoef2()){
            game.setFavorite(1);
        }
    }

    private boolean checkScores(String firstSetScores, String secondSetScores, TennisGame game) {
        if (secondSetScores.length() == 0) {
            return containsScore(game, firstSetScores, 0, true) ||
                    containsScore(game, firstSetScores, 0, false);
        } else {
            return containsScore(game, secondSetScores, 1, false) &&
                    equalsScore(game, firstSetScores, false) ||
                    containsScore(game, secondSetScores, 1, true) &&
                            equalsScore(game, firstSetScores, true);
        }
    }

    private boolean equalsScore(TennisGame game, String firstSetScores, boolean reverse) {
        for (String s : firstSetScores.split(";")) {
            boolean fav1 = Integer.parseInt(s.split("-")[0]) >=
                    Integer.parseInt(s.split("-")[1]);
            if (!reverse && game.getScoreStr().split(";")[0].equals(s)) {
                game.setFavorite(fav1 ? 1 : 2);
                return true;
            }
            if (reverse && game.getScoreStr().split(";")[0].equals(new StringBuilder(s).reverse().toString())) {
                game.setFavorite(fav1 ? 2 : 1);
                return true;
            }
        }
        return false;
    }

    private boolean containsScore(TennisGame game, String setScores, int indexOfSet, boolean reverse) {
        String setScorePointByPoint = game.getScorePointByPoint().split("_")[indexOfSet];
        if (setScores == null || setScores.length() == 0) return true;
        String[] scoreSplits = setScores.split(";");
        for (String setScore : scoreSplits) {
            boolean fav1 = Integer.parseInt(setScore.split("-")[0]) >=
                    Integer.parseInt(setScore.split("-")[1]);
            if (!reverse && setScorePointByPoint.contains(setScore)) {
                game.setFavorite(fav1 ? 1 : 2);
                return true;
            } else if (reverse && setScorePointByPoint.contains(new StringBuilder(setScore).reverse().toString())) {
                game.setFavorite(fav1 ? 2 : 1);
                return true;
            }
        }
        return false;
    }
}
