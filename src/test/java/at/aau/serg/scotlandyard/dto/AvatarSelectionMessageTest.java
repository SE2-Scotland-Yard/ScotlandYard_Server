package at.aau.serg.scotlandyard.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class AvatarSelectionMessageTest {

    @Test
    void testSetAndGetGameId() {
        AvatarSelectionMessage msg = new AvatarSelectionMessage();
        msg.setGameId("game123");
        assertEquals("game123", msg.getGameId());
    }

    @Test
    void testGetPlayerIdDefault() {
        AvatarSelectionMessage msg = new AvatarSelectionMessage();
        assertNull(msg.getPlayerId());
    }

    @Test
    void testGetAvatarResIdDefault() {
        AvatarSelectionMessage msg = new AvatarSelectionMessage();
        assertEquals(0, msg.getAvatarResId());
    }
}
