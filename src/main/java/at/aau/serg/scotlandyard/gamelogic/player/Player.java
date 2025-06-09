package at.aau.serg.scotlandyard.gamelogic.player;

import at.aau.serg.scotlandyard.gamelogic.board.Board;
import at.aau.serg.scotlandyard.gamelogic.board.Edge;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.PlayerTickets;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public abstract class Player {
    @Getter
    private final String name;
    @Getter
    protected final PlayerTickets tickets;
    @Setter
    private int pos;


    protected Player(String name, PlayerTickets tickets) {
        this.name = name;
        this.tickets = tickets;
        this.pos = new Random().nextInt(199) + 1;
    }

    public boolean isValidMove(int to, Ticket ticket, Board board) {
        if (!tickets.hasTicket(ticket)) {
            return false;
        }
        List<Edge> connections = board.getConnectionsFrom(this.pos);
        for (Edge edge : connections) {
            if (edge.getTo() == to && edge.getTicket() == ticket) {
                return true;
            }
        }
        return false;
    }

    public void move(int to, Ticket ticket, Board board) {
        if (isValidMove(to, ticket, board)) {
            tickets.useTicket(ticket);
            pos = to;
        } else {
            throw new IllegalArgumentException("Invalid move!");
        }
    }

    public int getPosition() {
        return pos;
    }

    public List<Integer> allowedNextMoves(Board board) {
        List<Integer> allowedMoves = new ArrayList<>();
        for (Edge edge : board.getConnectionsFrom(this.pos)) {
            Ticket requiredTicket = transportToTicket(edge.getTicket());
            if (tickets.hasTicket(requiredTicket)) {
                allowedMoves.add(edge.getTo());
            }
        }
        return allowedMoves;
    }

    private Ticket transportToTicket(Ticket ticket) {
        return ticket;
    }



}
