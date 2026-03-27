package com.security.demo.model;

import com.security.demo.DBmodel.CustomTeamEntity;
import com.security.demo.DBmodel.PlayerEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MatchSelection {
    private List<PlayerEntity> players;
    private CustomTeamEntity dreamTeam;
}
