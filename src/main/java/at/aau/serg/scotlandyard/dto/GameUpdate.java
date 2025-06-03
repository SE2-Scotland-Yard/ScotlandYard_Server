package at.aau.serg.scotlandyard.dto;


import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;


import java.util.Map;

public class GameUpdate {
    private String gameId;
    private Map<String, Integer> playerPositions;
    private String currentPlayer;
    private String winner;
    private Ticket lastTicketUsed;
    private Map<String, Map<Ticket, Integer>> ticketInventory;


    public GameUpdate(String gameId, Map<String, Integer> playerPositions, String currentPlayer, String winner, Ticket lastTicketUsed, Map<String, Map<Ticket, Integer>> ticketInventory) {
        this.gameId = gameId;
        this.playerPositions = playerPositions;
        this.currentPlayer = currentPlayer;
        this.winner = winner;
        this.lastTicketUsed = lastTicketUsed;
        this.ticketInventory = ticketInventory;
    }

    public String getGameId() {
        return gameId;
    }

    public String getCurrentPlayer() {

        return currentPlayer;
    }

    public Map<String, Integer> getPlayerPositions() {
        return playerPositions;
    }


    public  Ticket getLastTicketUsed() {

        return lastTicketUsed;
    }

    public String getWinner() {
        return winner;

    }

    public Map<String, Map<Ticket, Integer>> getTicketInventory() {
        return ticketInventory;
    }
}

