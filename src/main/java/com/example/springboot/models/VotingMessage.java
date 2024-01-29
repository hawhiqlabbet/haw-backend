package com.example.springboot.models;

import lombok.Data;

@Data
public class VotingMessage {
    private String gameId;
    private String username;
    private String votedFor;
}
