package com.example.springboot.models;

import com.example.springboot.models.LobbyData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class SpyQData implements LobbyData {
    public String spyName;
    public String country;
    public List<VotingObject> votingObjectList;
    public List<HasVoted> hasVotedList;
    public boolean foundSpy;
    public long endTime;
    public long endTimeConst;
    public long endVoteTime;
    public long endVoteTimeConst;

    public SpyQData(String spyName, String country, List<VotingObject> votingObject, List<HasVoted> hasVoted,
                    boolean foundSpy, long endTime, long endVoteTime){
        this.spyName            = spyName;
        this.country            = country;
        this.votingObjectList   = votingObject;
        this.hasVotedList       = hasVoted;
        this.foundSpy           = foundSpy;
        this.endTime            = endTime;
        this.endTimeConst       = endTime;
        this.endVoteTime        = endVoteTime;
        this.endVoteTimeConst   = endVoteTime;
    }

    public static class VotingObject {
        public String player;
        public int votes;
        public VotingObject(String player, int votes) {
            this.player = player;
            this.votes = votes;
        }
    }

    public static class HasVoted {
        public String player;
        public boolean hasVoted;
        public HasVoted(String player, boolean hasVoted){
            this.player = player;
            this.hasVoted = hasVoted;
        }
    }
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class StartGameMessage extends com.example.springboot.models.StartGameMessage {
        private String country;

        public StartGameMessage(String username, String gameId, long endTime, long endVoteTime, String country) {
            super(username, gameId, endTime, endVoteTime);
            this.country = country;
        }

        public StartGameMessage() {
            super();
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class GameDataMessage extends com.example.springboot.models.GameDataMessage {
        private String country;
        private boolean foundSpy;
        private String spyName;
        List<SpyQData.VotingObject> votingObject;

        public GameDataMessage(String country, long endTime, long endTimeConst, long endVoteTime, long endVoteTimeConst, String gameChoice, boolean foundSpy, String spyName, List<SpyQData.VotingObject> votingObject){
            super(endTime, endTimeConst, endVoteTime, endVoteTimeConst, gameChoice);
            this.country = country;
            this.foundSpy = foundSpy;
            this.spyName = spyName;
            this.votingObject = votingObject;
        }
    }

    public boolean hasPlayerVoted(String player){
        for(HasVoted obj : hasVotedList) {
            if(obj.player.equals(player)) {
                return obj.hasVoted;
            }
        }
        return false;
    }

    public void setVoted(String player){
        for(HasVoted obj : hasVotedList) {
            if(obj.player.equals(player)) {
                obj.hasVoted = true;
                return;
            }
        }
    }

    public void incrementVotes(String player) {
        for(VotingObject obj : votingObjectList) {
            if(obj.player.equals(player)) {
                obj.votes += 1;
                return;
            }
        }
    }

    public boolean everyoneHasVoted(){
        for (HasVoted obj : hasVotedList) {
            if (!obj.hasVoted) {
                return false; // If any player hasn't voted, return false
            }
        }
        return true; // If all players have voted, return true
    }

    public String getPlayerMostVotes() {
        int maxVotes = Integer.MIN_VALUE;
        String mostVotes = "";

        for (VotingObject obj : votingObjectList) {
            if (obj.votes > maxVotes) {
                maxVotes = obj.votes;
                mostVotes = obj.player;
            }
        }
        return mostVotes;
    }

    public void sortVotingObjectsByVotes() {
        votingObjectList.sort(new Comparator<VotingObject>() {
            @Override
            public int compare(VotingObject vo1, VotingObject vo2) {
                // Compare based on the number of votes in descending order
                return vo2.votes - vo1.votes;
            }
        });
    }
}