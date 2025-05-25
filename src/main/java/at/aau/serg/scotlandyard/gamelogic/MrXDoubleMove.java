package at.aau.serg.scotlandyard.gamelogic;

import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;

public class MrXDoubleMove {
    private final int firstMove;
    private final Ticket firstTicket;
    private final int secondMove;
    private final Ticket secondTicket;




    public MrXDoubleMove(int firstMove,Ticket firstTicket, int secondMove, Ticket secondTicket) {
        this.firstMove = firstMove;
        this.firstTicket = firstTicket;
        this.secondMove = secondMove;
        this.secondTicket = secondTicket;
    }

    public int getFirstMove() { return firstMove; }
    public int getSecondMove() { return secondMove; }
    public Ticket getFirstTicket() { return firstTicket; }
    public Ticket getSecondTicket() { return secondTicket; }
}
