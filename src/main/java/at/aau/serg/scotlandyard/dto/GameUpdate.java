package at.aau.serg.scotlandyard.dto;


import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;

import java.util.Map;

public class GameUpdate {
    private String gameId;
    private Map<String, Integer> playerPositions;
    private String currentPlayer;
    private Ticket lastTicketUsed;

    public GameUpdate(String gameId, Map<String, Integer> playerPositions, String currentPlayer, Ticket lastTicketUsed) {
        this.gameId = gameId;
        this.playerPositions = playerPositions;
        this.currentPlayer = currentPlayer;
        this.lastTicketUsed = lastTicketUsed;
    }

    public String getGameId() {
        return gameId;
    }

    public String getCurrentPlayer(){

        return currentPlayer;
    }

    public Map<String, Integer> getPlayerPositions() {
        return playerPositions;
    }

    public  Ticket getLastTicketUsed(){

        return lastTicketUsed;
    }
}

