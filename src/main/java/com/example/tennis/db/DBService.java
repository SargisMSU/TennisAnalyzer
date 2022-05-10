package com.example.tennis.db;

import com.example.tennis.entity.Coefs;
import com.example.tennis.entity.League;
import com.example.tennis.entity.TennisGame;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DBService {
    private static DBService instance;
    private final SessionFactory sessionFactory;
    private CopyOnWriteArrayList<League> leagues;
    private CopyOnWriteArrayList<TennisGame> games;
    private ObservableList<String> bookmakers;
    private ObservableList<String> tournamentTypes;
    private ObservableList<String> tournaments;
    private ConcurrentHashMap<League, HashMap<Integer, Integer>> parsedLeagues;

    private DBService() {
        Configuration configuration = getH2Configuration();
        sessionFactory = createSessionFactory(configuration);
        leagues = getAllLeagues();
        games = getAllGames();
        parsedLeagues = new ConcurrentHashMap<>();
        tournaments = FXCollections.observableList(new CopyOnWriteArrayList<>(){{add("Все");}});
        tournamentTypes = FXCollections.observableList(new CopyOnWriteArrayList<>(){{add("Все");}});
        bookmakers = FXCollections.observableList(new CopyOnWriteArrayList<>() {{add("");}});
        updateBKNames();
        updateTournaments(games);
    }

    public static DBService getInstance() {
        if (instance == null) {
            instance = new DBService();
        }
        return instance;
    }

    private void updateTournaments(Collection<TennisGame> games) {
        games.parallelStream().forEach(tennisGame -> {
            League league = tennisGame.getLeague();
            int season = tennisGame.getSeason();
            if (parsedLeagues.keySet().contains(league)) {
                HashMap<Integer, Integer> seasonCountMap = parsedLeagues.get(league);
                if (seasonCountMap.containsKey(season)) {
                    seasonCountMap.put(season, seasonCountMap.get(season) + 1);
                } else {
                    seasonCountMap.put(season, 1);
                }
            } else {
                HashMap<Integer, Integer> hashMap = new HashMap<>();
                hashMap.put(season, 1);
                parsedLeagues.put(league, hashMap);
            }
        });
        for (Map.Entry<League, HashMap<Integer, Integer>> leagueHashMapEntry : parsedLeagues.entrySet()) {
            League key = leagueHashMapEntry.getKey();
            HashMap<Integer, Integer> value = leagueHashMapEntry.getValue();
            for (Map.Entry<Integer, Integer> entry : value.entrySet()) {
                String tournament = leagueHashMapEntry.getKey().getCountry() + ": "
                        + key.getLeagueName() + " " + entry.getKey() + "(" + entry.getValue() + ")";
                if (!tournaments.contains(tournament))
                    tournaments.add(tournament);
            }
            if (!tournamentTypes.contains(leagueHashMapEntry.getKey().getCountry()))
                tournamentTypes.add(leagueHashMapEntry.getKey().getCountry());
        }
        sortCopyOnWriteArrayList(tournaments);
    }

    private void sortCopyOnWriteArrayList(ObservableList<String> copyOnWriteArrayList){
        ArrayList<String> temp = new ArrayList<>(copyOnWriteArrayList);
        temp.sort((o1, o2) -> {
            if (o1.equals("Все")) {
                return -1;
            } else if (o2.equals("Все")) {
                return 1;
            } else {
                return o1.compareTo(o2);
            }
        });
        Platform.runLater(()-> {
            copyOnWriteArrayList.clear();
            copyOnWriteArrayList.addAll(temp);
        });
    }

    private CopyOnWriteArrayList<TennisGame> getAllGames() {
        Session session = sessionFactory.openSession();
        GameDAO gameDao = new GameDAO(session);
        games = gameDao.getAllGames();
        session.close();
        return games;
    }

    public void saveGames(Set<TennisGame> gamesFull) {
        try {
            Session session = sessionFactory.openSession();
            GameDAO gameDAO = new GameDAO(session);
            gameDAO.saveGames(gamesFull);
            session.close();
            updateTournaments(gamesFull);
            updateBKNames();
        } catch (HibernateException e) {
            e.printStackTrace();
        }
    }

    private void updateBKNames() {
        try {
            Session session = sessionFactory.openSession();
            CoefsDAO coefsDAO = new CoefsDAO(session);
            Platform.runLater(() -> {
                coefsDAO.getAllBKNames().stream()
                        .filter(name -> !bookmakers.contains(name))
                        .forEach(name -> bookmakers.add(name));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveLeagues(CopyOnWriteArrayList<League> leagueList) {
        try {
            Session session = sessionFactory.openSession();
            LeagueDAO leagueDao = new LeagueDAO(session);
            leagueDao.saveLeagues(leagueList);
            session.close();
        } catch (HibernateException e) {
            e.printStackTrace();
        }
    }

    public CopyOnWriteArrayList<League> getAllLeagues() {
        Session session = sessionFactory.openSession();
        LeagueDAO leagueDAO = new LeagueDAO(session);
        this.leagues = leagueDAO.getAllLeagues();
        session.close();
        return leagues;
    }

    public CopyOnWriteArrayList<League> getLeagues() {
        return leagues;
    }

    public CopyOnWriteArrayList<TennisGame> getGames() {
        return games;
    }

    public ObservableList<String> getTournamentTypes() {
        return tournamentTypes;
    }

    public ObservableList<String> getTournaments() {
        return tournaments;
    }

    public ObservableList<String> getBookmakers() {
        return bookmakers;
    }

    public ConcurrentHashMap<League, HashMap<Integer, Integer>> getParsedLeagues() {
        return parsedLeagues;
    }

    private static SessionFactory createSessionFactory(Configuration configuration) {
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
        builder.applySettings(configuration.getProperties());
        ServiceRegistry serviceRegistry = builder.build();
        return configuration.buildSessionFactory(serviceRegistry);
    }

    private Configuration getH2Configuration() {
        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(League.class);
        configuration.addAnnotatedClass(TennisGame.class);
        configuration.addAnnotatedClass(Coefs.class);

        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        configuration.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        configuration.setProperty("hibernate.connection.url", "jdbc:h2:./tennis");
        configuration.setProperty("hibernate.connection.username", "test");
        configuration.setProperty("hibernate.connection.password", "test");
        configuration.setProperty("hibernate.connection.characterEncoding", "utf8");
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.format_sql", "true");
        configuration.setProperty("hibernate.use_sql_comments", "true");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        return configuration;
    }
}