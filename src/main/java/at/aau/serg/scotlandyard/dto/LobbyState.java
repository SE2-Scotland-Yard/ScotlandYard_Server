package at.aau.serg.scotlandyard.dto;



import java.util.List;
import java.util.Map;

public class LobbyState {

    private String gameId;
    private List<String> players;
    private Map<String, Boolean> readyStatus;
    private boolean isPublic;
    private boolean isStarted;
    private int maxPlayers;
    private int currentPlayerCount;
    private Map<String, Integer> avatars;

    public LobbyState(String gameId,
                      List<String> players,
                      Map<String, Boolean> readyStatus,
                      Map<String, String> selectedRoles,
                      boolean isPublic,
                      boolean isStarted,
                      int maxPlayers, Map<String, Integer> avatars) {
        this.gameId = gameId;
        this.players = players;
        this.readyStatus = readyStatus;
        this.isPublic = isPublic;
        this.isStarted = isStarted;
        this.maxPlayers = maxPlayers;
        this.currentPlayerCount = players.size();
        this.selectedRoles = selectedRoles;
        this.avatars = avatars;

    }

    // Getter & Setter

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public List<String> getPlayers() {
        return players;
    }

    private Map<String, String> selectedRoles;

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public Map<String, Boolean> getReadyStatus() {
        return readyStatus;
    }

    public void setReadyStatus(Map<String, Boolean> readyStatus) {
        this.readyStatus = readyStatus;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getCurrentPlayerCount() {
        return currentPlayerCount;
    }

    public void setCurrentPlayerCount(int currentPlayerCount) {
        this.currentPlayerCount = currentPlayerCount;
    }

    public Map<String, String> getSelectedRoles() {
        return selectedRoles;
    }
    public Map<String, Integer> getAvatars() { return avatars; }

}
