package com.security.demo.model;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
@Setter
@Getter
public class MatchState {
    private Integer over = 0 ;
    private Integer ball = 0;
    private Integer Innings = 1;
    private String teamid;
    private Integer currentscore =0;
    private Integer currentwickets = 0;
    private List<Ball> innings1state = new ArrayList<>();
    private List<Ball> innings2state = new ArrayList<>();
    private Integer matchid;
    private Long timestamp;


}
