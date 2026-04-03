package com.security.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamWrapper {
    private MatchTeam team;
    private List<PlayerCategory> players;
}