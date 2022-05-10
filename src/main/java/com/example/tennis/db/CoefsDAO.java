package com.example.tennis.db;

import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class CoefsDAO {
    Session session;

    public CoefsDAO(Session session) {
        this.session = session;
    }

    public List<String> getAllBKNames(){
        return new ArrayList<>(session.createQuery("select distinct c.bookmaker from Coefs as c",
                        String.class).getResultList());
    }
}
