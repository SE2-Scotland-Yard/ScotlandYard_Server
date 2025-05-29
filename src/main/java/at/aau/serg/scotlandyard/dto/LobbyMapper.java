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

        return new LobbyState(
                lobby.getGameId(),
                new ArrayList<>(lobby.getPlayers()),
                new HashMap<>(lobby.getReadyStatus()),
                roleMap,
                lobby.isPublic(),
                lobby.isStarted(),
                6,
                new HashMap<>(lobby.getAvatars())
        );

    }
}

