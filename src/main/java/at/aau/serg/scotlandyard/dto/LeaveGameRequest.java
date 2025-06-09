package at.aau.serg.scotlandyard.dto;

public class LeaveGameRequest {
    private String gameId;
    private String playerId;

    public LeaveGameRequest() {

    }

    public LeaveGameRequest(String gameId, String playerId) {
        this.gameId = gameId;
        this.playerId = playerId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
