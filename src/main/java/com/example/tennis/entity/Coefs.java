package com.example.tennis.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@ToString(exclude = {"tennisGame"})
@Entity
@NoArgsConstructor
public class Coefs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "fk_game")
    private TennisGame tennisGame;

    private String bookmaker;
    private double coef1, coef2;

    public Coefs(String bookmaker, double coef1, double coef2) {
        this.bookmaker = bookmaker;
        this.coef1 = coef1;
        this.coef2 = coef2;
    }
}
