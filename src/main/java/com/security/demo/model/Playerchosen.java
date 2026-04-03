package com.security.demo.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Data
public class Playerchosen {
    private String playerid;
    private Double points;
    private String team;
    private String name;
    private String type;
    private String url;

}


