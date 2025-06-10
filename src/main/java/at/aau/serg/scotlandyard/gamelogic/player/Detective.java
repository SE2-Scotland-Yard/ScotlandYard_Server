package at.aau.serg.scotlandyard.gamelogic.player;

import at.aau.serg.scotlandyard.gamelogic.player.tickets.PlayerTickets;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;

import java.util.EnumMap;
import java.util.Map;

public class Detective extends Player {

    public Detective(String name) {
        super(name,initializeTickets());

    }

    //für bots
    public Detective(String name, int startPos, PlayerTickets tickets) {
        super(name, tickets);
        this.setPos(startPos);
    }

    private static PlayerTickets initializeTickets() {
        Map<Ticket, Integer> initialTickets = new EnumMap<>(Ticket.class);
        initialTickets.put(Ticket.TAXI, 10);
        initialTickets.put(Ticket.BUS, 8);
        initialTickets.put(Ticket.UNDERGROUND, 4);
        return new PlayerTickets(initialTickets);
    }
}
