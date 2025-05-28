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

    public void addTicket(Ticket ticket){
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

    public void moveDouble(int firstTo, Ticket firstTicket, int secondTo, Ticket secondTicket, Board board) {
        if (!tickets.hasTicket(Ticket.DOUBLE)) {
            throw new IllegalArgumentException("Kein DOUBLE-Ticket verfügbar!");
        }

        // Prüfe beide Züge
        if (!isValidMove(firstTo, firstTicket, board)) {
            throw new IllegalArgumentException("Erster Zug ungültig!");
        }

        // Simuliere erste Bewegung temporär, um zweiten Zug zu validieren
        int originalPos = this.getPosition();
        this.setPos(firstTo);

        boolean validSecond = isValidMove(secondTo, secondTicket, board);
        this.setPos(originalPos); // Position zurücksetzen

        if (!validSecond) {
            throw new IllegalArgumentException("Zweiter Zug ungültig!");
        }

        // Zug ist gültig → Tickets verwenden und Position setzen
        tickets.useTicket(Ticket.DOUBLE);
        tickets.useTicket(firstTicket);
        this.setPos(firstTo);

        tickets.useTicket(secondTicket);
        this.setPos(secondTo);

        logger.info("MrX machte einen Doppelzug: {} → {}", firstTo, secondTo);
    }


    public void moveBlack(int to, Ticket ticket, Board board){
        if (isValidMove(to, ticket, board)) {
            tickets.useTicket(Ticket.BLACK);
            setPos(to);
        } else {
            throw new IllegalArgumentException("Invalid move!");
        }

    }
}
