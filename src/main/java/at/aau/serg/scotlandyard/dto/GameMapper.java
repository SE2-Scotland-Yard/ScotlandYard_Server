package at.aau.serg.scotlandyard.dto;

import at.aau.serg.scotlandyard.gamelogic.player.Player;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;

import java.util.HashMap;
import java.util.Map;

public class GameMapper {

    private GameMapper() {
        throw new UnsupportedOperationException("Utility class");
    }


    public static GameUpdate mapToGameUpdate(
            String gameId,
            Map<String, Integer> playerPositions,
            String currentPlayer,
            String winner,
            Ticket lastTicketUsed,
            Map<String, Player> players // <- neue Übergabe
    ) {
        Map<String, Map<Ticket, Integer>> ticketInventory = new HashMap<>();

        for (Map.Entry<String, Player> entry : players.entrySet()) {
            Player player = entry.getValue();
            String playerName = entry.getKey();
            ticketInventory.put(playerName, new HashMap<>(player.getTickets().getTicketMap()));
        }

        return new GameUpdate(gameId, playerPositions, currentPlayer, winner, lastTicketUsed, ticketInventory);
    }

}


