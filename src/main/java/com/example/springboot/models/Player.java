package com.example.springboot.models;

import lombok.Data;

@Data
public class Player {
    private String username;
    private String imageUrl;

    public Player(String username, String imageUrl) {
        this.username = username;
        this.imageUrl = imageUrl;
    }
}
