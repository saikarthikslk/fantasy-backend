package com.security.demo.DBmodel;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.parameters.P;

@Entity
@Getter
@Table(name = "player_stats")
@Setter
public class PlayerPoints {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String playerid;
    private Integer matchid;

    private Double totalpoints =0.0;

    private Integer score  = 0;

    //batsman
    private Integer runs =0;
    private Integer ballplayed =0;
    private Integer fours =0 ;
    private Integer sixes=0;
    private Double strikerate=0.0;


    //bowler

    private Integer wickets = 0;
    private Integer ballsbowled =0;
    private Integer scoregiven = 0 ;
    private Integer maidens=0;
    private Double eco= 0.0;
    private Integer wides = 0;
    private Integer nb= 0;


    //type of out
    private Integer catches =0 ;
    private Integer runouts =0;
    private Integer stumpouts =0;
    private Integer stumping =0;
    private Integer bowled = 0 ;
    private Integer lbw =0 ;
    private Integer catchouts =0;


    private boolean out = false;
    private String type = "";
    private String dismissal = "";
    public void initializeDefaults() {
        this.totalpoints = 0.0;
        this.score = 0;

        // batsman
        this.runs = 0;
        this.ballplayed = 0;
        this.fours = 0;
        this.sixes = 0;
        this.strikerate = 0.0;

        // bowler
        this.wickets = 0;
        this.ballsbowled = 0;
        this.scoregiven = 0;
        this.maidens = 0;
        this.eco = 0.0;
        this.wides = 0;
        this.nb = 0;

        // type of out
        this.catches = 0;
        this.runouts = 0;
        this.stumpouts = 0;
        this.stumping = 0;
        this.bowled = 0;
        this.lbw = 0;
        this.catchouts = 0;

        this.out = false;
        this.type = "";
        this.dismissal = "";
    }
}
