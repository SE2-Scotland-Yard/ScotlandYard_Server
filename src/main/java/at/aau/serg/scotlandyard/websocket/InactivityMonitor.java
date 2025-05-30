package at.aau.serg.scotlandyard.websocket;

import at.aau.serg.scotlandyard.gamelogic.Lobby;
import at.aau.serg.scotlandyard.gamelogic.LobbyManager;
import at.aau.serg.scotlandyard.dto.LobbyMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InactivityMonitor {

    private final LobbyManager lobbyManager;
    private final SimpMessagingTemplate messaging;

    public InactivityMonitor(LobbyManager lobbyManager, SimpMessagingTemplate messaging) {
        this.lobbyManager = lobbyManager;
        this.messaging = messaging;
    }

    @Scheduled(fixedRate = 60_000) // alle 60 Sekunden
    public void checkInactivePlayers() {
        long now = System.currentTimeMillis();
        long timeoutMillis = 4 * 60 * 1000;


        List<String> lobbiesToRemove = new ArrayList<>();

        for (Lobby lobby : lobbyManager.getAllLobbies()) {
            String gameId = lobby.getGameId();
            List<String> toKick = new ArrayList<>();

            for (String player : new ArrayList<>(lobby.getPlayers())) {
                long lastActive = lobby.getLastActivity(player);
                if (now - lastActive > timeoutMillis) {
                    toKick.add(player);
                }
            }

            for (String player : toKick) {
                lobby.removePlayer(player);
                System.out.println("Inaktiver Spieler entfernt: " + player);
            }

            // Sende Update an alle Clients
            messaging.convertAndSend("/topic/lobby/" + gameId, LobbyMapper.toLobbyState(lobby));

            // Lobby löschen, wenn leer
            if (lobby.getPlayers().isEmpty()) {
                lobbiesToRemove.add(gameId);
            }
        }

        // Nach dem Durchlauf entfernen
        for (String gameId : lobbiesToRemove) {
            lobbyManager.removeLobby(gameId);
            System.out.println("Leere Lobby gelöscht: " + gameId);
        }
    }
}