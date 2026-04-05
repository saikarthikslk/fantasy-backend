package com.security.demo.DBmodel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "match_info")
@Getter
@Setter
public class MatchInfoEntity implements Serializable {

    @Id
    @Column(name = "match_id")
    private int matchId;

    @Column(name = "series_id")
    private int seriesId;

    @Column(name = "series_name")
    private String seriesName;

    @Column(name = "match_desc")
    private String matchDesc;

    @Column(name = "match_format")
    private String matchFormat;

    @Column(name = "start_date")
    private long startDate;

    @Column(name = "end_date")
    private long endDate;

    @Column(name = "state")
    private String state;

    @Column(name = "status")
    private String status;

    @Column(name = "isloaded")
    private Integer isloaded = 0 ;

    // FK → team.team_id (team1)
    @ManyToOne
    @JoinColumn(name = "team1_id", referencedColumnName = "team_id")
    private TeamEntity team1;

    // FK → team.team_id (team2)
    @ManyToOne
    @JoinColumn(name = "team2_id", referencedColumnName = "team_id")
    private TeamEntity team2;

    // FK → venue.id
    @ManyToOne
    @JoinColumn(name = "venue_id", referencedColumnName = "id")
    private VenueEntity venueInfo;

    private Boolean isannounced = false;


    public MatchInfoEntity() {}

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

    public TeamEntity getTeam1() { return team1; }
    public void setTeam1(TeamEntity team1) { this.team1 = team1; }

    public TeamEntity getTeam2() { return team2; }
    public void setTeam2(TeamEntity team2) { this.team2 = team2; }

    public VenueEntity getVenueInfo() { return venueInfo; }
    public void setVenueInfo(VenueEntity venueInfo) { this.venueInfo = venueInfo; }
}