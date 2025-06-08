package at.aau.serg.scotlandyard.bot;

import at.aau.serg.scotlandyard.gamelogic.player.*;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.PlayerTickets;

public class BotFactory {

    public static Player createBotReplacement(Player original) {
        String name = original.getName();
        if (!name.startsWith("[BOT] ")) {
            name = "[BOT] " + name;
        }

        int pos = original.getPosition();
        PlayerTickets tickets = original.getTickets().copy(); //aktuelle tickets kopieren

        if (original instanceof MrX) {
            return new BotPlayer(name, pos, tickets);
        } else if (original instanceof Detective) {
            return new BotDetective(name, pos, tickets);
        }
        return null;
    }
}
