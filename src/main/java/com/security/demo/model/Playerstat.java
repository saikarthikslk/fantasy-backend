package com.security.demo.model;

import lombok.*;

import java.math.BigInteger;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Playerstat {

    private String team1;
    private String team2;
    private Integer ballsbowled;
    private String playerid;
    private Double score;
    private Integer ballplayed;
    private Integer wickets;
    private double eco;
    private Integer scoregiven;
    private long pos;
}

