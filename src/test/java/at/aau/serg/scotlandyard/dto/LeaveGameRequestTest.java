package at.aau.serg.scotlandyard.dto;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LeaveGameRequestTest {

    @Test
    void testConstructorAndGetters() {
        LeaveGameRequest req = new LeaveGameRequest("game42", "playerX");

        assertEquals("game42", req.getGameId());
        assertEquals("playerX", req.getPlayerId());
    }

    @Test
    void testDefaultConstructorAndSetters() {
        LeaveGameRequest req = new LeaveGameRequest();

        // Setter verwenden
        req.setGameId("game99");
        req.setPlayerId("playerY");

        // Getter prüfen
        assertEquals("game99", req.getGameId());
        assertEquals("playerY", req.getPlayerId());
    }

    @Test
    void testSetterOverridesValue() {
        LeaveGameRequest req = new LeaveGameRequest("initGame", "initPlayer");

        // Werte ändern
        req.setGameId("game77");
        req.setPlayerId("playerZ");

        assertEquals("game77", req.getGameId());
        assertEquals("playerZ", req.getPlayerId());
    }
}
