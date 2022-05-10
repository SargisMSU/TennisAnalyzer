package com.example.tennis.db;

import com.example.tennis.entity.TennisGame;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;


public class GameDAO {
    Session session;

    public GameDAO(Session session) {
        this.session = session;
    }

    public void saveGames(Set<TennisGame> games) {
        Transaction transaction = session.beginTransaction();
        for (TennisGame game : games) {
            session.saveOrUpdate(game);
        }
        transaction.commit();
    }

    public CopyOnWriteArrayList<TennisGame> getAllGames(){
        return new CopyOnWriteArrayList<>(session.
                createQuery("select distinct t from TennisGame t " +
                                "join fetch t.coefs " +
                                "join fetch t.league",
                        TennisGame.class).
                getResultList());
    }
}
