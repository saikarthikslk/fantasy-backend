package com.security.demo.model;


import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class UploadData
{

    private String name ;
    private byte[] image;

    private Boolean autoteam;
}
