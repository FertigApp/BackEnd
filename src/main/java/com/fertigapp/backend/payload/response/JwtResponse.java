package com.fertigapp.backend.payload.response;

import java.util.List;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String name;
    private String email;
    private List<String> roles;

    public JwtResponse(String token, String username, String name, String email, List<String> roles) {
        this.token = token;
        this.username = username;
        this.name = name;
        this.email = email;
        this.roles = roles;
    }

    public String getAccess_token() {
        return token;
    }

    public void setAccess_token(String accessToken) {
        this.token = accessToken;
    }

    public String getTokenType() {
        return type;
    }

    public void setTokenType(String tokenType) {
        this.type = tokenType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getName() {return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
