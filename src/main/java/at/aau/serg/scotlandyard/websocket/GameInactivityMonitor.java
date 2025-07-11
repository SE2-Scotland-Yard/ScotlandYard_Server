package at.aau.serg.scotlandyard.websocket;

import at.aau.serg.scotlandyard.bot.BotLogic;
import at.aau.serg.scotlandyard.gamelogic.GameManager;
import at.aau.serg.scotlandyard.gamelogic.GameState;
import at.aau.serg.scotlandyard.gamelogic.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GameInactivityMonitor {

    private final GameManager gameManager;
    private final SimpMessagingTemplate messaging;
    private static final Logger logger = LoggerFactory.getLogger(GameInactivityMonitor.class);

    public GameInactivityMonitor(GameManager gameManager, SimpMessagingTemplate messaging) {
        this.gameManager = gameManager;
        this.messaging = messaging;
    }

    @Scheduled(fixedRate = 30_000) // alle 30 Sekunden
    public void checkInactivePlayersInGame() {
        long now = System.currentTimeMillis();
        long timeoutMillis = 30L * 1000L; //30 sek

        for (String gameId : gameManager.getAllGameIds()) {
            GameState game = gameManager.getGame(gameId);
            if (game == null) continue;

            List<String> inactivePlayers = new ArrayList<>();

            for (Map.Entry<String, Long> entry : game.getLastActivityMap().entrySet()) {
                String player = entry.getKey();
                long lastActive = entry.getValue();
                if (now - lastActive > timeoutMillis) {
                    inactivePlayers.add(player);
                }
            }

            for (String player : inactivePlayers) {
                logger.info("Spieler im Spiel inaktiv: {} in Game {}",player, gameId);

                Player bot = game.replaceWithBot(player);

                if (bot == null) {
                    continue;
                }



                if (game.getCurrentPlayerName().equals(bot.getName())) {
                    var move = BotLogic.decideMove(bot.getName(), game);
                    if (move != null) {
                        game.movePlayer(bot.getName(), move.getKey(), move.getValue());
                    } else {
                        game.cantMove(gameId);
                    }
                }

            }

        }
    }


}
