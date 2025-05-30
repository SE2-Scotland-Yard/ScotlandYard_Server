package at.aau.serg.scotlandyard.gamelogic.player;

import at.aau.serg.scotlandyard.gamelogic.board.Board;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.PlayerTickets;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

public class MrX extends Player {

    private static final Logger logger = LoggerFactory.getLogger(MrX.class);

    public MrX(String name) {
        super(name, initializeTickets());
    }

    private static PlayerTickets initializeTickets() {
        Map<Ticket, Integer> initialTickets = new EnumMap<>(Ticket.class);

        initialTickets.put(Ticket.TAXI, 4);
        initialTickets.put(Ticket.BUS, 3);
        initialTickets.put(Ticket.UNDERGROUND, 3);
        initialTickets.put(Ticket.BLACK, 5);
        initialTickets.put(Ticket.DOUBLE, 2);

        return new PlayerTickets(initialTickets);
    }

    public void addTicket(Ticket ticket) {
        switch (ticket) {
            case TAXI:
                tickets.addTicket(Ticket.TAXI);
                break;
            case BUS:
                tickets.addTicket(Ticket.BUS);
                break;
            case UNDERGROUND:
                tickets.addTicket(Ticket.UNDERGROUND);
                break;
        }
    }

    public void moveDouble(int to, Ticket ticket1, Ticket ticket2, Board board) {
        if (!tickets.hasTicket(Ticket.DOUBLE)) {
            throw new IllegalArgumentException("Kein DOUBLE-Ticket verfügbar!");
        }
        System.out.println("HALLO MOVEEEEE");
        tickets.useTicket(Ticket.DOUBLE);
        setPos(to);

        logger.info("MrX machte einen Doppelzug: {} → {}", to, Ticket.DOUBLE);
    }


    public void moveBlack(int to, Ticket ticket, Board board) {
        if (isValidMove(to, ticket, board)) {
            tickets.useTicket(Ticket.BLACK);
            setPos(to);
        } else {
            throw new IllegalArgumentException("Invalid move!");
        }

    }
}