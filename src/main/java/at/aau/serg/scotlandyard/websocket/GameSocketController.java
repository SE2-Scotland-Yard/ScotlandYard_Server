package at.aau.serg.scotlandyard.websocket;



import at.aau.serg.scotlandyard.gamelogic.GameManager;
import at.aau.serg.scotlandyard.gamelogic.GameState;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.Map;

@Controller
public class GameSocketController {

    private final GameManager gameManager;
    private final SimpMessagingTemplate messaging;

    public GameSocketController(GameManager gameManager, SimpMessagingTemplate messaging) {
        this.gameManager = gameManager;
        this.messaging = messaging;
    }

    @MessageMapping("/game/ping")
    public void handleGamePing(Map<String, String> payload) {
        String gameId = payload.get("gameId");
        String playerId = payload.get("playerId");

        GameState game = gameManager.getGame(gameId);
        if (game != null) {
            game.updateLastActivity(playerId);
            System.out.println("→ [GAME]]Ping erhalten im Spiel von " + playerId);
        }
    }

    @MessageMapping("/game/leave")
    public void handleGameLeave(Map<String, String> payload) {
        String gameId = payload.get("gameId");
        String playerId = payload.get("playerId");

        System.out.println("Game leave received: " + gameId + ", " + playerId);

        GameState game = gameManager.getGame(gameId);
        if (game != null) {
            game.replaceWithBot(playerId);
            System.out.println("Spieler ersetzt durch Bot: " + playerId);
        }
    }









}