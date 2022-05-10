package com.example.tennis.filter;

import com.example.tennis.entity.TennisGame;

public class BaseFilter extends Filter {
    @Override
    public boolean check(TennisGame game) {
        String[] firstSetScores = game.getScoreStr().split(";")[0].split("-");
        if (game.getScorePointByPoint().split("_").length < 2)
            return false;
        if (firstSetScores.length < 2)
            return false;
        return checkNext(game);
    }
}
