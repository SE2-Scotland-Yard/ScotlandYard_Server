package at.aau.serg.scotlandyard.gamelogic.player.tickets;

import java.util.EnumMap;
import java.util.Map;

public class PlayerTickets {
    private final Map<Ticket, Integer> tickets;

    public PlayerTickets(Map<Ticket, Integer> initialTickets) {
        this.tickets = new EnumMap<>(Ticket.class);
        this.tickets.putAll(initialTickets);
    }

    public boolean hasTicket(Ticket ticket) {
        return tickets.getOrDefault(ticket, 0) > 0;
    }

    public boolean has2Tickets(Ticket ticket1, Ticket ticket2) {
        if(ticket1.equals(ticket2)) {
            return tickets.getOrDefault(ticket1, 0) > 1;
        } else {
            return hasTicket(ticket1) && hasTicket(ticket2);
        }
    }


    public void useTicket(Ticket ticket) {
        if (!hasTicket(ticket)) {
            throw new IllegalStateException("Spieler hat dieses Ticket nicht");
        }
        tickets.put(ticket, tickets.get(ticket) - 1);
    }

    public void addTicket(Ticket ticket) {
        tickets.put(ticket, tickets.getOrDefault(ticket, 0) + 1);
    }

    public int getTicketCount(Ticket ticket) {
        return tickets.getOrDefault(ticket, 0);
    }

    public Map<Ticket, Integer> getTicketMap() {
        return Map.copyOf(tickets); // unveränderlich
    }

}


