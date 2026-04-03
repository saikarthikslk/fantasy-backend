package com.security.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchData {
    private TeamWrapper team1;
    private TeamWrapper team2;

    public static class TeamScore {
        private Integer matchid;
        private Map<String,Object> total = new HashMap<>();
    }
}