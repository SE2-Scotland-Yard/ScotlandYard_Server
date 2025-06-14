package at.aau.serg.scotlandyard.bot;

import at.aau.serg.scotlandyard.gamelogic.player.*;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.PlayerTickets;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BotFactoryTest {

    private Detective detective;
    private MrX mrx;

    @BeforeEach
    void setUp() {
        Map<Ticket, Integer> ticketMap = new EnumMap<>(Ticket.class);
        ticketMap.put(Ticket.TAXI, 10);
        ticketMap.put(Ticket.BUS, 8);
        ticketMap.put(Ticket.UNDERGROUND, 4);
        ticketMap.put(Ticket.BLACK, 0);
        ticketMap.put(Ticket.DOUBLE, 0);

        PlayerTickets tickets = new PlayerTickets(ticketMap);
        detective = new Detective("Bot1", 42, tickets);
        mrx = new MrX("Bot2", 55, tickets);
    }

    @Test
    void testCreateBotDetectiveReturnsDetectiveInstance() {
        Player bot = BotFactory.createBotReplacement(detective);

        assertNotNull(bot);
        assertInstanceOf(BotDetective.class, bot);
        assertEquals("[BOT] Bot1", bot.getName());
        assertEquals(detective.getPosition(), bot.getPosition());

    }

    @Test
    void testCreateBotDetectiveReturnsMrXInstance() {
        Player bot2 = BotFactory.createBotReplacement(mrx);

        assertNotNull(bot2);
        assertInstanceOf(MrX.class, bot2);
        assertEquals("[BOT] Bot2", bot2.getName());
        assertEquals(mrx.getPosition(), bot2.getPosition());

    }
}

