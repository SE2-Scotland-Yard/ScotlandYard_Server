package at.aau.serg.scotlandyard.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LeaveRequestTest {

    @Test
    void testNoArgConstructor() {
        LeaveRequest request = new LeaveRequest();
        assertNull(request.getGameId());
        assertNull(request.getPlayerId());
    }

    @Test
    void testAllArgsConstructor() {
        LeaveRequest request = new LeaveRequest("game123", "player456");
        assertEquals("game123", request.getGameId());
        assertEquals("player456", request.getPlayerId());
    }

    @Test
    void testSetAndGetGameId() {
        LeaveRequest request = new LeaveRequest();
        request.setGameId("game789");
        assertEquals("game789", request.getGameId());
    }

    @Test
    void testSetAndGetPlayerId() {
        LeaveRequest request = new LeaveRequest();
        request.setPlayerId("player999");
        assertEquals("player999", request.getPlayerId());
    }

}
