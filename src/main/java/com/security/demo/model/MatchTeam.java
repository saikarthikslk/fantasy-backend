package com.security.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchTeam {
    private int teamid;
    private String teamname;
    private String teamsname;
    private boolean isfullmember;
    private boolean isassociated;
    private boolean isleagueteam;
    private boolean iswomenteam;
    private boolean isheader;
    private boolean isactive;
    private String teampriority;
    private boolean isvideopresent;
    private int imageid;
    private String countryname;
    private String belongsto;
    private String teamcolor;
}