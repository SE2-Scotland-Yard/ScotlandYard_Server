package at.aau.serg.scotlandyard.gamelogic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class LobbyTest {

    private Lobby lobby;

    @BeforeEach
    void setUp() {
        lobby = new Lobby("lobby", true);
    }

    @Test
    void testAddPlayer() {
        boolean success = lobby.addPlayer("Player");
        assertTrue(success);
        assertEquals(1, lobby.getPlayers().size());
    }

    @Test
    void testAddPlayerFull() {
        for(int i = 1; i <= 6; i++ ){
            lobby.addPlayer("Player" + i);
        }

        boolean success = lobby.addPlayer("Player7");
        assertFalse(success);
        assertEquals(6, lobby.getPlayers().size());
    }

    @Test
    void testAddPlayerDuplicate() {
        lobby.addPlayer("Player");
        boolean success = lobby.addPlayer("Player");
        assertFalse(success);
        assertEquals(1, lobby.getPlayers().size());
    }

    @Test
    void testAddPlayerStarted() {
        lobby.addPlayer("Player1");
        lobby.markStarted();
        boolean success = lobby.addPlayer("Player2");
        assertFalse(success);
        assertEquals(1, lobby.getPlayers().size());
    }

    @Test
    void testRemovePlayer() {
        lobby.addPlayer("Player");
        boolean success = lobby.removePlayer("Player");
        assertTrue(success);
        assertEquals(0, lobby.getPlayers().size());
    }

    @Test
    void testMarkReady(){
        lobby.addPlayer("Player");
        boolean success = lobby.markReady("Player");
        assertTrue(success);
        assertEquals(1, lobby.getReadyStatus().size());
    }

    @Test
    void testMarkReadyNoPlayer(){
        boolean success = lobby.markReady("Nothing");
        assertFalse(success);
    }

    @Test
    void testSelecetRole(){
        lobby.addPlayer("Player1");
        lobby.addPlayer("Player2");

        lobby.selectRole("Player1", Role.MRX);
        lobby.selectRole("Player2", Role.DETECTIVE);

        assertEquals(2, lobby.getPlayers().size());
        assertEquals(Role.MRX, lobby.getSelectedRole("Player1"));
        assertEquals(Role.DETECTIVE, lobby.getSelectedRole("Player2"));
    }

    @Test
    void testSelectRoleNoPlayer(){
        lobby.addPlayer("Player1");
        lobby.selectRole("Player2", Role.DETECTIVE);
        assertEquals(1, lobby.getPlayers().size());
        assertEquals(null, lobby.getSelectedRole("Player2"));
    }

    @Test
    void testAllReady(){
        lobby.addPlayer("Player1");
        lobby.addPlayer("Player2");
        lobby.markReady("Player1");
        lobby.markReady("Player2");
        boolean ready = lobby.allReady();
        assertTrue(ready);
    }

    @Test
    void testAllReadyEmpty(){
        boolean ready = lobby.allReady();
        assertFalse(ready);
    }

    @Test
    void testAllReadyNotReady(){
        lobby.addPlayer("Player1");
        lobby.addPlayer("Player2");
        lobby.markReady("Player1");
        boolean ready = lobby.allReady();
        assertFalse(ready);
    }

    @Test
    void testIsPlayerReady(){
        lobby.addPlayer("Player");
        lobby.markReady("Player");
        boolean ready = lobby.isPlayerReady("Player");
        assertTrue(ready);
    }

    @Test
    void testIsPlayerReadyNotReady(){
        lobby.addPlayer("Player1");
        boolean ready = lobby.isPlayerReady("Player");
        assertFalse(ready);
    }

    @Test
    void testGetGameID(){
        assertEquals("lobby", lobby.getGameId());
    }

    @Test
    void tesIsPublic(){
        assertTrue(lobby.isPublic());
    }

    @Test
    void testHasEnoughPlayers(){
        lobby.addPlayer("Player1");
        lobby.addPlayer("Player2");
        lobby.addPlayer("Player3");
        assertTrue(lobby.hasEnoughPlayers());
    }

    @Test
    void testHasEnoughPlayersNot(){
        assertFalse(lobby.hasEnoughPlayers());
    }

    @Test
    void testIsStarted(){
        assertFalse(lobby.isStarted());
    }

    @Test
    void testGetAllSelectedRoles(){
        lobby.addPlayer("Player1");
        lobby.addPlayer("Player2");
        lobby.selectRole("Player1", Role.DETECTIVE);
        lobby.selectRole("Player2", Role.DETECTIVE);
        assertEquals(2, lobby.getAllSelectedRoles().size());
    }

}
