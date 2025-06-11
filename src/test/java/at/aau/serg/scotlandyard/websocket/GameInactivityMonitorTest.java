package at.aau.serg.scotlandyard.websocket;

import at.aau.serg.scotlandyard.bot.BotLogic;
import at.aau.serg.scotlandyard.gamelogic.GameManager;
import at.aau.serg.scotlandyard.gamelogic.GameState;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class GameInactivityMonitorTest {
    GameManager gameManager = mock(GameManager.class);
    GameState game = mock(GameState.class);
    GameInactivityMonitor gameInactivityMonitor;

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStream));
        gameInactivityMonitor = new GameInactivityMonitor(gameManager, mock(SimpMessagingTemplate.class));
    }


    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    void testGameInactivityMonitorPlayerInactiveCurrentMove() {
        String gameId = "1";
        String player = "Player";
        long timeOutMillis = 3 * 60 * 1000 + 1L;
        when(gameManager.getAllGameIds()).thenReturn(Set.of(gameId));
        when(gameManager.getGame("1")).thenReturn(game);
        when(game.getLastActivityMap()).thenReturn(Map.of(player, System.currentTimeMillis() - timeOutMillis ));
        when(game.getCurrentPlayerName()).thenReturn(player);

        gameInactivityMonitor.checkInactivePlayersInGame();

        assertEquals("Spieler im Spiel inaktiv: " + player + " in Game " + gameId + "\n", outputStream.toString());
        verify(game).replaceWithBot(player);
    }

    @Test
    void testGameInactivityMonitorPlayerInactiveGameNull() {
        String gameId = "1";
        String player = "Player";
        when(gameManager.getAllGameIds()).thenReturn(Set.of(gameId));
        when(gameManager.getGame("1")).thenReturn(null);
        gameInactivityMonitor.checkInactivePlayersInGame();

        assertEquals("", outputStream.toString());

    }

    @Test
    void testGameInactivityMonitorPlayerActive() {
        String gameId = "1";
        String player = "Player";
        long timeOutMillis = 10L;
        when(gameManager.getAllGameIds()).thenReturn(Set.of(gameId));
        when(gameManager.getGame("1")).thenReturn(game);
        when(game.getLastActivityMap()).thenReturn(Map.of(player, System.currentTimeMillis() - timeOutMillis ));

        gameInactivityMonitor.checkInactivePlayersInGame();

        assertEquals("", outputStream.toString());

    }
}
