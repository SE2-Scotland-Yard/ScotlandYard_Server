package at.aau.serg.scotlandyard.bot;

import at.aau.serg.scotlandyard.gamelogic.player.MrX;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.PlayerTickets;

public class BotPlayer extends MrX {

    public BotPlayer(String name, int startPos, PlayerTickets tickets) {
        super(name, startPos, tickets);
    }
}
