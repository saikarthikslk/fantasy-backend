package com.security.demo.model;

public class Team {
    private int teamId;
    private String teamName;
    private String teamSName;
    private int imageId;

    public Team() {}

    public Team(int teamId, String teamName, String teamSName, int imageId) {
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

    @Override
    public String toString() {
        return "Team{teamId=" + teamId + ", teamName='" + teamName + "', teamSName='" + teamSName + "'}";
    }
}