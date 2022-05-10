package com.example.tennis.filter;

import com.example.tennis.entity.TennisGame;

public class RankFilter extends Filter{
    boolean rankUnder;
    String rankLiderText,  rank2Text, minDiffText, minDiffTypeChoiceBoxValue;

    public RankFilter(boolean rankUnder, String rankLiderText, String rank2Text,
                      String minDiffText, String minDiffTypeChoiceBoxValue) {
        this.rankUnder = rankUnder;
        this.rankLiderText = rankLiderText;
        this.rank2Text = rank2Text;
        this.minDiffText = minDiffText;
        this.minDiffTypeChoiceBoxValue = minDiffTypeChoiceBoxValue;
    }

    @Override
    public boolean check(TennisGame game) {
        boolean b = checkRanks(game, rankUnder, rankLiderText, rank2Text, minDiffText, minDiffTypeChoiceBoxValue);
        if (!b)
            return false;
        return checkNext(game);
    }

    private boolean checkRanks(TennisGame game, boolean rankUnder, String rankLeaderText, String rank2Text,
                              String minDiffText, String minDiffTypeChoiceBoxValue){
        int rankFav = game.getFavorite() == 1 ? game.getRank1() : game.getRank2();
        int rank2 = game.getFavorite() == 2 ? game.getRank1() : game.getRank2();
        if (rankUnder && rankFav >= rank2) return false;
        if (rankLeaderText.length() > 0){
            String[] rankLeaderSplits = rankLeaderText.split("-");
            if (rankLeaderSplits.length > 1){
                int rankLeaderMin = -1, rankLeaderMax = -1;
                try {
                    if (rankLeaderSplits[0].length() > 0){
                        rankLeaderMin = Integer.parseInt(rankLeaderSplits[0]);
                    }
                    if (rankLeaderSplits[1].length() > 0){
                        rankLeaderMax = Integer.parseInt(rankLeaderSplits[1]);
                    }
                }catch (Exception ignored){
                }
                if (rankLeaderMin != -1 && rankFav < rankLeaderMin) return false;
                if (rankLeaderMax != -1 && rankFav > rankLeaderMax) return false;
            }
        }
        if (rank2Text.length() > 0){
            String[] rank2Splits = rank2Text.split("-");
            if (rank2Splits.length > 1){
                int rank2Min = -1, rank2Max = -1;
                try {
                    if (rank2Splits[0].length() > 0){
                        rank2Min = Integer.parseInt(rank2Splits[0]);
                    }
                    if (rank2Splits[1].length() > 0){
                        rank2Max = Integer.parseInt(rank2Splits[1]);
                    }
                }catch (Exception ignored){
                }
                if (rank2Min != -1 && rank2 < rank2Min) return false;
                if (rank2Max != -1 && rank2 > rank2Max) return false;
            }
        }
        int diff = Math.abs(rankFav - rank2);
        try {
            int diffMin = Integer.parseInt(minDiffText);
            if (minDiffTypeChoiceBoxValue.equals("%")) {
                if ((double) rankFav * diffMin / 100 > diff)
                    return false;
            } else {
                if (diff < diffMin) return false;
            }
        }catch (Exception ignored){
        }
        return true;
    }
}
