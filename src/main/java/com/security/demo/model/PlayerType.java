package com.security.demo.model;


import lombok.Getter;
import lombok.Setter;

@Getter
public enum PlayerType {
    AR("AR"),BAT("BAT"),BOWL("BOWL"),WK("WK"),SUB("SUB");
    public final String type;
     PlayerType(String type){
        this.type = type;
    }

}
