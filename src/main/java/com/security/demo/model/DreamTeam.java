package com.security.demo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DreamTeam {
    private Integer matchid;
    private List<SelectedPlayer> properties;
}
