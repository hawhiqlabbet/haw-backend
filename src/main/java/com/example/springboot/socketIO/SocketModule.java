package com.example.springboot.socketIO;

import ch.qos.logback.core.joran.sanity.Pair;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.example.springboot.models.*;
import com.example.springboot.services.LobbyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@EnableAsync
@Slf4j
@Component
public class SocketModule {

    private final SocketIOServer server;
    private final SocketService socketService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private LobbyService lobbyService;

    @Data
    public static class SocketRecieve {
        String username;
        String gameId;
        String imageUrl;
    }

    @Data
    public static class SocketSend {
        private String message;
        private String gameId;
        private String username;
        private List<Player> data;

        public SocketSend() {
        }

        public SocketSend(String message, List<Player> data, String gameId, String username) {
            this.message = message;
            this.data = data;
            this.gameId = gameId;
            this.username = username;
        }
    }



    @Autowired
    public SocketModule(SocketIOServer server, SocketService socketService) {
        this.server = server;
        this.socketService = socketService;
        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());
        server.addEventListener("hostGame", SocketRecieve.class, handleHostGame());
        server.addEventListener("newRound", SocketRecieve.class, handleNewRound());
        server.addEventListener("closeLobby", SocketRecieve.class, handleCloseLobby());
        server.addEventListener("leaveGame", SocketRecieve.class, handleLeaveGame());
        server.addEventListener("startGame", SocketRecieve.class, handleStartGame());
        server.addEventListener("joinGame", SocketRecieve.class, handleJoinGame());
        server.addEventListener("reportVotingDone", SocketRecieve.class, handleVotingDone());
        server.addEventListener("reportHiQlashAnswersDone", SocketRecieve.class, handleHiQlashAnswersDone());
        server.addEventListener("reportHiQlashVotingDone", SocketRecieve.class, handleHiQlashVotingDone());
    }

    @Async
    @Scheduled(fixedRate = 1000) // Update every second
    public void updateTimers() {
        for(Map.Entry<String, GameLobby> entry: lobbyService.getActiveLobbies().entrySet()) {
            GameLobby lobby = entry.getValue();
            lobby.setTimeout(lobby.getTimeout() - 1);

            // If lobby has not started, just check if timeout and go next
            if(lobbyService.getLobbyData(entry.getKey()) == null) {
                if(lobby.getTimeout() <= 0){
                    lobbyService.removeLobby(entry.getKey());
                    lobbyService.removeLobbyData(entry.getKey());
                }
                continue;
            }

            // All clients in lobby
            Collection<SocketIOClient> clients = server.getRoomOperations(entry.getKey()).getClients();

            // Reduce timers depending on which game choice.
            if(lobbyService.getGameLobby(entry.getKey()).getGameChoice().equals("SpyQ")) {
                SpyQData lobbyData = (SpyQData) lobbyService.getLobbyData(entry.getKey());
                lobbyData.endTime -= 1;
                lobbyData.endVoteTime -= 1;
                socketService.sendMessageCollection("timeUpdateEvent", clients, Map.of("endTime", lobbyData.endTime, "endVoteTime", lobbyData.endVoteTime));
            }
            else if(lobbyService.getGameLobby(entry.getKey()).getGameChoice().equals("HiQlash")) {
                HiQlashData lobbyData = (HiQlashData) lobbyService.getLobbyData(entry.getKey());

                // If answering prompts, decrement endTime until end which should end first phase
                if(lobbyData.isAnsweringPrompts()) {
                    lobbyData.setEndTime(lobbyData.getEndTime() - 1);

                    // Send initial prompt data
                    if(lobbyData.getEndTime() <= 0 || lobbyData.hasAllPlayersAnswered()) {
                        socketService.sendMessageCollection("hiQlashAnswersDone", clients, Map.of("gameId", entry.getKey()));
                        lobbyData.setAnsweringPrompts(false);

                        String prompt = lobbyData.getUsedPrompts().get(0);
                        lobbyData.getUsedPrompts().remove(0);
                        List<String> players = lobbyData.getPlayersHavingPrompt(prompt);
                        List<String> promptAnswers = new ArrayList<String>(Arrays.asList(lobbyData.getAnswer(players.get(0), prompt), lobbyData.getAnswer(players.get(1), prompt)));

                        // Set initial state
                        lobbyData.setCurrentAnswers(promptAnswers);
                        lobbyData.setCurrentPlayers(players);
                        lobbyData.setCurrentPrompt(prompt);

                        socketService.sendMessageCollection("hiQlashPromptUpdate", clients, Map.of("prompt", prompt, "players", players, "promptAnswers", promptAnswers));
                    }
                }
                // Else, for n == number of players, display prompt => display answers => allow voting for endVoteTime milliseconds
                else {
                    lobbyData.setEndVoteTime(lobbyData.getEndVoteTime() - 1);

                    // If everyone voted, then show results for n seconds before updating
                    if(lobbyData.everyoneHasVoted() && lobbyData.getEndVoteTime() > 0) {
                        lobbyData.setEndVoteTime(0);
                    }

                    if(lobbyData.getEndVoteTime() <= -23) { // Update this value if less or more of viewing time
                        lobbyData.setCurrRound(lobbyData.getCurrRound() + 1);

                        if(lobbyData.getCurrRound() <= lobbyData.getNumRounds()) {

                            // Reset timer and votes and count scores
                            lobbyData.setEndVoteTime(lobbyData.getEndVoteTimeConst());
                            lobbyData.calculateScores();
                            lobbyData.resetVotes();

                            // New prompt data
                            String prompt = lobbyData.getUsedPrompts().get(0);
                            lobbyData.getUsedPrompts().remove(0);
                            List<String> players = lobbyData.getPlayersHavingPrompt(prompt);
                            List<String> promptAnswers = new ArrayList<String>(Arrays.asList(lobbyData.getAnswer(players.get(0), prompt), lobbyData.getAnswer(players.get(1), prompt)));

                            // Set new state
                            lobbyData.setCurrentAnswers(promptAnswers);
                            lobbyData.setCurrentPlayers(players);
                            lobbyData.setCurrentPrompt(prompt);

                            socketService.sendMessageCollection("hiQlashPromptUpdate", clients, Map.of("prompt", prompt, "players", players, "promptAnswers", promptAnswers));
                        }
                        else {
                            // Calculate last scores and send for end viewing
                            if(lobbyData.getCurrRound() == lobbyData.getNumRounds() + 1) {
                                lobbyData.calculateScores();
                                lobbyData.resetVotes();
                            }

                            List<HiQlashData.PlayerScores> playerScores = lobbyData.getPlayerScores();
                            playerScores.sort(Comparator.comparingInt(HiQlashData.PlayerScores::getVotes).reversed());
                            int highestScore = playerScores.get(0).getVotes();
                            socketService.sendMessageCollection("hiQlashEnd", clients, Map.of("playerScores", playerScores, "highestScore", highestScore));
                        }
                    }
                }
                socketService.sendMessageCollection("timeUpdateEvent", clients, Map.of("endTime", lobbyData.getEndTime(), "endVoteTime", lobbyData.getEndVoteTime()));
            }

            // Check if lobby has timeouted
            if(lobby.getTimeout() <= 0){
                lobbyService.removeLobby(entry.getKey());
                lobbyService.removeLobbyData(entry.getKey());
            }


        }
    }

    private DataListener<SocketRecieve> handleVotingDone() {
        return (senderClient, data, ackSender) -> {
            String gameId = data.getGameId();
            LobbyData ld = lobbyService.getLobbyData(gameId);

            if(ld != null) {
                SpyQData lobbyData = (SpyQData)lobbyService.getLobbyData(gameId);
                List<SpyQData.VotingObject> votingData = lobbyData.getVotingObjectList();
                boolean foundSpy = lobbyData.foundSpy;
                String spyName = lobbyData.getSpyName();

                Collection<SocketIOClient> clients = server.getRoomOperations(gameId).getClients();
                socketService.sendMessageCollection("votingDone", clients, Map.of("gameId", gameId, "votingData", votingData, "foundSpy", foundSpy, "spyName", spyName));
            }
        };
    }

    private DataListener<SocketRecieve> handleHiQlashAnswersDone() {
        return (senderClient, data, ackSender) -> {
            String gameId = data.getGameId();
            LobbyData id = lobbyService.getLobbyData(gameId);

            if(id != null) {
                HiQlashData lobbyData = (HiQlashData) lobbyService.getLobbyData(gameId);
                List<SpyQData.VotingObject> votingData = lobbyData.getVotingObjectList();

                Collection<SocketIOClient> clients = server.getRoomOperations(gameId).getClients();
                socketService.sendMessageCollection("hiQlashAnswersDone", clients, Map.of("gameId", gameId));
            }
        };
    }

    private DataListener<SocketRecieve> handleHiQlashVotingDone() {
        return (senderClient, data, ackSender) -> {
            String gameId = data.getGameId();
            LobbyData ld = lobbyService.getLobbyData(gameId);

            if(ld != null) {
                HiQlashData lobbyData = (HiQlashData) lobbyService.getLobbyData(gameId);
                List<SpyQData.VotingObject> votingData = lobbyData.getVotingObjectList();
                List<String> votedForOne = lobbyData.getVotedForOne();
                List<String> votedForTwo = lobbyData.getVotedForTwo();

                Collection<SocketIOClient> clients = server.getRoomOperations(gameId).getClients();
                socketService.sendMessageCollection("hiQlashVotingDone", clients, Map.of("gameId", gameId, "votingData", votingData, "votedForOne", votedForOne, "votedForTwo", votedForTwo));
            }
        };
    }

    private DataListener<SocketRecieve> handleStartGame() {
        return (senderClient, data, ackSender) -> {
            String gameId = data.getGameId();
            String username = data.getUsername();

            GameLobby lobby = lobbyService.getGameLobby(gameId);
            String gameChoice = lobby.getGameChoice();

            if(gameChoice.equals("SpyQ")) {
                SpyQData spyQData =(SpyQData) lobbyService.getLobbyData(gameId);
                String country      = spyQData.getCountry();
                long endTime        = spyQData.getEndTime();
                long endVoteTime    = spyQData.getEndVoteTime();

                // Dont send country to spy
                String spy = spyQData.getSpyName();
                SpyQData.StartGameMessage gameDataSpy = new SpyQData.StartGameMessage(username, gameId, endTime, endVoteTime, "");
                lobbyService.socketToUser.get(spy).getClient().sendEvent("hostStarted", Map.of("username", username, "gameChoice", gameChoice,"gameData", gameDataSpy));
                SpyQData.StartGameMessage gameData = new SpyQData.StartGameMessage(username, gameId, endTime, endVoteTime, country);
                socketService.sendMessage(gameId, "hostStarted", lobbyService.socketToUser.get(spy).getClient(), Map.of("username", username, "gameChoice", gameChoice, "gameData", gameData));
            }
            else if(gameChoice.equals("HiQlash")) {
                HiQlashData hiQlashData =(HiQlashData) lobbyService.getLobbyData(gameId);

                long endTime = hiQlashData.getEndTime();
                long endVoteTime = hiQlashData.getEndVoteTime();

                // Send prompts to each user
                for(HiQlashData.PlayerPrompts p: hiQlashData.getPromptsForPlayers()) {
                    HiQlashData.StartGameMessage gameData = new HiQlashData.StartGameMessage(username, gameId, endTime, endVoteTime, p.getPrompts());
                    lobbyService.socketToUser.get(p.getPlayer()).getClient().sendEvent("hostStarted", Map.of("username", username, "gameChoice", gameChoice, "gameData", gameData));
                }
            }
        };
    }

    private DataListener<SocketRecieve> handleLeaveGame() {
        return (senderClient, data, ackSender) -> {
            String gameId = data.getGameId();
            String username = data.getUsername();

            // Socket to user
            lobbyService.socketToUser.remove(username);
            //senderClient.leaveRoom(gameId);

            // Getting the list of players from the socketService
            List<Player> players = lobbyService.activeLobbies.get(gameId).getPlayers();

            socketService.sendMessage(gameId, "playerLeft", senderClient, new SocketSend(username, players, gameId, username));
        };
    }

    private DataListener<SocketRecieve> handleNewRound() {
        return (senderClient, data, ackSender) -> {
            String gameId = data.getGameId();
            String username = data.getUsername();

            List<Player> players = lobbyService.activeLobbies.get(gameId).getPlayers();

            System.out.println("User " + username + " started a new round of game " + gameId + " and the players are " + players);

            socketService.sendMessage(gameId, "newRound", senderClient, new SocketSend(username, players, gameId, username));
        };
    }

    private DataListener<SocketRecieve> handleCloseLobby() {
        return (senderClient, data, ackSender) -> {
            String gameId = data.getGameId();
            String username = data.getUsername();

            // Socket to user
            lobbyService.socketToUser.remove(username);

            // Just because a Socket message requires players :(
            List<Player> players = new ArrayList<>();

            socketService.sendMessage(gameId, "lobbyClosed", senderClient, new SocketSend(username, players, gameId, username));
            socketService.closeLobby(gameId, senderClient);
        };
    }

    private DataListener<SocketRecieve> handleHostGame() {
        return (senderClient, data, ackSender) -> {
            log.info(data.toString());

            String gameId = data.getGameId();
            String username = data.getUsername();

            // Joining the socket room
            senderClient.joinRoom(gameId);

            // Socket to user
            lobbyService.socketToUser.put(username, new SocketGameId(senderClient, gameId));
        };
    }

    private DataListener<SocketRecieve> handleJoinGame() {
        return (senderClient, data, ackSender) -> {
            // Extracting data from SocketSend
            String gameId = data.getGameId();
            String username = data.getUsername();
            String imageUrl = data.getImageUrl();

            // Joining the socket room
            senderClient.joinRoom(gameId);

            // Adding user to the socketToUser map
            senderClient.joinRoom(gameId);

            // Socket to user
            lobbyService.socketToUser.put(username, new SocketGameId(senderClient, gameId));

            // Getting the list of players from the socketService
            List<Player> players = lobbyService.activeLobbies.get(gameId).getPlayers();

            // Emitting 'playerJoined' event to all clients in the room
            socketService.sendMessage(gameId, "playerJoined", senderClient, new SocketSend(username, players, gameId, username));

            // Logging the event
            System.out.println("User " + username + " joined game " + gameId +
                    " and image url " + imageUrl + " and the players are " + players);
        };
    }

    private ConnectListener onConnected() {
        return (client) -> {
            log.info("Socket ID[{}]  Connected to socket", client.getSessionId().toString());
            //client.getNamespace().getBroadcastOperations().sendEvent("myEvent", "reconnectMessage");
        };
    }


    private DisconnectListener onDisconnected() {
        return client -> {
            log.info("Client[{}] - Disconnected from socket", client.getSessionId().toString());
        };

    }

}