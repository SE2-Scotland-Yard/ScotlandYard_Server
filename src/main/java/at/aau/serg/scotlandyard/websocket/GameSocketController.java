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
            logger.info("→ [GAME]]Ping erhalten im Spiel von {}",playerId);
        }
    }

    @MessageMapping("/game/leave")
    public void handleGameLeave(Map<String, String> payload) {
        String gameId = payload.get("gameId");
        String playerId = payload.get("playerId");

        logger.info("Game leave received: {} {}",gameId,playerId);

        GameState game = gameManager.getGame(gameId);
        if (game != null) {
            game.replaceWithBot(playerId);
            logger.info("Spieler ersetzt durch Bot: {}",playerId);
        }
    }









}