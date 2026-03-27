package com.security.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PLayerwrapper {
    @JsonProperty("player")
    List<Player> players;
 }
