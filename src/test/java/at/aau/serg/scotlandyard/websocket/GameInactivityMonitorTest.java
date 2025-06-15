package at.aau.serg.scotlandyard.websocket;

import at.aau.serg.scotlandyard.bot.BotLogic;
import at.aau.serg.scotlandyard.gamelogic.GameManager;
import at.aau.serg.scotlandyard.gamelogic.GameState;
import at.aau.serg.scotlandyard.gamelogic.player.Player;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

import static org.mockito.Mockito.*;

class GameInactivityMonitorTest {

    private GameManager gameManager;
    private SimpMessagingTemplate messaging;
    private GameInactivityMonitor monitor;

    @BeforeEach
    void setUp() {
        gameManager = mock(GameManager.class);
        messaging = mock(SimpMessagingTemplate.class);
        monitor = new GameInactivityMonitor(gameManager, messaging);
    }

    @Test
    void testGameIsNull() {
        when(gameManager.getAllGameIds()).thenReturn(Set.of("game1"));
        when(gameManager.getGame("game1")).thenReturn(null);

        monitor.checkInactivePlayersInGame();

        // nothing should happen
        verify(gameManager).getGame("game1");
    }

    @Test
    void testPlayerIsActive() {
        GameState game = mock(GameState.class);
        when(gameManager.getAllGameIds()).thenReturn(Set.of("game1"));
        when(gameManager.getGame("game1")).thenReturn(game);

        String player = "Alice";
        Map<String, Long> activity = Map.of(player, System.currentTimeMillis());
        when(game.getLastActivityMap()).thenReturn(activity);

        monitor.checkInactivePlayersInGame();

        verify(game, never()).replaceWithBot(any());
    }

    @Test
    void testBotReplacementReturnsNull() {
        GameState game = mock(GameState.class);
        when(gameManager.getAllGameIds()).thenReturn(Set.of("game1"));
        when(gameManager.getGame("game1")).thenReturn(game);

        String player = "Bob";
        Map<String, Long> activity = Map.of(player, System.currentTimeMillis() - (4 * 60 * 1000));
        when(game.getLastActivityMap()).thenReturn(activity);
        when(game.replaceWithBot(player)).thenReturn(null);

        monitor.checkInactivePlayersInGame();

        verify(game).replaceWithBot(player);
    }

    @Test
    void testBotIsNotCurrentPlayer() {
        GameState game = mock(GameState.class);
        Player bot = mock(Player.class);

        when(bot.getName()).thenReturn("BotCharlie");

        when(gameManager.getAllGameIds()).thenReturn(Set.of("game1"));
        when(gameManager.getGame("game1")).thenReturn(game);
        when(game.getLastActivityMap()).thenReturn(
                Map.of("Charlie", System.currentTimeMillis() - (4 * 60 * 1000))
        );
        when(game.replaceWithBot("Charlie")).thenReturn(bot);
        when(game.getCurrentPlayerName()).thenReturn("Alice");

        monitor.checkInactivePlayersInGame();

        verify(game, never()).movePlayer(any(), anyInt(), any());
    }


    @Test
    void testBotCanMove() {
        GameState game = mock(GameState.class);
        Player bot = mock(Player.class);

        when(bot.getName()).thenReturn("BotDave");

        when(gameManager.getAllGameIds()).thenReturn(Set.of("game1"));
        when(gameManager.getGame("game1")).thenReturn(game);
        when(game.getLastActivityMap()).thenReturn(
                Map.of("Dave", System.currentTimeMillis() - (4 * 60 * 1000))
        );
        when(game.replaceWithBot("Dave")).thenReturn(bot);
        when(game.getCurrentPlayerName()).thenReturn("BotDave");

        try (MockedStatic<BotLogic> botLogic = mockStatic(BotLogic.class)) {

            botLogic.when(() -> BotLogic.decideMove("BotDave", game))
                    .thenReturn(Map.entry(42, Ticket.TAXI));


            monitor.checkInactivePlayersInGame();

            verify(game).movePlayer("BotDave", 42, Ticket.TAXI);
        }
    }




    @Test
    void testBotCannotMove() {
        GameState game = mock(GameState.class);
        Player bot = mock(Player.class);

        when(bot.getName()).thenReturn("BotEve");

        when(gameManager.getAllGameIds()).thenReturn(Set.of("game1"));
        when(gameManager.getGame("game1")).thenReturn(game);
        when(game.getLastActivityMap()).thenReturn(Map.of("Eve", System.currentTimeMillis() - (4 * 60 * 1000)));
        when(game.replaceWithBot("Eve")).thenReturn(bot);
        when(game.getCurrentPlayerName()).thenReturn("BotEve");

        try (MockedStatic<BotLogic> botLogic = mockStatic(BotLogic.class)) {
            botLogic.when(() -> BotLogic.decideMove("BotEve", game)).thenReturn(null);

            monitor.checkInactivePlayersInGame();

            verify(game).cantMove("game1");
        }
    }

    @Test
    void testNoInactivePlayers() {
        GameState game = mock(GameState.class);
        when(gameManager.getAllGameIds()).thenReturn(Set.of("game1"));
        when(gameManager.getGame("game1")).thenReturn(game);
        when(game.getLastActivityMap()).thenReturn(Map.of("Alice", System.currentTimeMillis())); // Alle aktiv

        monitor.checkInactivePlayersInGame();

        verify(game, never()).replaceWithBot(any());
    }

    @Test
    void testPlayerJustOverTimeout() {
        GameState game = mock(GameState.class);
        when(gameManager.getAllGameIds()).thenReturn(Set.of("game1"));
        when(gameManager.getGame("game1")).thenReturn(game);

        // 4 Minuten und 1 Millisekunde alt → sicher inaktiv
        long justOverTimeout = System.currentTimeMillis() - (4 * 60 * 1000 + 1);
        when(game.getLastActivityMap()).thenReturn(Map.of("Bob", justOverTimeout));

        monitor.checkInactivePlayersInGame();

        verify(game).replaceWithBot("Bob");
    }


}
