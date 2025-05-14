package at.aau.serg.scotlandyard.controller;

import at.aau.serg.scotlandyard.dto.GameOverviewDTO;
import at.aau.serg.scotlandyard.gamelogic.GameManager;
import at.aau.serg.scotlandyard.gamelogic.GameState;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/game")
public class GameController {

    public static final String GAME_NOT_FOUND = "Spiel nicht gefunden";
    private final GameManager gameManager;
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    public GameController(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @GetMapping("/allowedMoves")
    public ResponseEntity<List<Map.Entry<Integer, String>>> getMoves(
            @RequestParam String gameId,
            @RequestParam String name
    ) {
        GameState game = gameManager.getGame(gameId);

        if (game == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(List.of(Map.entry(-1, "Game mit ID '" + gameId + "' nicht gefunden.")));
        }

        List<Map.Entry<Integer, String>> allowedMoves = game.getAllowedMoves(name).stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().name())) // Ticket zu String konvertieren
                .toList();
        logger.info("Allowed moves: {}",allowedMoves);
        return ResponseEntity.ok(allowedMoves);
    }
    @PostMapping("/move")
    public Map<String, String> move(
            @RequestParam String gameId,
            @RequestParam String name,
            @RequestParam int to,
            @RequestParam String gotTicket
    ) {

        Map<String, String> response = new HashMap<>();
        // 1. Ticket validieren
        Ticket ticket;
        try {
            ticket = Ticket.valueOf(gotTicket);
        } catch (IllegalArgumentException e) {
            response.put("message", "Ungültiges Ticket: " + gotTicket);
            return response;
        }

        // 2. Spiel validieren
        GameState game = gameManager.getGame(gameId);
        if (game == null) {
            response.put("message", "Spiel nicht gefunden!");
            return response;
        }

        // 3. Spieler existiert?
        if (!game.getAllPlayers().containsKey(name)) {
            response.put("message", "Spieler " + name + " existiert nicht!");
            return response;
        }

        // 4. Zug durchführen
        if (!game.movePlayer(name, to, ticket)) {
            response.put("message", "Ungültiger Zug!");
            return response;
        }

        // 5. Gewinner prüfen
        if (game.getWinner() != GameState.Winner.NONE) {
            response.put("message", getWinner(gameId));
            return response;
        }

        response.put("message", "Spieler " + name + " bewegt sich zu " + to + " in Spiel " + gameId);
        return response;
    }

    @PostMapping("/moveDouble")
    public String moveDouble(@RequestParam String gameId,
                             @RequestParam String name,
                             @RequestParam int firstTo,
                             @RequestParam Ticket firstTicket,
                             @RequestParam int secondTo,
                             @RequestParam Ticket secondTicket) {
        GameState game = gameManager.getGame(gameId);
        if (game == null) return GAME_NOT_FOUND;
        if (!game.moveMrXDouble(name, firstTo, firstTicket, secondTo, secondTicket)) {
            return "Ungültiger Doppelzug!";
        }
        return "MrX machte einen Doppelzug: " + firstTo + " → " + secondTo;
    }

    @GetMapping("/mrXposition")
    public String getMrXPosition(@RequestParam String gameId) {
        GameState game = gameManager.getGame(gameId);
        return (game != null) ? game.getVisibleMrXPosition() : GAME_NOT_FOUND;
    }

    @GetMapping("/mrXhistory")
    public List<String> getMrXHistory(@RequestParam String gameId) {
        GameState game = gameManager.getGame(gameId);
        return (game != null) ? game.getMrXMoveHistory() : List.of(GAME_NOT_FOUND);
    }

    @GetMapping("/all")
    public List<GameOverviewDTO> getAllGames() {
        return gameManager.getAllGameIds().stream()
                .map(id -> {
                    GameState game = gameManager.getGame(id);
                    Map<String, String> players = new HashMap<>();
                    for (var entry : game.getAllPlayers().entrySet()) {
                        players.put(entry.getKey(), entry.getValue().getClass().getSimpleName());
                    }
                    return new GameOverviewDTO(id, players);
                })
                .toList();
    }

    @GetMapping("/winner")
    public String getWinner(@RequestParam String gameId) {
        GameState game = gameManager.getGame(gameId);
        if (game == null) return GAME_NOT_FOUND;
        switch (game.getWinner()) {
            case MR_X:
                return "Mr.X hat gewonnen!";
            case DETECTIVE:
                return "Detektive haben gewonnen!";
            default:
                return "Spiel läuft noch.";
        }
    }
}
