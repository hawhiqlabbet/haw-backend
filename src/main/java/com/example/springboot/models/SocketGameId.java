package com.example.springboot.models;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.Data;

import java.net.Socket;

@Data
public class SocketGameId {
    private SocketIOClient client;
    private String gameId;

    public SocketGameId(SocketIOClient client, String gameId) {
        this.client = client;
        this.gameId = gameId;
    }
}
