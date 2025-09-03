package com.linghang.backend.mywust_basic.Entity;

import lombok.Data;

@Data
public class GetCourseEntity {
    private String cookie;
    private String term;

    public String getCookie() {
        return cookie;
    }

    public String getTerm() {
        return term;
    }
}
