package com.security.demo.DBmodel;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "team")
public class TeamEntity implements Serializable {

    @Id
    @Column(name = "team_id")
    private int teamId;




    @Column(name = "team_name")
    private String teamName;

    @Column(name = "team_s_name")
    private String teamSName;

    @Column(name = "image_id")
    private int imageId;

    public TeamEntity() {}

    public TeamEntity(int teamId, String teamName, String teamSName, int imageId) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamSName = teamSName;
        this.imageId = imageId;
    }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getTeamSName() { return teamSName; }
    public void setTeamSName(String teamSName) { this.teamSName = teamSName; }

    public int getImageId() { return imageId; }
    public void setImageId(int imageId) { this.imageId = imageId; }
}