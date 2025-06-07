package at.aau.serg.scotlandyard.gamelogic;

import at.aau.serg.scotlandyard.gamelogic.board.Board;
import at.aau.serg.scotlandyard.gamelogic.board.Edge;
import at.aau.serg.scotlandyard.gamelogic.player.Detective;
import at.aau.serg.scotlandyard.gamelogic.player.MrX;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerMovementTest {

    private Board board;
    private SimpMessagingTemplate messagingTemplate;

    @BeforeEach
    void setup() {
        board = new Board();
        messagingTemplate = mock(SimpMessagingTemplate.class);
    }

    @Test
    void testValidMove_ShouldUpdatePositionAndUseTicket() {
        Detective detective = new Detective("Maxmustermann");
        MrX mrX = new MrX("GeheimagentX");

        GameState game = new GameState("1234", null);

        game.addPlayer(detective.getName(), detective);
        game.addPlayer(mrX.getName(), mrX);
        game.initRoundManager(List.of(detective), mrX);

        int from = detective.getPosition();
        List<Edge> connections = board.getConnectionsFrom(from);

        Edge validEdge = connections.stream()
                .filter(e -> detective.getTickets().hasTicket(e.getTicket()))
                .findFirst().orElseThrow();

        detective.move(validEdge.getTo(), validEdge.getTicket(), board);

        assertEquals(validEdge.getTo(), detective.getPosition());
        assertEquals(9, detective.getTickets().getTicketCount(validEdge.getTicket()));
    }

    @Test
    void testInvalidMove_ShouldThrowException() {
        Detective detective = new Detective("Maxmustermann");
        assertThrows(IllegalArgumentException.class, () ->
                detective.move(999, Ticket.TAXI, board));
    }

    @Test
    void testMovePlayer_ValidMove_ShouldReturnTrue() {
        GameState game = new GameState("1234", messagingTemplate);
        Detective det = new Detective("Maxmustermann");
        MrX mrX = new MrX("X");

        game.addPlayer("Alice", det);
        game.addPlayer("X", mrX);
        game.initRoundManager(List.of(det), mrX);

        int from = det.getPosition();
        Edge validEdge = game.getBoard().getConnectionsFrom(from).stream()
                .filter(e -> det.getTickets().hasTicket(e.getTicket()))
                .findFirst()
                .orElseThrow();

        int to = validEdge.getTo();
        Ticket ticket = validEdge.getTicket();

        boolean moved = game.movePlayer("Alice", to, ticket);

        assertTrue(moved);
        assertEquals(to, det.getPosition());
    }

    @Test
    void testMovePlayer_InvalidTarget_ShouldReturnFalse() {
        GameState game = new GameState("1234", messagingTemplate);
        Detective det = new Detective("Maxmustermann");
        MrX mrX = new MrX("X");

        game.addPlayer("Bob", det);
        game.addPlayer("X", mrX);
        game.initRoundManager(List.of(det), mrX);

        boolean moved = game.movePlayer("Bob", 999, Ticket.TAXI);
        assertFalse(moved);
    }


    @Test
    void testMrXDoubleMove_ShouldExecuteTwoMovesAndStoreHistory() {
        GameState game = new GameState("1234", messagingTemplate);
        MrX mrX = new MrX("Maxmustermann");
        Detective det = new Detective("Dummy");

        game.addPlayer("X", mrX);
        game.addPlayer("D", det);
        game.initRoundManager(List.of(det), mrX);

        int from = mrX.getPosition();
        List<Edge> firstEdges = board.getConnectionsFrom(from);
        assertFalse(firstEdges.isEmpty(), "Keine Verbindungen vom Startpunkt.");

        Edge first = firstEdges.get(0);
        List<Edge> secondEdges = board.getConnectionsFrom(first.getTo());
        assertFalse(secondEdges.isEmpty(), "Keine Verbindungen vom Zwischenziel.");

        Edge second = secondEdges.get(0);

        boolean moved = game.moveMrXDouble("X", first.getTo(), second.getTo(), first.getTicket(), second.getTicket());

        assertTrue(moved, "Der Doppelzug sollte erfolgreich sein.");
        assertEquals(second.getTo(), mrX.getPosition(), "MrX sollte am Ziel des zweiten Zugs stehen.");
        assertEquals(2, game.getMrXMoveHistory().size(), "Die MrX-Zughistorie sollte zwei Einträge enthalten.");
    }

    private void moveMrXToRound(GameState game, MrX mrX, int targetRound) {
        Detective dummy = new Detective("D");
        game.addPlayer(mrX.getName(), mrX);
        game.addPlayer(dummy.getName(), dummy);
        game.initRoundManager(List.of(dummy), mrX);

        int currentPos = mrX.getPosition();

        for (int i = 1; i <= targetRound; i++) {
            Edge edge = board.getConnectionsFrom(currentPos).get(0);
            game.movePlayer(mrX.getName(), edge.getTo(), edge.getTicket());
            currentPos = edge.getTo();
        }
    }

    @Test
    void moveBlackTest_ValidMove(){
        MrX mrX = new MrX("MrX");
        GameState game = new GameState("game-id", messagingTemplate);
        Detective det = new Detective("Dummy");

        game.addPlayer("X", mrX);
        game.addPlayer("D", det);
        game.initRoundManager(List.of(det), mrX);

        int from = mrX.getPosition();
        List<Edge> connections = board.getConnectionsFrom(from);

        Edge validEdge = connections.stream()
                .filter(e -> e.getTicket() != Ticket.DOUBLE) // Sicherheitscheck
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Kein gültiger Pfad für MrX gefunden"));

        mrX.getTickets().addTicket(Ticket.BLACK);

        mrX.moveBlack(validEdge.getTo(), validEdge.getTicket(), board);

        assertEquals(validEdge.getTo(), mrX.getPosition());
    }

    @Test
    void testMoveBlack_InvalidMove_ShouldThrowException() {
        MrX mrX = new MrX("MrX");
        mrX.getTickets().addTicket(Ticket.BLACK);

        assertThrows(IllegalArgumentException.class, () ->
                mrX.moveBlack(9999, Ticket.TAXI, board));
    }


}