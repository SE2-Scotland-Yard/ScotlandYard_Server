package at.aau.serg.scotlandyard.gamelogic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class GameManager {

    private final Map<String, GameState> games = new HashMap<>();
    private final SimpMessagingTemplate messaging;

    @Autowired
    public GameManager(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    public GameState getOrCreateGame(String gameId) {
        return games.computeIfAbsent(gameId, id -> new GameState(id, messaging));
    }

    public GameState getGame(String gameId) {
        return games.get(gameId);
    }

    public void removeGame(String gameId) {
        games.remove(gameId);
    }

    public Set<String> getAllGameIds() {
        return games.keySet();
    }
}
