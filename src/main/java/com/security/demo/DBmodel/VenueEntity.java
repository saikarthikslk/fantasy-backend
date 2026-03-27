package com.security.demo.DBmodel;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "venue")
public class VenueEntity implements Serializable {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "ground")
    private String ground;

    @Column(name = "city")
    private String city;

    @Column(name = "timezone")
    private String timezone;

    public VenueEntity() {}

    public VenueEntity(int id, String ground, String city, String timezone) {
        this.id = id;
        this.ground = ground;
        this.city = city;
        this.timezone = timezone;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getGround() { return ground; }
    public void setGround(String ground) { this.ground = ground; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
}