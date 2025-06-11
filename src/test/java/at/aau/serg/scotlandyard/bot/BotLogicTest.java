package at.aau.serg.scotlandyard.bot;


import at.aau.serg.scotlandyard.gamelogic.GameState;
import at.aau.serg.scotlandyard.gamelogic.player.Player;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import static org.mockito.Mockito.*;

class BotLogicTest {

    @Test
    void decideMove_shouldReturnShortestMove() {
        String playerName = "Detective1";
        GameState gameState = mock(GameState.class);

        Map.Entry<Integer, Ticket> expectedMove = Map.entry(2, Ticket.TAXI);
        when(gameState.getShortestMoveTo(playerName)).thenReturn(Collections.singletonList(expectedMove));

        Map.Entry<Integer, Ticket> result = BotLogic.decideMove(playerName, gameState);

        assertEquals(expectedMove, result);
    }

    @Test
    void decideMove_shouldReturnNullWhenNoMovesAvailable() {
        String playerName = "Detective1";
        GameState gameState = mock(GameState.class);
        when(gameState.getShortestMoveTo(playerName)).thenReturn(Collections.emptyList());

        Map.Entry<Integer, Ticket> result = BotLogic.decideMove(playerName, gameState);

        assertNull(result);
    }

    @Test
    void executeBotMove_shouldMovePlayerWhenValidMoveExists() {
        Player bot = mock(Player.class);
        when(bot.getName()).thenReturn("Detective1");

        GameState gameState = mock(GameState.class);
        Map.Entry<Integer, Ticket> move = Map.entry(5, Ticket.BUS);
        when(gameState.getShortestMoveTo("Detective1")).thenReturn(Collections.singletonList(move));

        BotLogic.executeBotMove(bot, gameState);

        verify(gameState).movePlayer("Detective1", 5, Ticket.BUS);
    }

    @Test
    void executeBotMove_shouldCallCantMoveWhenNoValidMoves() {
        Player bot = mock(Player.class);
        when(bot.getName()).thenReturn("Detective1");

        GameState gameState = mock(GameState.class);
        when(gameState.getShortestMoveTo("Detective1")).thenReturn(Collections.emptyList());
        when(gameState.getGameId()).thenReturn("123");

        BotLogic.executeBotMove(bot, gameState);

        verify(gameState).cantMove("123");
        verify(gameState, never()).movePlayer(any(), anyInt(), any());
    }
}