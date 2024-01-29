package com.example.springboot.socketIO;

import com.corundumstudio.socketio.SocketIOClient;
import com.example.springboot.models.StartGameMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service
@Slf4j
public class SocketService {

    public void sendMessage(String room, String eventName, SocketIOClient senderClient, SocketModule.SocketSend message) {
        for (SocketIOClient client : senderClient.getNamespace().getRoomOperations(room).getClients()) {
            if (!client.getSessionId().equals(senderClient.getSessionId())) {
                client.sendEvent(eventName, message);
            }
        }
    }

    public void sendMessage(String room, String eventName, SocketIOClient senderClient, StartGameMessage message) {
        for (SocketIOClient client : senderClient.getNamespace().getRoomOperations(room).getClients()) {
                client.sendEvent(eventName, message);
        }
    }

    public void sendMessage(String room, String eventName,  SocketIOClient senderClient, Map<String, Object> message) {
        for (SocketIOClient client : senderClient.getNamespace().getRoomOperations(room).getClients()) {
            if (!client.getSessionId().equals(senderClient.getSessionId())) {
                client.sendEvent(eventName, message);
            }
        }
    }

    public void sendMessageCollection(String eventName, Collection<SocketIOClient> clients, Map<String, Object> message) {
        for (SocketIOClient client : clients) {
                client.sendEvent(eventName, message);
        }
    }

    public void closeLobby(String room, SocketIOClient senderClient) {
        for (SocketIOClient client : senderClient.getNamespace().getRoomOperations(room).getClients()) {
                client.leaveRoom(room);
        }
    }
}
