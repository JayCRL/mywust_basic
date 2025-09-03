package com.linghang.backend.mywust_basic.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UnderGraduateLoginA {
    private String username;
    private String password;
    public String getUsername(){
        return  this.username;
    }
    public String getPassword(){
        return  this.password;
    }
}
