package com.security.demo.DBmodel;

import com.security.demo.model.DreamTeam;
import com.security.demo.model.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "player")
@Getter
@Setter
public class PlayerEntity implements Serializable {

    @Id
    @Column(name = "id")
    private String id;        // kept as String since JSON has "1413" as string

    @Column(name = "name")
    private String name;

    @Column(name = "image_id")
    private int imageId;

    @Column(name = "batting_style")
    private String battingStyle;

    @Column(name = "bowling_style")
    private String bowlingStyle;


    @Column(name = "type")
    private String type;

    @ManyToOne
    @JoinColumn(name = "team_id", referencedColumnName = "team_id")
    private TeamEntity team;

    public PlayerEntity() {}

    public PlayerEntity(String id, String name, int imageId, String battingStyle, String bowlingStyle) {
        this.id = id;
        this.name = name;
        this.imageId = imageId;
        this.battingStyle = battingStyle;
        this.bowlingStyle = bowlingStyle;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getImageId() { return imageId; }
    public void setImageId(int imageId) { this.imageId = imageId; }

    public String getBattingStyle() { return battingStyle; }
    public void setBattingStyle(String battingStyle) { this.battingStyle = battingStyle; }

    public String getBowlingStyle() { return bowlingStyle; }
    public void setBowlingStyle(String bowlingStyle) { this.bowlingStyle = bowlingStyle; }
}