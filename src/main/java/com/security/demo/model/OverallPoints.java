package com.security.demo.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class OverallPoints {
    private List<ChildStats> stats;
    private Double totalpoints = 0.0;
    private String name ;
    private String email;
    private byte[] imageurl;
}
