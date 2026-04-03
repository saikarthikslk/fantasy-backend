package com.security.demo.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class Playerdata {
    private String id;
    private String name;
    private boolean captain;
    private String role;
    private boolean keeper;
    private String teamname;
    private boolean isheader;
    private int imageId;
    private String battingStyle;
    private String bowlingStyle;
    private int faceimageid;
    private int countryimageid;
    private String playingxichange;
    private String inmatchchange;
    private boolean isoverseas;
}
