package com.example.tennis.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
public class TennisGame {
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "mtCode")
    League league;
    String gamer1, gamer2;
    int score1, score2, rank1, rank2, season;
    @Transient
    double coef1, coef2;

    @OneToMany(cascade = CascadeType.ALL,
            mappedBy = "tennisGame",
            fetch = FetchType.LAZY)
    List<Coefs> coefs;
    String scoreStr;//score by sets score11-score12;score21-score22;...
    @Column(length = 450)
    String scorePointByPoint;
    @Id
    String gameId;
    long dateInSeconds;

    String country1, country2, stadium;

    @Transient
    int favorite = 0;

    public TennisGame(League league, String gamer1, String gamer2, int score1, int score2, int season, String scoreStr,
                      String gameId, long dateInSeconds, String country1, String country2, String stadium) {
        this.league = league;
        this.gamer1 = gamer1;
        this.gamer2 = gamer2;
        this.season = season;
        this.score1 = score1;
        this.score2 = score2;
        this.scoreStr = scoreStr;
        this.gameId = gameId;
        this.dateInSeconds = dateInSeconds;
        this.country1 = country1;
        this.country2 = country2;
        this.stadium = stadium;
    }

    public void setCoefs(List<Coefs> coefs) {
        this.coefs = coefs;
        for (Coefs coef : coefs) {
            coef.setTennisGame(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TennisGame that = (TennisGame) o;
        return gameId != null && Objects.equals(gameId, that.gameId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
