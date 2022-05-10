package com.example.tennis.filter;

import com.example.tennis.entity.TennisGame;

public class CoefFilter extends Filter{
    String coefLeaderText, coef2Text;

    public CoefFilter(String coefLeaderText, String coef2Text) {
        this.coefLeaderText = coefLeaderText;
        this.coef2Text = coef2Text;
    }

    @Override
    public boolean check(TennisGame game) {
        boolean b = checkCoefs(game, coefLeaderText, coef2Text);
        if (!b) return false;
        return checkNext(game);
    }

    private boolean checkCoefs(TennisGame game, String coefLeaderText, String coef2Text) {
        double coefLeader =  game.getFavorite() == 1 ? game.getCoef1() : game.getCoef2();
        double coef2 =  game.getFavorite() == 2 ? game.getCoef1() : game.getCoef2();
        boolean checkLeader = checkCoef(coefLeader, coefLeaderText);
        boolean check2 = checkCoef(coef2, coef2Text);
        return checkLeader && check2;
    }

    private boolean checkCoef(double coefLeader, String coefLeaderText) {
        if (coefLeaderText.length() > 0){
            String[] coefSplits = coefLeaderText.replaceAll(",",".").split("-");
            if (coefSplits.length == 2){
                double coefMin = -1, coefMax = -1;
                try {
                    if(coefSplits[0].length() > 0)
                        coefMin = Double.parseDouble(coefSplits[0]);
                    if (coefSplits[1].length() > 0)
                        coefMax = Double.parseDouble(coefSplits[1]);
                }catch (Exception ignore){
                }
                if (coefMin != -1 && coefLeader < coefMin) return false;
                if (coefMax != -1 && coefLeader > coefMax) return false;
            }
        }
        return true;
    }
}
