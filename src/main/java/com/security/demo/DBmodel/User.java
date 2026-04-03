package com.security.demo.DBmodel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity()
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id ;
    public String email;
    public String name;
    private Timestamp created_at;
    private String gamename;
    private byte[] profielpic;
}
