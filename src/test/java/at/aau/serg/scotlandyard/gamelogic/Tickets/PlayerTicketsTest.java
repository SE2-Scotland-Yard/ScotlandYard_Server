package at.aau.serg.scotlandyard.gamelogic.Tickets;

import at.aau.serg.scotlandyard.gamelogic.player.tickets.PlayerTickets;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTicketsTest {
    private PlayerTickets playerTickets;
    private final Map<Ticket, Integer> initialTickets = new EnumMap<>(Ticket.class);


    @BeforeEach
    void setUp() {
        initialTickets.put(Ticket.TAXI, 4);
        initialTickets.put(Ticket.BUS, 3);
        initialTickets.put(Ticket.UNDERGROUND, 2);
        initialTickets.put(Ticket.BLACK, 1);
        initialTickets.put(Ticket.DOUBLE,0);
        playerTickets = new PlayerTickets(initialTickets);
    }


    @Test
    void hasTicketTestAvailable(){
        assertTrue(playerTickets.hasTicket(Ticket.TAXI));
        assertTrue(playerTickets.hasTicket(Ticket.BUS));
        assertTrue(playerTickets.hasTicket(Ticket.UNDERGROUND));
        assertTrue(playerTickets.hasTicket(Ticket.BLACK));

    }

    @Test
    void hasTicketTestNotAvailable(){
        assertFalse(playerTickets.hasTicket(Ticket.DOUBLE));
    }

    @Test
    void useTicketTest(){
        playerTickets.useTicket(Ticket.TAXI);
        assertEquals(3, playerTickets.getTicketCount(Ticket.TAXI));
    }

    @Test
    void useTicket_ThrowExceptionWhenNoTicketsLeft() {
        assertThrows(IllegalStateException.class, () -> playerTickets.useTicket(Ticket.DOUBLE));
    }

    @Test
    void addTicket_shouldIncreaseTicketCount() {
        playerTickets.addTicket(Ticket.TAXI);
        assertEquals(5, playerTickets.getTicketCount(Ticket.TAXI));

        playerTickets.addTicket(Ticket.DOUBLE);
        assertEquals(1, playerTickets.getTicketCount(Ticket.DOUBLE));

        playerTickets.addTicket(Ticket.BUS);
        assertEquals(4, playerTickets.getTicketCount(Ticket.BUS));

        playerTickets.addTicket(Ticket.UNDERGROUND);
        assertEquals(3, playerTickets.getTicketCount(Ticket.UNDERGROUND));

    }

    @Test
    void getTicketCount() {
        assertEquals(4, playerTickets.getTicketCount(Ticket.TAXI));
        assertEquals(3, playerTickets.getTicketCount(Ticket.BUS));
        assertEquals(2, playerTickets.getTicketCount(Ticket.UNDERGROUND));
        assertEquals(1, playerTickets.getTicketCount(Ticket.BLACK));
        assertEquals(0, playerTickets.getTicketCount(Ticket.DOUBLE));
    }





}
