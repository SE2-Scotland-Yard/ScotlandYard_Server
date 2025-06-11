package at.aau.serg.scotlandyard.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LobbyStateTest {

    private LobbyState lobbyState;
    private final String gameId = "game123";
    private final List<String> players = Arrays.asList("Player1", "Player2");
    private final Map<String, Boolean> readyStatus = Map.of("Player1", true, "Player2", false);
    private final Map<String, String> selectedRoles = Map.of("Player1", "Detective", "Player2", "Mr. X");
    private final boolean isPublic = true;
    private final boolean isStarted = false;
    private final int maxPlayers = 4;
    private final Map<String, Integer> avatars = Map.of("Player1", 1, "Player2", 2);

    @BeforeEach
    void setUp() {
        lobbyState = LobbyState.builder()
                .gameId(gameId)
                .players(players)
                .readyStatus(readyStatus)
                .selectedRoles(selectedRoles)
                .isPublic(isPublic)
                .isStarted(isStarted)
                .maxPlayers(maxPlayers)
                .currentPlayerCount(players.size()) // oder manuell setzen
                .avatars(avatars)
                .build();
    }


    // --- Game ID ---
    @Test
    void testGetGameId() {
        assertEquals("game123", lobbyState.getGameId());
    }

    @Test
    void testSetGameId() {
        lobbyState.setGameId("newGame");
        assertEquals("newGame", lobbyState.getGameId());
    }

    // --- Players ---
    @Test
    void testGetPlayers() {
        assertEquals(players, lobbyState.getPlayers());
    }

    @Test
    void testSetPlayers() {
        List<String> newPlayers = List.of("Player3");
        lobbyState.setPlayers(newPlayers);
        assertEquals(newPlayers, lobbyState.getPlayers());
    }

    // --- Ready Status ---
    @Test
    void testGetReadyStatus() {
        assertEquals(readyStatus, lobbyState.getReadyStatus());
    }

    @Test
    void testSetReadyStatus() {
        Map<String, Boolean> newStatus = Map.of("Player3", true);
        lobbyState.setReadyStatus(newStatus);
        assertEquals(newStatus, lobbyState.getReadyStatus());
    }

    // --- Selected Roles ---
    @Test
    void testGetSelectedRoles() {
        assertEquals(selectedRoles, lobbyState.getSelectedRoles());
    }

    // --- Public ---
    @Test
    void testIsPublic() {
        assertTrue(lobbyState.isPublic());
    }

    @Test
    void testSetPublic() {
        lobbyState.setPublic(false);
        assertFalse(lobbyState.isPublic());
    }

    // --- Started ---
    @Test
    void testIsStarted() {
        assertFalse(lobbyState.isStarted());
    }

    @Test
    void testSetStarted() {
        lobbyState.setStarted(true);
        assertTrue(lobbyState.isStarted());
    }

    // --- Max Players ---
    @Test
    void testGetMaxPlayers() {
        assertEquals(4, lobbyState.getMaxPlayers());
    }

    @Test
    void testSetMaxPlayers() {
        lobbyState.setMaxPlayers(6);
        assertEquals(6, lobbyState.getMaxPlayers());
    }

    // --- Current Player Count ---
    @Test
    void testGetCurrentPlayerCount() {
        assertEquals(2, lobbyState.getCurrentPlayerCount());
    }

    @Test
    void testSetCurrentPlayerCount() {
        lobbyState.setCurrentPlayerCount(3);
        assertEquals(3, lobbyState.getCurrentPlayerCount());
    }
}
