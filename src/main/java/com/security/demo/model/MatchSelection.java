package com.security.demo.model;

import com.security.demo.DBmodel.CustomTeamEntity;
import com.security.demo.DBmodel.PlayerEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class MatchSelection {
    private List<PlayerEntity> players;
    private CustomTeamEntity dreamTeam;
    private SmartTeam smartTeam;
    private boolean isannounced = false;
    private Integer views = 0;
    private Map<String,List<Playerstat>> stats = new HashMap<>();
}
