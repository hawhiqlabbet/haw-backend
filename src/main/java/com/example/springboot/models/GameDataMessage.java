package com.example.springboot.models;

import lombok.Data;

@Data
public abstract class GameDataMessage {
     long endTime;
     long endTimeConst;
     long endVoteTime;
     long endVoteTimeConst;

     String gameChoice;

    public GameDataMessage() {

    }
    public GameDataMessage(long endTime, long endTimeConst, long endVoteTime, long endVoteTimeConst, String gameChoice) {
        this.endTime          = endTime;
        this.endTimeConst     = endTimeConst;
        this.endVoteTime      = endVoteTime;
        this.endVoteTimeConst = endVoteTimeConst;
        this.gameChoice       = gameChoice;
    }
}
