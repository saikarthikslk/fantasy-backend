package com.security.demo.model;

import com.security.demo.DBmodel.PlayerEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class TeamPoints {
    private List<Playerchosen> playerEntities;
    private String captain;
    private String vcaptain;
    private Integer did;
    private Integer matchid;
    private Double totalpoints;
    private String name ;
    private String email;
    private Integer position;
    private byte[] imageurl;
}
