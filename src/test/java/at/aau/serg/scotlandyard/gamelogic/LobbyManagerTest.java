package at.aau.serg.scotlandyard.gamelogic;

import org.junit.jupiter.api.Test;
import java.util.Collection;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;


class LobbyManagerTest {

    @Test
    void getOrCreateLobby_newLobby() {
        LobbyManager lobbyManager = new LobbyManager();
        String gameId = "ABCDEF";
        Lobby lobby = lobbyManager.getOrCreateLobby(gameId, true);
        assertNotNull(lobby);
        assertEquals(gameId, lobby.getGameId());
        assertTrue(lobby.isPublic());
        assertTrue(lobbyManager.lobbyExists(gameId));
    }

    @Test
    void getOrCreateLobby_existingLobby() {
        LobbyManager lobbyManager = new LobbyManager();
        String gameId = "GHIJKL";
        Lobby existingLobby = lobbyManager.getOrCreateLobby(gameId, false);
        Lobby retrievedLobby = lobbyManager.getOrCreateLobby(gameId, true); // isPublic sollte keinen Unterschied machen
        assertSame(existingLobby, retrievedLobby);
        assertEquals(gameId, retrievedLobby.getGameId());
        assertFalse(retrievedLobby.isPublic()); // Behält den ursprünglichen Wert
    }

    @Test
    void getLobby_existingLobby() {
        LobbyManager lobbyManager = new LobbyManager();
        String gameId = "MNOPQR";
        Lobby createdLobby = lobbyManager.getOrCreateLobby(gameId, true);
        Lobby retrievedLobby = lobbyManager.getLobby(gameId);
        assertSame(createdLobby, retrievedLobby);
    }

    @Test
    void getLobby_nonExistingLobby() {
        LobbyManager lobbyManager = new LobbyManager();
        Lobby retrievedLobby = lobbyManager.getLobby("STUVWX");
        assertNull(retrievedLobby);
    }

    @Test
    void removeLobby_existingLobby() {
        LobbyManager lobbyManager = new LobbyManager();
        String gameId = "YZ1234";
        lobbyManager.getOrCreateLobby(gameId, true);
        assertTrue(lobbyManager.lobbyExists(gameId));
        lobbyManager.removeLobby(gameId);
        assertFalse(lobbyManager.lobbyExists(gameId));
        assertNull(lobbyManager.getLobby(gameId));
    }

    @Test
    void removeLobby_nonExistingLobby() {
        LobbyManager lobbyManager = new LobbyManager();
        lobbyManager.removeLobby("567890"); // Sollte keine Fehler verursachen
        assertFalse(lobbyManager.lobbyExists("567890"));
    }

    @Test
    void getAllLobbyIds_multipleLobbies() {
        LobbyManager lobbyManager = new LobbyManager();
        lobbyManager.getOrCreateLobby("Lobby1", true);
        lobbyManager.getOrCreateLobby("Lobby2", false);
        lobbyManager.getOrCreateLobby("Lobby3", true);
        Set<String> allLobbyIds = lobbyManager.getAllLobbyIds();
        assertEquals(3, allLobbyIds.size());
        assertTrue(allLobbyIds.contains("Lobby1"));
        assertTrue(allLobbyIds.contains("Lobby2"));
        assertTrue(allLobbyIds.contains("Lobby3"));
    }

    @Test
    void getAllLobbyIds_noLobbies() {
        LobbyManager lobbyManager = new LobbyManager();
        Set<String> allLobbyIds = lobbyManager.getAllLobbyIds();
        assertTrue(allLobbyIds.isEmpty());
    }

    @Test
    void getAllLobbies_multipleLobbies() {
        LobbyManager lobbyManager = new LobbyManager();
        Lobby lobby1 = lobbyManager.getOrCreateLobby("LobbyA", true);
        Lobby lobby2 = lobbyManager.getOrCreateLobby("LobbyB", false);
        Collection<Lobby> allLobbies = lobbyManager.getAllLobbies();
        assertEquals(2, allLobbies.size());
        assertTrue(allLobbies.contains(lobby1));
        assertTrue(allLobbies.contains(lobby2));
    }

    @Test
    void getAllLobbies_noLobbies() {
        LobbyManager lobbyManager = new LobbyManager();
        Collection<Lobby> allLobbies = lobbyManager.getAllLobbies();
        assertTrue(allLobbies.isEmpty());
    }

    @Test
    void lobbyExists_existingLobby() {
        LobbyManager lobbyManager = new LobbyManager();
        lobbyManager.getOrCreateLobby("Exists", true);
        assertTrue(lobbyManager.lobbyExists("Exists"));
    }

    @Test
    void lobbyExists_nonExistingLobby() {
        LobbyManager lobbyManager = new LobbyManager();
        assertFalse(lobbyManager.lobbyExists("NotExists"));
    }



    @Test
    void getPublicLobbies_noPublicLobbies() {
        LobbyManager lobbyManager = new LobbyManager();
        lobbyManager.createLobby(false);
        lobbyManager.createLobby(false);
        assertTrue(lobbyManager.getPublicLobbies().isEmpty());
    }

}