package at.aau.serg.scotlandyard.dto;

import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameUpdateTest {

    @Test
    void testConstructorAndGetters() {
        Map<String, Integer> positions = new HashMap<>();
        positions.put("Anna", 42);
        positions.put("Bert", 17);

        GameUpdate update = new GameUpdate("game123", positions, "Anna", "NONE", Ticket.TAXI, new HashMap<>());

        assertEquals("game123", update.getGameId());
        assertEquals(2, update.getPlayerPositions().size());
        assertEquals(42, update.getPlayerPositions().get("Anna"));
        assertEquals(17, update.getPlayerPositions().get("Bert"));
        assertEquals("Anna", update.getCurrentPlayer());
    }

    @Test
    void testEmptyPositions() {
        GameUpdate update = new GameUpdate("emptyGame", new HashMap<>(), "Bob", "NONE", Ticket.TAXI, new HashMap<>());
        assertEquals("emptyGame", update.getGameId());
        assertTrue(update.getPlayerPositions().isEmpty());
        assertEquals("Bob", update.getCurrentPlayer());
    }

    @Test
    void testNullPositions() {
        // Teste Verhalten bei null als Map (optional, je nach gewünschtem Verhalten)
        GameUpdate update = new GameUpdate("nullGame", null, null,null, Ticket.TAXI, new HashMap<>());
        assertEquals("nullGame", update.getGameId());
        assertNull(update.getPlayerPositions());
        assertNull(update.getCurrentPlayer());
    }

}
