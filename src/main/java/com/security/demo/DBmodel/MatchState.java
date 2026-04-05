package com.security.demo.DBmodel;


import com.security.demo.DBmodel.PlayerPoints;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
@Setter
@Getter
@Entity
@Table(name = "matchstatus")
public class MatchState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer Innings = 1;
    private Integer matchid;
    private Long timestamp;

    private String innings1;
    private String innings2;

    private String matchstatus = "Match Getting Started";

    private String tosswonby;

    private String team1;

    private String team2;

    private Boolean isannounced = false;

}
