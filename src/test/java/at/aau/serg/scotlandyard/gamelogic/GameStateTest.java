package at.aau.serg.scotlandyard.gamelogic;

import at.aau.serg.scotlandyard.gamelogic.board.Board;
import at.aau.serg.scotlandyard.gamelogic.player.Detective;
import at.aau.serg.scotlandyard.gamelogic.player.MrX;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.PlayerTickets;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameStateTest {
    private GameState gameState;
    private MrX mrX;
    private Detective detective;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @BeforeEach
    void setUp() {
        gameState = new GameState("testGame", messagingTemplate);
        mrX = mock(MrX.class);
        detective = mock(Detective.class);
        gameState.addPlayer("MrX", mrX);
        gameState.addPlayer("Detective", detective);
        gameState.initRoundManager(List.of(detective), mrX);
    }

    @Test
    void testAddPlayer() {
        assertEquals(2, gameState.getAllPlayers().size());
        assertTrue(gameState.getAllPlayers().containsKey("MrX"));
        assertTrue(gameState.getAllPlayers().containsKey("Detective"));
    }

    @Test
    void testGetAllowedMoves() {
        when(mrX.getPosition()).thenReturn(1);
        when(mrX.getTickets()).thenReturn(getDefaultTickets());

        var allowedMoves = gameState.getAllowedMoves("MrX");
        assertFalse(allowedMoves.isEmpty());
    }

    private static PlayerTickets getDefaultTickets() {
        Map<Ticket, Integer> initialTickets = new EnumMap<>(Ticket.class);

        initialTickets.put(Ticket.TAXI, 4);
        initialTickets.put(Ticket.BUS, 3);
        initialTickets.put(Ticket.UNDERGROUND, 3);
        initialTickets.put(Ticket.BLACK, 5);
        initialTickets.put(Ticket.DOUBLE, 2);

        return new PlayerTickets(initialTickets);
    }

    @Test
    void testGetAllowedMovesPlayerIsNull() {
        var allowedMoves = gameState.getAllowedMoves("Nothing");
        assertTrue(allowedMoves.isEmpty());
    }

    @Test
    void testMovePlayerMrX() {
        when(mrX.isValidMove(anyInt(), any(), any())).thenReturn(true);
        boolean successful = gameState.movePlayer("MrX", 1, Ticket.TAXI);

        verify(mrX).move(eq(1), eq(Ticket.TAXI), any());
        assertTrue(successful);
    }

    @Test
    void testMovePlayerMrXInvalid() {
        when(mrX.isValidMove(anyInt(), any(), any())).thenReturn(false);
        boolean successful = gameState.movePlayer("MrX", 1, Ticket.TAXI);

        assertFalse(successful);
        verify(mrX, never()).move(anyInt(), any(), any());
    }

    @Test
    void testMovePlayerDetective() {
        when(detective.isValidMove(anyInt(), any(), any())).thenReturn(true);
        boolean successful = gameState.movePlayer("Detective", 1, Ticket.TAXI);

        verify(detective).move(eq(1), eq(Ticket.TAXI), any());
        assertTrue(successful);
    }

    @Test
    void testMovePlayerDetectivePositionTaken() {
        Detective detective2 = mock(Detective.class);
        gameState.addPlayer("Detective2", detective2);
        when(detective2.isValidMove(anyInt(), any(), any())).thenReturn(false);
        when(detective.getPosition()).thenReturn(1);

        boolean successful = gameState.movePlayer("Detective2", 1, Ticket.TAXI);

        assertFalse(successful);
        verify(detective2, never()).move(anyInt(), any(), any());
    }

    @Test
    void testMovePlayerDetectiveInvalid() {
        when(detective.isValidMove(anyInt(), any(), any())).thenReturn(false);
        boolean successful = gameState.movePlayer("Detective", 1, Ticket.TAXI);

        assertFalse(successful);
        verify(detective, never()).move(anyInt(), any(), any());
    }


    @Test
    void testMoveMrXDoubleInvalid() {
        boolean successful = gameState.moveMrXDouble("Detective", 1, Ticket.TAXI, 1, Ticket.TAXI);
        assertFalse(successful);
        verify(mrX, never()).move(anyInt(), any(), any());
    }

    @Test
    void testGetVisibleMrXPositionRoundNotReveal() {
        String position = gameState.getVisibleMrXPosition();
        assertEquals("?", position);
    }


    @Test
    void testGetVisibleMrXPositionMrXIsNull() {
        GameState gameStateNew = new GameState("newGame", messagingTemplate);
        String position = gameStateNew.getVisibleMrXPosition();
        assertEquals("MrX nicht im Spiel", position);
    }

    @Test
    void testGetMrXMoveHistory() {
        when(mrX.isValidMove(anyInt(), any(Ticket.class), any(Board.class))).thenReturn(true);

        gameState.movePlayer("MrX", 1, Ticket.TAXI);
        gameState.movePlayer("MrX", 2, Ticket.BUS);
        gameState.movePlayer("MrX", 3, Ticket.UNDERGROUND);

        var history = gameState.getMrXMoveHistory();
        assertEquals(3, history.size());
    }

    @Test
    void testGetWinnerNone() throws Exception {
        RoundManager roundManager = mock(RoundManager.class);
        when(roundManager.isGameOver()).thenReturn(false);

        Field nameField = GameState.class.getDeclaredField("roundManager");
        nameField.setAccessible(true);
        nameField.set(gameState, roundManager);

        assertEquals(GameState.Winner.NONE, gameState.getWinner());
    }

    @Test
    void testGetWinnerDetective() throws Exception {
        RoundManager roundManager = mock(RoundManager.class);
        when(roundManager.isGameOver()).thenReturn(true);
        when(roundManager.isMrXCaptured()).thenReturn(true);

        Field nameField = GameState.class.getDeclaredField("roundManager");
        nameField.setAccessible(true);
        nameField.set(gameState, roundManager);

        assertEquals(GameState.Winner.DETECTIVE, gameState.getWinner());
    }

    @Test
    void testGetWinnerMrX() throws Exception {
        RoundManager roundManager = mock(RoundManager.class);
        when(roundManager.isGameOver()).thenReturn(true);
        when(roundManager.isMrXCaptured()).thenReturn(false);

        Field nameField = GameState.class.getDeclaredField("roundManager");
        nameField.setAccessible(true);
        nameField.set(gameState, roundManager);

        assertEquals(GameState.Winner.MR_X, gameState.getWinner());
    }

    @Test
    void testGetRevealRounds() {
        var revealRounds = gameState.getRevealRounds();
        assertEquals(List.of(3, 8, 13, 18, 24), revealRounds);
    }

    @Test
    void testGetBoard() {
        Board board = gameState.getBoard();
        assertNotNull(board);
    }

    @Test
    void testIsPositionOccupied() {
        when(detective.getPosition()).thenReturn(5);
        assertTrue(gameState.isPositionOccupied(5));
        assertFalse(gameState.isPositionOccupied(10));
    }

    @Test
    void testGetMrXPosition() {
        when(mrX.getPosition()).thenReturn(1);
        int pos = gameState.getMrXPosition("MrX");
        assertEquals(1, pos);
    }

    @Test
    void testGetMrXPositionNull() {
        int pos = gameState.getMrXPosition("Nothing");
        assertEquals(0, pos);
    }
}