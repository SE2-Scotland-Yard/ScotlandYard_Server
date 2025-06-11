package at.aau.serg.scotlandyard.dto;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BotMessageTest {

    @Test
    void testAddBotMessageSetAndGet() {
        AddBotMessage addMsg = new AddBotMessage();
        addMsg.setGameId("game123");

        assertEquals("game123", addMsg.getGameId());
    }

    @Test
    void testRemoveBotMessageSetAndGet() {
        RemoveBotMessage removeMsg = new RemoveBotMessage();
        removeMsg.setGameId("game456");

        assertEquals("game456", removeMsg.getGameId());
    }
}
