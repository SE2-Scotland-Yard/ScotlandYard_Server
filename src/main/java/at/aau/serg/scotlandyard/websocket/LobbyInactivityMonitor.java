package at.aau.serg.scotlandyard.websocket;

import at.aau.serg.scotlandyard.gamelogic.Lobby;
import at.aau.serg.scotlandyard.gamelogic.LobbyManager;
import at.aau.serg.scotlandyard.dto.LobbyMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component
public class LobbyInactivityMonitor {

    private final LobbyManager lobbyManager;
    private final SimpMessagingTemplate messaging;

    private static final Logger logger = LoggerFactory.getLogger(LobbyInactivityMonitor.class);

    public LobbyInactivityMonitor(LobbyManager lobbyManager, SimpMessagingTemplate messaging) {
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
                if (isBot(player)) {
                    continue; // Bots NICHT entfernen
                }
                long lastActive = lobby.getLastActivity(player);
                if (now - lastActive > timeoutMillis) {
                    toKick.add(player);
                }
            }

            for (String player : toKick) {
                lobby.removePlayer(player);
                logger.info("Inaktiver Spieler entfernt: {}", player);
            }

            // Update an Clients
            messaging.convertAndSend("/topic/lobby/" + gameId, LobbyMapper.toLobbyState(lobby));

            // Lösche die Lobby, wenn leer oder nur Bots
            boolean allBots = lobby.getPlayers().stream().allMatch(this::isBot);
            if (lobby.getPlayers().isEmpty() || allBots) {
                lobbiesToRemove.add(gameId);
                logger.info("Lobby '{}' gelöscht, weil leer oder nur Bots vorhanden.", gameId);
            }
        }


        // Nach dem Durchlauf entfernen
        for (String gameId : lobbiesToRemove) {
            lobbyManager.removeLobby(gameId);
            logger.info("Leere Lobby gelöscht: {}", gameId);
        }
    }

    private boolean isBot(String playerName) {
        return playerName != null && playerName.startsWith("[BOT]");
    }

}