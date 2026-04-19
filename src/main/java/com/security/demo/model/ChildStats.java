package com.security.demo.model;

import com.security.demo.DBmodel.TeamEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ChildStats {

    private Integer matchid;
    private Integer did;
    private Integer position;
    private TeamEntity t1;
    private TeamEntity t2;
    private Long timestamp;
    private Double points;
    private Boolean isauto ;
}
