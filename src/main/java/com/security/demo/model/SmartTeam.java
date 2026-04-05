package com.security.demo.model;


import com.security.demo.DBmodel.PlayerEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class SmartTeam {

    private List<PlayerEntity> players;
    private String captain;
    private String vicecaptain;
}
