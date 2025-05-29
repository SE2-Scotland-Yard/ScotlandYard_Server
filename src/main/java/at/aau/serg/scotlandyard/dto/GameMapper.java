package at.aau.serg.scotlandyard.dto;

import at.aau.serg.scotlandyard.gamelogic.GameState;
import at.aau.serg.scotlandyard.gamelogic.player.MrX;
import at.aau.serg.scotlandyard.gamelogic.player.Player;

import java.util.HashMap;
import java.util.Map;

public class GameMapper {

    private GameMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static GameUpdate mapToGameUpdate(String gameId, Map<String, Integer> playerPositions, String currentPlayer, String winner) {

        return new GameUpdate(gameId, playerPositions, currentPlayer,winner);
    }
}

