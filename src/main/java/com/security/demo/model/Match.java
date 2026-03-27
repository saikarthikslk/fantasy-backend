package com.security.demo.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Match{
    private int matchId;
    private int seriesId;
    private String seriesName;
    private String matchDesc;
    private String matchFormat;
    private long startDate;
    private long endDate;
    private String state;
    private String status;

    // Child objects (fetched via matchId as FK in their tables)
    private Team team1;
    private Team team2;
    private Venue venueInfo;

    public Match() {}

    public Match(int matchId, int seriesId, String seriesName, String matchDesc,
                     String matchFormat, long startDate, long endDate,
                     String state, String status, Team team1, Team team2, Venue venueInfo) {
        this.matchId = matchId;
        this.seriesId = seriesId;
        this.seriesName = seriesName;
        this.matchDesc = matchDesc;
        this.matchFormat = matchFormat;
        this.startDate = startDate;
        this.endDate = endDate;
        this.state = state;
        this.status = status;
        this.team1 = team1;
        this.team2 = team2;
        this.venueInfo = venueInfo;
    }

    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }

    public int getSeriesId() { return seriesId; }
    public void setSeriesId(int seriesId) { this.seriesId = seriesId; }

    public String getSeriesName() { return seriesName; }
    public void setSeriesName(String seriesName) { this.seriesName = seriesName; }

    public String getMatchDesc() { return matchDesc; }
    public void setMatchDesc(String matchDesc) { this.matchDesc = matchDesc; }

    public String getMatchFormat() { return matchFormat; }
    public void setMatchFormat(String matchFormat) { this.matchFormat = matchFormat; }

    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }

    public long getEndDate() { return endDate; }
    public void setEndDate(long endDate) { this.endDate = endDate; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Team getTeam1() { return team1; }
    public void setTeam1(Team team1) { this.team1 = team1; }

    public Team getTeam2() { return team2; }
    public void setTeam2(Team team2) { this.team2 = team2; }

    public Venue getVenueInfo() { return venueInfo; }
    public void setVenueInfo(Venue venueInfo) { this.venueInfo = venueInfo; }

    @Override
    public String toString() {
        return "MatchInfo{matchId=" + matchId + ", matchDesc='" + matchDesc +
                "', team1=" + team1 + ", team2=" + team2 + ", venue=" + venueInfo + "}";
    }
}