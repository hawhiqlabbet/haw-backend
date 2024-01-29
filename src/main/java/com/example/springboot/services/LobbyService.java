package com.example.springboot.services;

import com.corundumstudio.socketio.SocketIOClient;
import com.example.springboot.models.GameLobby;
import com.example.springboot.models.SocketGameId;
import com.example.springboot.models.SpyQData;
import org.springframework.stereotype.Service;

import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.example.springboot.models.LobbyData;

@Service
public class LobbyService {
    public ConcurrentHashMap<String, GameLobby> activeLobbies;
    public ConcurrentHashMap<String, LobbyData> lobbyData;
    public ConcurrentHashMap<String, SocketGameId> socketToUser;

    public LobbyService() {
        this.activeLobbies = new ConcurrentHashMap<String, GameLobby>();
        this.lobbyData     = new ConcurrentHashMap<String, LobbyData>();
        this.socketToUser  = new ConcurrentHashMap<String, SocketGameId>();
    }

    // Active Lobbies functions
    public ConcurrentHashMap<String, GameLobby> getActiveLobbies() {
        return activeLobbies;
    }
    public ConcurrentHashMap<String, SocketGameId> getSocketToUsers() { return socketToUser; }

    public GameLobby getGameLobby(String gameId) {
        return activeLobbies.get(gameId);
    }

    public void addLobby(String gameId, GameLobby gameLobby){
        activeLobbies.put(gameId, gameLobby);
    }

    public void removeLobby(String gameId) {
        activeLobbies.remove(gameId);
    }

    public boolean lobbyExists(String gameId) {
        return activeLobbies.containsKey(gameId);
    }


    // Lobby Data functions
    public void addLobbyData(String gameId, LobbyData data){
        lobbyData.put(gameId, data);
    }
    public void removeLobbyData(String gameId) {
        lobbyData.remove(gameId);
    }
    public LobbyData getLobbyData(String gameId) {
        LobbyData ld = lobbyData.get(gameId);
        if(ld instanceof SpyQData)
            return (SpyQData) ld;
        else
            return ld;
    }


    // Lobby Utility
    public static String generateGameId() {
        // Define the characters allowed in the code
        String allowedCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        // Set the length of the code
        int codeLength = 4;

        // Create a StringBuilder to build the code
        StringBuilder codeBuilder = new StringBuilder(codeLength);

        // Use Random to generate random indices from the allowed characters
        Random random = new Random();
        for (int i = 0; i < codeLength; i++) {
            int randomIndex = random.nextInt(allowedCharacters.length());
            char randomChar = allowedCharacters.charAt(randomIndex);
            codeBuilder.append(randomChar);
        }
        return codeBuilder.toString();
    }
}
