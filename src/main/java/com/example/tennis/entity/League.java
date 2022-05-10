package com.example.tennis.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter @Setter @ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class League {
    @Id @EqualsAndHashCode.Include
    String mtCode;
    String country, countryCode, leagueName, leagueCode;
}
