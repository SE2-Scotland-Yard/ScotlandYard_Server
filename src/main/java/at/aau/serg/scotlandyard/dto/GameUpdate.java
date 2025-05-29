package at.aau.serg.scotlandyard.dto;


import at.aau.serg.scotlandyard.gamelogic.GameState;

import java.util.Map;

public class GameUpdate {
    private String gameId;
    private Map<String, Integer> playerPositions;
    private String currentPlayer;
    private String winner;

    public GameUpdate(String gameId, Map<String, Integer> playerPositions, String currentPlayer, String winner) {
        this.gameId = gameId;
        this.playerPositions = playerPositions;
        this.currentPlayer = currentPlayer;
        this.winner = winner;
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

    public String getWinner() {
        return winner;
    }
}

