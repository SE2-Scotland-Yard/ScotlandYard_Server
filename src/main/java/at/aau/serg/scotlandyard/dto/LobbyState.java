package at.aau.serg.scotlandyard.dto;



import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

public class LobbyState {

    @Getter
    @Setter
    private String gameId;
    @Getter
    @Setter
    private List<String> players;
    @Setter
    @Getter
    private Map<String, Boolean> readyStatus;
    private boolean isPublic;
    private boolean isStarted;
    @Setter
    @Getter
    private int maxPlayers;
    @Setter
    @Getter
    private int currentPlayerCount;
    @Getter
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

    @Getter
    private Map<String, String> selectedRoles;

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

}
