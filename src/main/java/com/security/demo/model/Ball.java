package com.security.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Table(name = "ball")
@Entity
@Setter
public class Ball {
    @Id
    private Integer id;
    private String ball;
    private Integer score;
    private String team;
    private Integer innings;
    private Integer wickets;
    private Integer totalruns;
    private Integer legbyes;
    private Integer boundary;
    private Integer runsran;
    private String type;
    private Integer isnoball=0;
    private Integer iswide=0;
    private String p1;
    private String bowler;
    private String p2;
    private String wicketype;
    private String catchp;
    private String runoutp;
    private String stumpout;
    private boolean isLegalBall = true;
    private String playerout;
    private String commentary;
    private Long timestamp;
}
