package at.aau.serg.scotlandyard.websocket;

import at.aau.serg.scotlandyard.gamelogic.GameManager;
import at.aau.serg.scotlandyard.gamelogic.GameState;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

class GameSocketControllerTest {

    private GameManager gameManager;
    private SimpMessagingTemplate messaging;
    private GameSocketController controller;
    private GameState gameState;

    @BeforeEach
    void setUp() {
        gameManager = mock(GameManager.class);
        messaging = mock(SimpMessagingTemplate.class);
        controller = new GameSocketController(gameManager, messaging);
        gameState = mock(GameState.class);
    }

    @Test
    void testHandleAllowedMoves_GameExists() {
        when(gameManager.getGame("game1")).thenReturn(gameState);
        List<Map.Entry<Integer, Ticket>> moves = List.of(Map.entry(1, Ticket.TAXI));
        when(gameState.getAllowedMoves("Anna")).thenReturn(moves);

        controller.handleAllowedMoves("game1", "Anna");

        verify(messaging).convertAndSend("/topic/game/game1/allowedMoves/Anna", moves);
    }

    @Test
    void testHandleAllowedMoves_GameNotExists() {
        when(gameManager.getGame("game2")).thenReturn(null);
        controller.handleAllowedMoves("game2", "Bob");
        verifyNoInteractions(messaging);
    }

    @Test
    void testHandleMove_Success() {
        GameSocketController.MoveRequest req = new GameSocketController.MoveRequest("g1", "Anna", 42, Ticket.BUS);
        when(gameManager.getGame("g1")).thenReturn(gameState);
        when(gameState.movePlayer("Anna", 42, Ticket.BUS)).thenReturn(true);

        controller.handleMove(req);

        verify(messaging).convertAndSend("/topic/game/g1/state", gameState);
    }

    @Test
    void testHandleMove_GameNotExists() {
        GameSocketController.MoveRequest req = new GameSocketController.MoveRequest("gX", "Anna", 1, Ticket.TAXI);
        when(gameManager.getGame("gX")).thenReturn(null);

        controller.handleMove(req);

        verifyNoInteractions(messaging);
    }

    @Test
    void testHandleMove_NotSuccessful() {
        GameSocketController.MoveRequest req = new GameSocketController.MoveRequest("g1", "Anna", 42, Ticket.BUS);
        when(gameManager.getGame("g1")).thenReturn(gameState);
        when(gameState.movePlayer("Anna", 42, Ticket.BUS)).thenReturn(false);

        controller.handleMove(req);

        verify(messaging, never()).convertAndSend(Optional.of(anyString()), any());
    }

    @Test
    void testHandleDoubleMove_Success() {
        GameSocketController.DoubleMoveRequest req = new GameSocketController.DoubleMoveRequest("g2", "MrX", 1, Ticket.BUS, 2, Ticket.TAXI);
        when(gameManager.getGame("g2")).thenReturn(gameState);
        when(gameState.moveMrXDouble("MrX", 1, Ticket.BUS, 2, Ticket.TAXI)).thenReturn(true);

        controller.handleDoubleMove(req);

        verify(messaging).convertAndSend("/topic/game/g2/state", gameState);
    }

    @Test
    void testHandleDoubleMove_GameNotExists() {
        GameSocketController.DoubleMoveRequest req = new GameSocketController.DoubleMoveRequest("gX", "MrX", 1, Ticket.BUS, 2, Ticket.TAXI);
        when(gameManager.getGame("gX")).thenReturn(null);

        controller.handleDoubleMove(req);

        verifyNoInteractions(messaging);
    }

    @Test
    void testHandleDoubleMove_NotSuccessful() {
        GameSocketController.DoubleMoveRequest req = new GameSocketController.DoubleMoveRequest("g2", "MrX", 1, Ticket.BUS, 2, Ticket.TAXI);
        when(gameManager.getGame("g2")).thenReturn(gameState);
        when(gameState.moveMrXDouble("MrX", 1, Ticket.BUS, 2, Ticket.TAXI)).thenReturn(false);

        controller.handleDoubleMove(req);

        verify(messaging, never()).convertAndSend(Optional.of(anyString()), any());
    }

    @Test
    void testHandleMrXPosition_GameExists() {
        when(gameManager.getGame("g3")).thenReturn(gameState);
        when(gameState.getVisibleMrXPosition()).thenReturn("123");

        controller.handleMrXPosition("g3");

        verify(messaging).convertAndSend("/topic/game/g3/mrXPosition","123");
    }

    @Test
    void testHandleMrXPosition_GameNotExists() {
        when(gameManager.getGame("gX")).thenReturn(null);

        controller.handleMrXPosition("gX");

        verifyNoInteractions(messaging);
    }

    @Test
    void testHandleWinner_MrX() {
        when(gameManager.getGame("g4")).thenReturn(gameState);
        when(gameState.getWinner()).thenReturn(GameState.Winner.MR_X);

        controller.handleWinner("g4");

        verify(messaging).convertAndSend("/topic/game/g4/winner","Mr.X hat gewonnen!");
    }

    @Test
    void testHandleWinner_Detective() {
        when(gameManager.getGame("g5")).thenReturn(gameState);
        when(gameState.getWinner()).thenReturn(GameState.Winner.DETECTIVE);

        controller.handleWinner("g5");

        verify(messaging).convertAndSend("/topic/game/g5/winner","Detektive haben gewonnen!");
    }

    @Test
    void testHandleWinner_None() {
        when(gameManager.getGame("g6")).thenReturn(gameState);
        when(gameState.getWinner()).thenReturn(GameState.Winner.NONE);

        controller.handleWinner("g6");

        verify(messaging).convertAndSend("/topic/game/g6/winner", "Spiel läuft noch.");
    }

    @Test
    void testHandleWinner_GameNotExists() {
        when(gameManager.getGame("gX")).thenReturn(null);

        controller.handleWinner("gX");

        verifyNoInteractions(messaging);
    }

}
