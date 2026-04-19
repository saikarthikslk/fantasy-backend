package com.security.demo.DBmodel;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;
import java.sql.Timestamp;

@Table(name = "activity")
@Entity
@Getter
@Setter
public class Acitivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String track;
    private Timestamp timestamp;

    private String email;
}
