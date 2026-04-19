package com.security.demo.model;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class Tracking {

    private String host;
    private String addr;
    private String path;
    private String body;
}
