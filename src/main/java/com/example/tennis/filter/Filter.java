package com.example.tennis.filter;

import com.example.tennis.entity.TennisGame;

public abstract class Filter {
    Filter next;

    public Filter with(Filter next) {
        this.next = next;
        return next;
    }

    public abstract boolean check(TennisGame game);

    public boolean checkNext(TennisGame game){
        if (next == null){
            return true;
        }
        return next.check(game);
    }
}
