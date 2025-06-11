package at.aau.serg.scotlandyard.websocket;

import at.aau.serg.scotlandyard.gamelogic.GameManager;
import at.aau.serg.scotlandyard.gamelogic.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.mockito.Mockito.*;

class GameSocketControllerTest {

    private GameManager gameManager;
    private SimpMessagingTemplate messaging;
    private GameSocketController controller;

    @BeforeEach
    void setUp() {
        gameManager = mock(GameManager.class);
        messaging = mock(SimpMessagingTemplate.class);
        controller = new GameSocketController(gameManager, messaging);
    }

    @Test
    void testHandleGamePing_updatesActivity() {
        String gameId = "game1";
        String playerId = "Alice";
        Map<String, String> payload = Map.of(
                "gameId", gameId,
                "playerId", playerId
        );

        GameState game = mock(GameState.class);
        when(gameManager.getGame(gameId)).thenReturn(game);

        controller.handleGamePing(payload);

        verify(game).updateLastActivity(playerId);
    }


    @Test
    void testHandleGameLeave_replacesWithBot() {
        String gameId = "game2";
        String playerId = "Charlie";
        Map<String, String> payload = Map.of(
                "gameId", gameId,
                "playerId", playerId
        );

        GameState game = mock(GameState.class);
        when(gameManager.getGame(gameId)).thenReturn(game);

        controller.handleGameLeave(payload);

        verify(game).replaceWithBot(playerId);
    }

}
