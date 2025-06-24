package at.aau.serg.scotlandyard.controller;

import at.aau.serg.scotlandyard.dto.JoinResponse;
import at.aau.serg.scotlandyard.gamelogic.Lobby;
import at.aau.serg.scotlandyard.gamelogic.LobbyManager;
import at.aau.serg.scotlandyard.dto.LobbyState;
import at.aau.serg.scotlandyard.dto.LobbyMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;

@RestController
@RequestMapping("/api/lobby")
public class LobbyController {

    private final LobbyManager lobbyManager;
    private final SimpMessagingTemplate messaging;
    private static final Logger logger = LoggerFactory.getLogger(LobbyController.class);
    private static final String LOBBY_TOPIC_PREFIX = "/topic/lobby/";


    public LobbyController(LobbyManager lobbyManager, SimpMessagingTemplate messaging) {
        this.lobbyManager = lobbyManager;
        this.messaging = messaging;
    }

    @PostMapping("/create")
    public LobbyState createLobby(
            @RequestParam boolean isPublic,
            @RequestParam String name
    ) {
        // 1) Lobby anlegen
        Lobby lobby = lobbyManager.createLobby(isPublic);
        // 2) Ersteller direkt als Spieler hinzufügen
        lobby.addPlayer(name);
        //2.1) aktivität setzen
        lobby.updateLastActivity(name);

        // 3)  Zustand broadcasten
        LobbyState state = LobbyMapper.toLobbyState(lobby);
        messaging.convertAndSend(LOBBY_TOPIC_PREFIX + lobby.getGameId(), state);

        // 4) State zurück an den Aufrufer
        return state;
    }


    @PostMapping("/{gameId}/join")
    public ResponseEntity<JoinResponse> joinLobby(@PathVariable String gameId,
                                                  @RequestParam String name) {
        Lobby lobby = lobbyManager.getLobby(gameId);
        if (lobby == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JoinResponse("Lobby mit der ID " + gameId + " wurde nicht gefunden."));
        }

        boolean success = lobby.addPlayer(name);
        if (!success) {
            return ResponseEntity.badRequest()
                    .body(new JoinResponse("Lobby ist voll oder bereits gestartet."));
        }
        // aktivität setzen
        lobby.updateLastActivity(name);

        String sanitizedGameId = gameId.replaceAll("[\\n\\r\\t]", "_");
        logger.info("WS-Broadcast: /topic/lobby/{}" , sanitizedGameId);
        logger.info("Spieler in der Lobby: {}" , lobby.getPlayers());

        LobbyState state = LobbyMapper.toLobbyState(lobby);
        messaging.convertAndSend(LOBBY_TOPIC_PREFIX + gameId, state);

        return ResponseEntity.ok(new JoinResponse(name + " ist der Lobby " + gameId + " beigetreten."));
    }


    @PostMapping("/{gameId}/leave")
    public ResponseEntity<String> leaveLobby(
            @PathVariable String gameId,
            @RequestParam String name
    ) {
        Lobby lobby = lobbyManager.getLobby(gameId);
        if (lobby == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lobby nicht gefunden");
        }
        lobby.removePlayer(name);
        // Wenn keine Spieler mehr drin sind, löschen
        if (lobby.getPlayers().isEmpty()) {
            lobbyManager.removeLobby(gameId);
        } else {
            // sonst Broadcast des neuen Zustands
            LobbyState state = LobbyMapper.toLobbyState(lobby);
            messaging.convertAndSend(LOBBY_TOPIC_PREFIX + gameId, state);
        }
        return ResponseEntity.ok(name + " hat die Lobby verlassen");
    }



    @GetMapping("/public")
    public List<LobbyState> getPublicLobbies() {
        return lobbyManager.getPublicLobbies().stream()
                .map(LobbyMapper::toLobbyState)
                .toList();
    }

    @GetMapping("/{gameId}/status")
    public ResponseEntity<LobbyState> getLobbyStatus(@PathVariable String gameId) {
        Lobby lobby = lobbyManager.getLobby(gameId);
        if (lobby == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(LobbyMapper.toLobbyState(lobby));
    }
}
