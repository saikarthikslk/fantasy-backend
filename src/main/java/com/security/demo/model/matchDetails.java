package com.security.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class matchDetails {

    @JsonProperty("matchDetailsMap")
    private matchDetailsMap matchDetailsMap;
}
