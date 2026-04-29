package com.security.demo.DBmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

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
    private Boolean autoteam;

    private byte[] profielpic;
    private String num;
    private String replacementtype = "score";
    private Boolean isactive= true;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "email",referencedColumnName = "email")
    private List<Acitivity> logs;
}
