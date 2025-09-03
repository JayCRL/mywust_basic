package com.linghang.backend.mywust_basic.Entity;
public class UserInfo {
    String token;
    String cookie;

    public UserInfo(String token, String cookie) {
        this.token = token;
        this.cookie = cookie;
    }

    public String getToken() {
        return token;
    }

    public String getCookie() {
        return cookie;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
