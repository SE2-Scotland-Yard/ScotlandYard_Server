package at.aau.serg.scotlandyard.bot;

import at.aau.serg.scotlandyard.gamelogic.GameState;
import at.aau.serg.scotlandyard.gamelogic.player.Player;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;

import java.util.List;
import java.util.Map;

public class BotLogic {
    public static Map.Entry<Integer, Ticket> decideMove(String playerName, GameState gameState) {
        List<Map.Entry<Integer, Ticket>> moves = gameState.getAllowedMoves(playerName);
        return moves.isEmpty() ? null : moves.get(0);
    }


    public static void executeBotMove(Player bot, GameState gameState) {
        var move = decideMove(bot.getName(), gameState);
        if (move != null) {
            gameState.movePlayer(bot.getName(), move.getKey(), move.getValue());
        } else {
            gameState.cantMove(gameState.getGameId());
        }
    }

}
