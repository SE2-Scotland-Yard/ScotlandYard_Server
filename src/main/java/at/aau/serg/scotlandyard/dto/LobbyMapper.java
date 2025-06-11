package at.aau.serg.scotlandyard.dto;

import at.aau.serg.scotlandyard.gamelogic.Lobby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LobbyMapper {

    protected LobbyMapper() {
        throw new UnsupportedOperationException("LobbyMapper ist eine Hilfsklasse und darf nicht instanziiert werden.");
    }

    public static LobbyState toLobbyState(Lobby lobby) {
        Map<String, String> roleMap = new HashMap<>();
        for (var entry : lobby.getAllSelectedRoles().entrySet()) {
            roleMap.put(entry.getKey(), entry.getValue().name()); // Enum → String
        }

        return LobbyState.builder()
                .gameId(lobby.getGameId())
                .players(new ArrayList<>(lobby.getPlayers()))
                .readyStatus(new HashMap<>(lobby.getReadyStatus()))
                .selectedRoles(roleMap)
                .isPublic(lobby.isPublic())
                .isStarted(lobby.isStarted())
                .maxPlayers(6)
                .currentPlayerCount(lobby.getPlayers().size())
                .avatars(new HashMap<>(lobby.getAvatars()))
                .build();
    }
}

