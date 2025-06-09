package at.aau.serg.scotlandyard.bot;

import at.aau.serg.scotlandyard.gamelogic.player.Detective;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.PlayerTickets;

public class BotDetective extends Detective {

    public BotDetective(String name, int startPos, PlayerTickets tickets) {
        super(name, startPos, tickets);
    }

    @Override
    public boolean isBot() {
        return true;
    }
}
