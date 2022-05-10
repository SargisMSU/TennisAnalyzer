package com.example.tennis.filter;

import java.util.stream.Collectors;

public class FiltersChainFactory {
    public static Filter createChainOfFilters(String tournament, String country, String scoreFirst,
                                              String scoreSecond, boolean outsiderWinAtHome, String coefLider,
                                              String coef2, boolean rankUnder, String rankLeader, String rank2,
                                              String minDiff, String minDiffType){
        Filter filter = new BaseFilter();
        filter.with(new LeagueFilter(tournament, country))
                .with(new ScoreFilter(scoreFirst, scoreSecond))
                .with(new OutsiderWinInHomeFilter(outsiderWinAtHome))
                .with(new CoefFilter(coefLider, coef2))
                .with(new RankFilter(rankUnder, rankLeader, rank2, minDiff, minDiffType));
        return filter;
    }
}
