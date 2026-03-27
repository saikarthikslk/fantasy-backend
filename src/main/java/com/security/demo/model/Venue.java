package com.security.demo.model;

public class Venue {
    private int id;
    private String ground;
    private String city;
    private String timezone;

    public Venue() {}

    public Venue(int id, String ground, String city, String timezone) {
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

    @Override
    public String toString() {
        return "Venue{id=" + id + ", ground='" + ground + "', city='" + city + "', timezone='" + timezone + "'}";
    }
}