package at.aau.serg.scotlandyard.gamelogic.player.tickets;

import java.util.HashMap;
import java.util.Map;

public class PlayerTickets {
    private final Map<Ticket, Integer> tickets;

    public PlayerTickets(Map<Ticket, Integer> initialTickets) {
        this.tickets = new HashMap<>(initialTickets);
    }

    public boolean hasTicket(Ticket ticket) {
        return tickets.getOrDefault(ticket, 0) > 0;
    }

    public void useTicket(Ticket ticket) {
        if (!hasTicket(ticket)) {
            throw new IllegalStateException("Spieler hat dieses Ticket nich");
        }
        tickets.put(ticket, tickets.get(ticket) - 1);
    }

    public void addTicket(Ticket ticket) {
        tickets.put(ticket, tickets.getOrDefault(ticket, 0) + 1);
    }

    public int getTicketCount(Ticket ticket) {
        return tickets.getOrDefault(ticket, 0);
    }
}


