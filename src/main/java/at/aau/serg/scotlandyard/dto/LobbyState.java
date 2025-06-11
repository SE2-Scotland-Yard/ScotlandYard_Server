package at.aau.serg.scotlandyard.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class LobbyState {
    private String gameId;
    private List<String> players;

    @Builder.Default
    private Map<String, Boolean> readyStatus = new HashMap<>();

    @Builder.Default
    private Map<String, String> selectedRoles = new HashMap<>();

    @Builder.Default
    private Map<String, Integer> avatars = new HashMap<>();

    @Builder.Default
    private boolean isPublic = false;

    @Builder.Default
    private boolean isStarted = false;

    private int maxPlayers;
    private int currentPlayerCount;
}

