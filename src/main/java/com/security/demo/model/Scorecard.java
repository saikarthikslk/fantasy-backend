package com.security.demo.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class Scorecard {
    private Team t1;
    private Team t2;
    private String innings1score;
    private String innings2score;
    private String winmessage;

}
