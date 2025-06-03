package at.aau.serg.scotlandyard.gamelogic;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LobbyManager {

    private final Map<String, Lobby> lobbies = new HashMap<>();
    private final Random random = new Random();

    public Lobby getOrCreateLobby(String gameId, boolean isPublic) {
        return lobbies.computeIfAbsent(gameId, id -> new Lobby(id, isPublic));
    }

    public Lobby getLobby(String gameId) {
        return lobbies.get(gameId);
    }

    public void removeLobby(String gameId) {
        lobbies.remove(gameId);
    }

    public Set<String> getAllLobbyIds() {
        return lobbies.keySet();
    }

    public Collection<Lobby> getAllLobbies() {
        return lobbies.values();
    }

    public boolean lobbyExists(String gameId) {
        return lobbies.containsKey(gameId);
    }

    public Collection<Lobby> getPublicLobbies() {
        return lobbies.values().stream()
                .filter(lobby -> lobby.isPublic() && !lobby.isStarted())
                .toList();
    }

    public Lobby createLobby(boolean isPublic) {
        String gameId;
        do {
            gameId = generateGameId(6);
        } while (lobbies.containsKey(gameId)); // Keine gleichen Lobby-IDs

        Lobby lobby = new Lobby(gameId, isPublic);
        lobbies.put(gameId, lobby);
        return lobby;
    }

    public void leaveLobby(String gameId, String playerId) {
        Lobby lobby = lobbies.get(gameId);
        if (lobby != null) {
            lobby.removePlayer(playerId);

            // Wenn die Lobby leer ist → löschen
            if (lobby.getPlayers().isEmpty()) {
                lobbies.remove(gameId);
            }
        }
    }



    private String generateGameId(int length) {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }





}
