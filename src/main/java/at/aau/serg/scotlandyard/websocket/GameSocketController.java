package at.aau.serg.scotlandyard.websocket;

import at.aau.serg.scotlandyard.gamelogic.GameManager;
import at.aau.serg.scotlandyard.gamelogic.GameState;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class GameSocketController {

    private final GameManager gameManager;
    private final SimpMessagingTemplate messaging;
    private static final String TOPIC_GAME_LITERAL = "/topic/game/";

    public GameSocketController(GameManager gameManager, SimpMessagingTemplate messaging) {
        this.gameManager = gameManager;
        this.messaging = messaging;
    }


    @MessageMapping("/game/allowedMoves")
    public void handleAllowedMoves(String gameId, String playerName) {
        GameState game = gameManager.getGame(gameId);
        if (game == null) return;

        List<Map.Entry<Integer, Ticket>> allowedMoves = game.getAllowedMoves(playerName);
        messaging.convertAndSend(TOPIC_GAME_LITERAL + gameId + "/allowedMoves/" + playerName, allowedMoves);
    }


    @MessageMapping("/game/move")
    public void handleMove(MoveRequest request) {
        GameState game = gameManager.getGame(request.gameId());
        if (game == null) return;

        boolean success = game.movePlayer(request.playerName(), request.target(), request.ticket());
        if (success) {
            messaging.convertAndSend(TOPIC_GAME_LITERAL + request.gameId() + "/state", game);
        }
    }


    @MessageMapping("/game/moveDouble")
    public void handleDoubleMove(DoubleMoveRequest request) {
        GameState game = gameManager.getGame(request.gameId());
        if (game == null) return;

        boolean success = game.moveMrXDouble(
                request.playerName(),
                request.firstTarget(), request.firstTicket(),
                request.secondTarget(), request.secondTicket()
        );
        if (success) {
            messaging.convertAndSend(TOPIC_GAME_LITERAL + request.gameId() + "/state", game);
        }
    }


    @MessageMapping("/game/mrXPosition")
    public void handleMrXPosition(String gameId) {
        GameState game = gameManager.getGame(gameId);
        if (game == null) return;

        String position = game.getVisibleMrXPosition();
        messaging.convertAndSend(TOPIC_GAME_LITERAL + gameId + "/mrXPosition", position);
    }


    @MessageMapping("/game/winner")
    public void handleWinner(String gameId) {
        GameState game = gameManager.getGame(gameId);
        if (game == null) return;

        String winner = switch (game.getWinner()) {
            case MR_X -> "Mr.X hat gewonnen!";
            case DETECTIVE -> "Detektive haben gewonnen!";
            default -> "Spiel läuft noch.";
        };
        messaging.convertAndSend(TOPIC_GAME_LITERAL + gameId + "/winner", winner);
    }

  
    public record MoveRequest(String gameId, String playerName, int target, Ticket ticket) {}
    public record DoubleMoveRequest(String gameId, String playerName, int firstTarget, Ticket firstTicket, int secondTarget, Ticket secondTicket) {}
}