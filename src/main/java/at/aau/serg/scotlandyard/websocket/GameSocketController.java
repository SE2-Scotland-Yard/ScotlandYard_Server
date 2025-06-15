package at.aau.serg.scotlandyard.websocket;



import at.aau.serg.scotlandyard.gamelogic.GameManager;
import at.aau.serg.scotlandyard.gamelogic.GameState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.Map;

@Controller
public class GameSocketController {

    private final GameManager gameManager;
    private final SimpMessagingTemplate messaging;
    private static final Logger logger = LoggerFactory.getLogger(GameSocketController.class);
    private static final String SAFE_LOG_LITERAL = "[\n\r\t]";

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
            String safePlayerId = playerId.replaceAll(SAFE_LOG_LITERAL, "_");
            logger.info("→ [GAME]]Ping erhalten im Spiel von {}", safePlayerId);
        }
    }

    @MessageMapping("/game/leave")
    public void handleGameLeave(Map<String, String> payload) {
        String gameId = payload.get("gameId");
        String playerId = payload.get("playerId");

        String safeGameId = gameId != null ? gameId.replaceAll(SAFE_LOG_LITERAL, "_") : "null";
        String safePlayerId = playerId != null ? playerId.replaceAll(SAFE_LOG_LITERAL, "_") : "null";

        logger.info("Game leave received: {} {}",safeGameId,safePlayerId);

        GameState game = gameManager.getGame(gameId);
        if (game != null) {
            game.replaceWithBot(playerId);
            logger.info("Spieler ersetzt durch Bot: {}",safePlayerId);
        }
    }









}