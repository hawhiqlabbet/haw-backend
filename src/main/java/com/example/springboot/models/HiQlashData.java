package com.example.springboot.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Data
public class HiQlashData implements LobbyData{
    boolean answeringPrompts;
    int currRound;
    int numPlayers;
    int numRounds;
    long endTime;
    long endTimeConst;
    long endVoteTime;
    long endVoteTimeConst;
    List<PlayerPrompts> promptsForPlayers;
    List<String> prompts;
    List<PlayerScores> playerScores;
    List<String> usedPrompts;

    public List<SpyQData.VotingObject> votingObjectList;
    public List<SpyQData.HasVoted> hasVotedList;

    // Viewing state
    String currentPrompt;
    List<String> currentAnswers;
    List<String> currentPlayers;
    List<String> votedForOne;
    List<String> votedForTwo;

    public HiQlashData(int numPlayers, List<Player> players, List<SpyQData.VotingObject> votingObject, List<SpyQData.HasVoted> hasVoted, long endTime, long endVoteTime, List<String> prompts) {

        this.answeringPrompts = true;
        this.currRound        = 1;
        this.numPlayers       = numPlayers;
        this.numRounds        = numPlayers;
        this.endTime          = endTime;
        this.endTimeConst     = endTime;
        this.endVoteTime      = endVoteTime;
        this.endVoteTimeConst = endVoteTime;

        this.promptsForPlayers = new ArrayList<PlayerPrompts>();
        this.prompts           = prompts;
        this.playerScores      = new ArrayList<PlayerScores>();
        this.usedPrompts       = new ArrayList<String>();

        this.votingObjectList = votingObject;
        this.hasVotedList     = hasVoted;

        // Viewing state
        this.currentPrompt = "";
        this.currentAnswers = new ArrayList<String>();
        this.currentPlayers = new ArrayList<String>();
        this.votedForOne    = new ArrayList<String>();
        this.votedForTwo    = new ArrayList<String>();

        Collections.shuffle(this.prompts);
        Collections.shuffle(players);


        for(int i = 0; i < numPlayers; ++i) {
            String playerName = players.get(i).getUsername();
            PlayerPrompts playerPrompts = new PlayerPrompts(playerName);

            // Initialize scores
            this.playerScores.add(new PlayerScores(playerName, 0));

            // Determine prompts for each player
            for(int j = 0; j < min(2, this.numPlayers); ++j) {
                int promptIndex = i + j;
                if(i == (numPlayers - 1) && j == 1) {
                    promptIndex = 0;
                }
                String currPrompt = prompts.get(promptIndex);

                // Replace with names included
                if (currPrompt.contains("<--->")) {
                    Random random = new Random();
                    boolean done = false;
                    String randomizedPlayerInLobby = players.get(random.nextInt(numPlayers - 1)).getUsername();
                    currPrompt = currPrompt.replace("<--->", randomizedPlayerInLobby);
                }

                playerPrompts.prompts.add(currPrompt);
                playerPrompts.promptAnswers.add("");
                if(!usedPrompts.contains(currPrompt)) {
                    usedPrompts.add(currPrompt);
                }
            }

            // Add unique prompts to the player
            promptsForPlayers.add(playerPrompts);
        }
        System.err.println("USED PROMPTS: " + this.usedPrompts);

        Collections.shuffle(this.usedPrompts);
    }

    @Data
    public static class PlayerScores {
        String player;
        int votes;

        public PlayerScores (String username, int score){
            this.player = username;
            this.votes  = score;
        }
    }
    @Data
    public static class PlayerPrompts {
        List<String> promptAnswers;
        List<String> prompts;
        String player;
        boolean hasAnswered;

        public PlayerPrompts(String player){
            this.promptAnswers = new ArrayList<String>();
            this.prompts       = new ArrayList<String>();
            this.player        = player;
            this.hasAnswered   = false;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class StartGameMessage extends com.example.springboot.models.StartGameMessage {
        private List<String> prompts;

        public StartGameMessage(String username, String gameId, long endTime, long endVoteTime, List<String> prompts) {
            super(username, gameId, endTime, endVoteTime);
            this.prompts = prompts;
        }

        public StartGameMessage() {
            super();
            this.prompts = new ArrayList<String>();
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class GameDataMessage extends com.example.springboot.models.GameDataMessage {
        List<SpyQData.VotingObject> votingObject;
        List<String> prompts;
        boolean hasAnswered;
        boolean hasAllAnswered;
        boolean hasVoted;
        boolean hasAllVoted;

        // Viewing state
        String currentPrompt;
        List<String> currentAnswers;
        List<String> currentPlayers;
        List<String> votedForOne;
        List<String> votedForTwo;

        public GameDataMessage(long endTime, long endTimeConst, long endVoteTime, long endVoteTimeConst, String gameChoice, List<SpyQData.VotingObject> votingObject, List<String> prompts){
            super(endTime, endTimeConst, endVoteTime, endVoteTimeConst, gameChoice);
            this.votingObject = votingObject;
            this.prompts = prompts;
            this.hasAnswered = false;
            this.hasAllAnswered = false;
            this.hasVoted = false;
            this.hasAllVoted = false;

            this.currentPrompt = "";
            this.currentAnswers = new ArrayList<String>();
            this.currentPlayers = new ArrayList<String>();
            this.votedForOne    = new ArrayList<String>();
            this.votedForTwo    = new ArrayList<String>();

        }
    }

    public List<String> getPlayerPrompts(String username){
        for(PlayerPrompts p: promptsForPlayers) {
            if(username.equals(p.player)){
                return p.prompts;
            }
        }
        System.err.println("Player " + username + " does not have any prompts");
        return new ArrayList<String>();
    }

    public List<String> getPlayersHavingPrompt(String inputPrompt){
        List<String> players = new ArrayList<String>();
        for(PlayerPrompts playerPrompts: promptsForPlayers) {
            for(String prompt : playerPrompts.getPrompts())
                if(prompt.equals(inputPrompt)){
                    players.add(playerPrompts.getPlayer());
                    if(players.size() == 2)
                        return players;
            }
        }
        System.err.println("Number of players having prompt " + inputPrompt + " is not 2");
        return players;
    }

    public void setAnswer(String player, String prompt, String answer) {
        for(PlayerPrompts p : promptsForPlayers) {
            if(p.getPlayer().equals(player)) {
                p.promptAnswers.set(p.getPrompts().indexOf(prompt), answer);
                p.setHasAnswered(true);
            }
        }
    }

    public String getAnswer(String player, String prompt) {
        for(PlayerPrompts p : promptsForPlayers) {
            if(p.getPlayer().equals(player)) {
                for(int i = 0; i < p.getPrompts().size(); ++i) {
                    if(p.getPrompts().get(i).equals(prompt)) {
                        return p.getPromptAnswers().get(i);
                    }
                }
            }
        }
        System.err.println("Player " + player + " does not have prompt " + prompt);
        return "";
    }

    public boolean hasPlayerAnswered(String player) {
        for (PlayerPrompts p : promptsForPlayers) {
            if (p.getPlayer().equals(player)) {
                return p.isHasAnswered();
            }
        }
        System.err.println("Player " + player + " does not exist");
        return false;
    }

    public boolean hasAllPlayersAnswered(){
        for(PlayerPrompts p : this.promptsForPlayers) {
            for(String promptAnswer : p.getPromptAnswers()){
                if(promptAnswer.isEmpty())
                    return false;
            }
        }
        return true;
    }

    public boolean hasPlayerVoted(String player){
        for(SpyQData.HasVoted obj : hasVotedList) {
            if(obj.player.equals(player)) {
                return obj.hasVoted;
            }
        }
        return false;
    }

    public void setVoted(String player){
        for(SpyQData.HasVoted obj : hasVotedList) {
            if(obj.player.equals(player)) {
                obj.hasVoted = true;
                return;
            }
        }
    }

    public void setVotedFor(String player, String votedFor){
        if(currentPlayers.get(0).equals(votedFor)){
            votedForOne.add(player);
        }
        else if(currentPlayers.get(1).equals(votedFor)) {
            votedForTwo.add(player);
        }
        else {
            System.err.println("Player " + player + " Cannot vote for " + votedFor);
        }
    }

    public void resetVotes() {
        votedForOne.clear();
        votedForTwo.clear();

        for(SpyQData.HasVoted obj : hasVotedList) {
            obj.hasVoted = false;
        }

        for(SpyQData.VotingObject obj : votingObjectList) {
            obj.votes = 0;
        }
    }

    public void incrementVotes(String player) {
        for(SpyQData.VotingObject obj : votingObjectList) {
            if(obj.player.equals(player)) {
                obj.votes += 1;
                return;
            }
        }
    }

    public boolean everyoneHasVoted(){
        for (SpyQData.HasVoted obj : hasVotedList) {
            if (!obj.hasVoted) {
                return false; // If any player hasn't voted, return false
            }
        }
        return true; // If all players have voted, return true
    }

    public void calculateScores() {
        for (SpyQData.VotingObject obj : votingObjectList) {
            String player = obj.player;
            int score     = obj.votes * 100;

            for(PlayerScores scores : playerScores) {
                if(scores.player.equals(player)){
                    scores.votes += score;
                }
            }
        }
    }

    public String getPlayerMostVotes() {
        int maxVotes = Integer.MIN_VALUE;
        String mostVotes = "";

        for (SpyQData.VotingObject obj : votingObjectList) {
            if (obj.votes > maxVotes) {
                maxVotes = obj.votes;
                mostVotes = obj.player;
            }
        }
        return mostVotes;
    }

    public void sortVotingObjectsByVotes() {
        votingObjectList.sort(new Comparator<SpyQData.VotingObject>() {
            @Override
            public int compare(SpyQData.VotingObject vo1, SpyQData.VotingObject vo2) {
                // Compare based on the number of votes in descending order
                return vo2.votes - vo1.votes;
            }
        });
    }
}
