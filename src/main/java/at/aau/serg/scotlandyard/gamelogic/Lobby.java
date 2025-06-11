package at.aau.serg.scotlandyard.gamelogic;

import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Lobby {

    @Getter
    private final String gameId;
    @Getter
    private final Set<String> players = new HashSet<>();
    @Getter
    private final Map<String, Boolean> readyStatus = new HashMap<>();
    private final Map<String, Role> selectedRoles = new HashMap<>();
    private static final int MINPLAYERS = 3;
    private static final int MAXPLAYERS = 6;
    private final boolean isPublic;
    @Getter
    private boolean started = false;
    private final Map<String, Integer> avatars = new ConcurrentHashMap<>();
    private final Map<String, Long> lastActivityMap = new HashMap<>();

    public Lobby(String gameId, boolean isPublic) {
        this.gameId = gameId;
        this.isPublic = isPublic;
    }

    public boolean addPlayer(String name) {
        if (started || players.size() >= MAXPLAYERS) return false;
        boolean added = players.add(name);
        if (added) {
            readyStatus.put(name, false); // standardmäßig nicht bereit
        }
        return added;
    }

    public boolean removePlayer(String name) {
        readyStatus.remove(name);
        selectedRoles.remove(name);
        return players.remove(name);
    }

    public boolean markReady(String name) {
        if (!players.contains(name)) return false;
        readyStatus.put(name, true);
        return true;
    }

    public void selectRole(String player, Role role) {
        if (players.contains(player)) {
            selectedRoles.put(player, role);

            if (role == Role.MRX) {
                avatars.remove(player);
            }
        }
    }

    public Role getSelectedRole(String player) {
        return selectedRoles.get(player);
    }



    public boolean isPlayerReady(String name) {
        return readyStatus.getOrDefault(name, false);
    }

    public boolean allReady() {
        return !readyStatus.isEmpty() &&
                readyStatus.values().stream().allMatch(Boolean::booleanValue);
    }

    public void selectAvatar(String player, Integer avatar) {
        // Wenn der Spieler MRX ist → kein Avatar erlaubt
        if ("MRX".equals(selectedRoles.get(player))) {
            avatars.remove(player);
            return;
        }

        if (avatars.containsValue(avatar) && !avatar.equals(avatars.get(player))) {
            return;
        }

        avatars.put(player, avatar);
    }

    public boolean hasExactlyOneMrX() {
        return selectedRoles.values().stream()
                .filter(role -> role == Role.MRX)
                .count() == 1;
    }

    public void updateLastActivity(String playerId) {
        lastActivityMap.put(playerId, System.currentTimeMillis());
    }

    public Long getLastActivity(String playerId) {
        return lastActivityMap.getOrDefault(playerId, 0L);
    }


    public boolean isPublic() {
        return isPublic;
    }

    public void markStarted() {
        this.started = true;
    }

    public void markNotReady(String playerId) {
        readyStatus.put(playerId, false);
    }


    public boolean hasEnoughPlayers() {

        return players.size()>= MINPLAYERS;
    }

    public Map<String, Role> getAllSelectedRoles() {
        return selectedRoles;
    }




    public Map<String, Integer> getAvatars() {
        return avatars;
    }

}

