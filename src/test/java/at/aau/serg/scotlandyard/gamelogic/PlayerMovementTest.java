package at.aau.serg.scotlandyard.gamelogic;

import at.aau.serg.scotlandyard.gamelogic.board.Board;
import at.aau.serg.scotlandyard.gamelogic.board.Edge;
import at.aau.serg.scotlandyard.gamelogic.player.Detective;
import at.aau.serg.scotlandyard.gamelogic.player.MrX;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
    void testVisibleMrXPosition_ShouldReturnPositionOnRevealRound() {
        GameState game = new GameState("1234", messagingTemplate);
        MrX mrX = new MrX("Maxmustermann");

        moveMrXToRound(game, mrX, 3);

        String visible = game.getVisibleMrXPosition();
        assertEquals(String.valueOf(mrX.getPosition()), visible);
    }

    @Test
    void testInvisibleMrXPosition_ShouldReturnQuestionMark() {
        GameState game = new GameState("1234", messagingTemplate);
        MrX mrX = new MrX("Maxmustermann");

        moveMrXToRound(game, mrX, 1);

        String visible = game.getVisibleMrXPosition();
        assertEquals("?", visible);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 5, 6, 7})
    void testMrXShouldBeInvisibleInTheseRounds(int roundsToMove) {
        GameState game = new GameState("1234", messagingTemplate);
        MrX mrX = new MrX("Maxmustermann");

        moveMrXToRound(game, mrX, roundsToMove);

        if (!game.getRevealRounds().contains(roundsToMove)) {
            assertEquals("?", game.getVisibleMrXPosition());
        }
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
        Edge first = board.getConnectionsFrom(from).get(0);
        Edge second = board.getConnectionsFrom(first.getTo()).get(0);

        boolean moved = game.moveMrXDouble("X",
                first.getTo(), first.getTicket(),
                second.getTo(), second.getTicket());

        assertTrue(moved);
        assertEquals(second.getTo(), mrX.getPosition());
        assertEquals(2, game.getMrXMoveHistory().size());
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
}