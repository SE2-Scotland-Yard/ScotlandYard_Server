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


}