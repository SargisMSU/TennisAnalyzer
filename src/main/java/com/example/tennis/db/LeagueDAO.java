package com.example.tennis.db;

import com.example.tennis.entity.League;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LeagueDAO {
    Session session;

    public LeagueDAO(Session session) {
        this.session = session;
    }

    public void saveLeagues(List<League> leagueList) {
        Transaction transaction = session.beginTransaction();
        for (int i = 0; i < leagueList.size(); i++) {
            session.saveOrUpdate(leagueList.get(i));
        }
        transaction.commit();
    }

    public CopyOnWriteArrayList<League> getAllLeagues(){
        return new CopyOnWriteArrayList<>(session.
                createQuery("from League", League.class).
                getResultList());
    }

    public League getLeague(String leagueMTCode) {
        return session.get(League.class, leagueMTCode);
    }
}
