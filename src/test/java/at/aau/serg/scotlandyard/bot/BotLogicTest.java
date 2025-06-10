package at.aau.serg.scotlandyard.bot;

import at.aau.serg.scotlandyard.gamelogic.GameState;
import at.aau.serg.scotlandyard.gamelogic.player.Player;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotLogicTest {

    private GameState gameState;
    private Player bot;

    @BeforeEach
    void setUp() {
        gameState = mock(GameState.class);
        bot = mock(Player.class);

        when(bot.getName()).thenReturn("Bot1");
        when(gameState.getGameId()).thenReturn("game-123");
    }

    @Test
    void testDecideMoveReturnsFirstAllowedMove() {
        List<Map.Entry<Integer, Ticket>> moves = List.of(
                Map.entry(42, Ticket.TAXI),
                Map.entry(43, Ticket.BUS)
        );
        when(gameState.getAllowedMoves("Bot1")).thenReturn(moves);

        var move = BotLogic.decideMove("Bot1", gameState);

        assertNotNull(move);
        assertEquals(42, move.getKey());
        assertEquals(Ticket.TAXI, move.getValue());
    }

    @Test
    void testDecideMoveReturnsNullIfNoMoves() {
        when(gameState.getAllowedMoves("Bot1")).thenReturn(List.of());

        var move = BotLogic.decideMove("Bot1", gameState);

        assertNull(move);
    }

    @Test
    void testExecuteBotMoveCallsMovePlayer() {
        when(gameState.getAllowedMoves("Bot1")).thenReturn(
                List.of(Map.entry(50, Ticket.BUS))
        );

        BotLogic.executeBotMove(bot, gameState);

        verify(gameState).movePlayer("Bot1", 50, Ticket.BUS);
    }

    @Test
    void testExecuteBotMoveCallsCantMoveIfNoMoves() {
        when(gameState.getAllowedMoves("Bot1")).thenReturn(List.of());

        BotLogic.executeBotMove(bot, gameState);

        verify(gameState).cantMove("game-123");
    }
}
