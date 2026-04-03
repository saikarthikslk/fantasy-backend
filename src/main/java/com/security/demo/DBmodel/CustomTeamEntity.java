package com.security.demo.DBmodel;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "customteams")
@Getter
@Setter
@NoArgsConstructor
public class CustomTeamEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String email;
    private String team;
    private Integer match_id;
    private Timestamp created_at;


}
