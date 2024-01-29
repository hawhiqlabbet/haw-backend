package com.example.springboot.models;

import lombok.Data;

@Data
public abstract class StartGameMessage {
    String username;
    String gameId;
    long endTime;
    long endTimeConst;
    long endVoteTime;
    long endVoteTimeConst;

    public StartGameMessage(String username, String gameId, long endTime, long endVoteTime) {
        this.username         = username;
        this.gameId           = gameId;
        this.endTime          = endTime;
        this.endTimeConst     = endTime;
        this.endVoteTime      = endVoteTime;
        this.endVoteTimeConst = endVoteTime;
    }

    public StartGameMessage() {

    }
}
