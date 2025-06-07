package at.aau.serg.scotlandyard.websocket;

import at.aau.serg.scotlandyard.dto.*;
import at.aau.serg.scotlandyard.gamelogic.*;
import at.aau.serg.scotlandyard.gamelogic.player.Detective;
import at.aau.serg.scotlandyard.gamelogic.player.MrX;
import at.aau.serg.scotlandyard.gamelogic.player.Player;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Random;

import java.util.*;

@Controller
public class LobbySocketController {

    private final LobbyManager lobbyManager;
    private final GameManager gameManager;
    private final SimpMessagingTemplate messaging;
    private static final Logger logger = LoggerFactory.getLogger(LobbySocketController.class);
    private static final String TOPIC_LOBBY_LITERAL = "/topic/lobby/";

    public LobbySocketController(LobbyManager lobbyManager, GameManager gameManager, SimpMessagingTemplate messaging) {
        this.lobbyManager = lobbyManager;
        this.gameManager = gameManager;
        this.messaging = messaging;
    }

    @MessageMapping("/lobby/ready")
    public void handleReady(ReadyMessage msg) {
        String gameId = msg.getGameId();
        Lobby lobby = lobbyManager.getLobby(gameId);
        if (lobby == null) return;

        //Aktivität updaten
        lobby.updateLastActivity(msg.getPlayerId());

        //Spieler als bereit markieren
        lobby.markReady(msg.getPlayerId());

        //Lobby-Zustand an Clients senden
        messaging.convertAndSend(TOPIC_LOBBY_LITERAL + gameId, LobbyMapper.toLobbyState(lobby));

        //Spielstart prüfen
        if (lobby.allReady() && lobby.hasEnoughPlayers() && !lobby.isStarted()) {
            if (!lobby.hasExactlyOneMrX()) {
                // Broadcast-Fehlermeldung an alle in der Lobby
                messaging.convertAndSend(TOPIC_LOBBY_LITERAL + gameId + "/error",
                        Map.of("error", "Es konnte kein Verbrecher ausgemacht werden"));

                // Alle wieder auf 'nicht bereit' setzen
                for (String player : lobby.getPlayers()) {
                    lobby.markNotReady(player);
                }

                // Aktualisierten Lobby-Zustand erneut senden
                messaging.convertAndSend(TOPIC_LOBBY_LITERAL + gameId, LobbyMapper.toLobbyState(lobby));
                return;
            }


            lobby.markStarted();
            messaging.convertAndSend(TOPIC_LOBBY_LITERAL + gameId, LobbyMapper.toLobbyState(lobby)); // Lobby-Update

            GameState game = initializeGame(gameId, lobby);
            if(game != null){
                logger.info(game.toString());
            }

        }
    }

    private GameState initializeGame(String gameId, Lobby lobby) {
        GameState game = gameManager.getOrCreateGame(gameId);
        List<Detective> detectives = new ArrayList<>();
        MrX mrX = null;

        for (String playerName : lobby.getPlayers()) {
            Role role = lobby.getSelectedRole(playerName);
            Player player = (role == Role.MRX) ? new MrX(playerName) : new Detective(playerName);
            game.addPlayer(playerName, player);

            if (role == Role.MRX) {
                mrX = (MrX) player;
            } else {
                detectives.add((Detective) player);
            }
        }
        for(Detective detective : detectives) {
            if(mrX.getPosition()==detective.getPosition()){
                mrX.setPos(new Random().nextInt(199)+1);
            }
        }

        if (!detectives.isEmpty()) {
            game.initRoundManager(detectives, mrX); // RoundManager korrekt initialisieren

            String sanitizedGameId = gameId.replaceAll("[\\n\\r\\t]", "_");
            logger.info("Sending GameUpdate to /topic/game/{}", sanitizedGameId);
            logger.info("Aktueller Spieler im Mapper: {}", game.getCurrentPlayerName());

            messaging.convertAndSend(
                    "/topic/game/" + gameId,
                    GameMapper.mapToGameUpdate(
                            gameId,
                            game.getRoundManager().getPlayerPositions(),
                            game.getCurrentPlayerName(),
                            "NONE",
                            null,
                            game.getAllPlayers()
                    )
            );
        }


        return game;
    }

    @MessageMapping("/lobby/role")
    public void selectRole(RoleSelectionMessage msg) {
        Lobby lobby = lobbyManager.getLobby(msg.getGameId());
        if (lobby != null) {

            //Aktivität updaten
            lobby.updateLastActivity(msg.getPlayerId());

            String currentRole = String.valueOf(lobby.getSelectedRole(msg.getPlayerId()));

            boolean roleChanged = !msg.getRole().equals(currentRole);
            lobby.selectRole(msg.getPlayerId(), msg.getRole());

            if (roleChanged) {
                lobby.markNotReady(msg.getPlayerId());

                // Avatar löschen, wenn Rolle zu MRX wechselt
                if ("MRX".equals(msg.getRole())) {
                    lobby.getAvatars().remove(msg.getPlayerId());
                }
            }

            messaging.convertAndSend(TOPIC_LOBBY_LITERAL + msg.getGameId(), LobbyMapper.toLobbyState(lobby));
        }
    }



    @MessageMapping("/lobby/avatar")
    public void handleAvatarSelection(AvatarSelectionMessage msg) {
        Lobby lobby = lobbyManager.getLobby(msg.getGameId());

        //Aktivität updaten
        lobby.updateLastActivity(msg.getPlayerId());

        if (Role.DETECTIVE.equals(lobby.getSelectedRole(msg.getPlayerId()))) {
            int desiredAvatar = msg.getAvatarResId();
            String playerId = msg.getPlayerId();

            // Ist der Avatar schon vergeben (außer vom aktuellen Spieler)?
            boolean isTaken = lobby.getAvatars().values().stream()
                    .anyMatch(avatar -> avatar == desiredAvatar &&
                            !playerId.equals(getKeyByValue(lobby.getAvatars(), avatar)));

            if (!isTaken) {
                lobby.selectAvatar(playerId, desiredAvatar);
                messaging.convertAndSend("/topic/lobby/" + msg.getGameId(), LobbyMapper.toLobbyState(lobby));
            }
        }
    }

    // Hilfsmethode zum Finden des Spielers, der den Avatar hat
    private String getKeyByValue(Map<String, Integer> map, int value) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() == value) {
                return entry.getKey();
            }
        }
        return null;
    }

    @MessageMapping("/lobby/leave")
    public void handleLeave(@Payload LeaveRequest request) {
        String gameId = request.getGameId();
        String playerId = request.getPlayerId();

        Lobby lobby = lobbyManager.getLobby(gameId);
        if (lobby != null) {
            lobbyManager.leaveLobby(gameId, playerId);
            messaging.convertAndSend("/topic/lobby/" + gameId, LobbyMapper.toLobbyState(lobby));
        }
    }

    @MessageMapping("/lobby/ping")
    public void handlePing(Map<String, String> payload) {
        String gameId = payload.get("gameId");
        String playerId = payload.get("playerId");

        Lobby lobby = lobbyManager.getLobby(gameId);
        if (lobby != null) {
            lobby.updateLastActivity(playerId);
            logger.info("→ Ping erhalten von {}" , playerId);
        }
    }








    @MessageMapping("/game/requestOwnPosition")
    public void handleOwnPositionRequest(Map<String, String> request) {
        String gameId = request.get("gameId");
        String playerId = request.get("playerId");

        GameState game = gameManager.getGame(gameId);
        if (game == null) return;

        Player player = game.getAllPlayers().get(playerId);
        if (player != null) {
            int position = player.getPosition();

            messaging.convertAndSend("/topic/ownPosition/" + playerId, Map.of("position", position));




            logger.info("→ Eigene Position an MrX gesendet: {}", position);
        }
    }

}
