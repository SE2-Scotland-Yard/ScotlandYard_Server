package at.aau.serg.scotlandyard.controller;

import at.aau.serg.scotlandyard.dto.GameOverviewDTO;
import at.aau.serg.scotlandyard.gamelogic.GameManager;
import at.aau.serg.scotlandyard.gamelogic.GameState;
import at.aau.serg.scotlandyard.gamelogic.MrXDoubleMove;
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
    private static final String MESSAGE = "message";

    public GameController(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @GetMapping("/mrXPosition")
    public ResponseEntity<?> mrXPosition(
            @RequestParam String gameId,
            @RequestParam String name
    ) {
        GameState game = gameManager.getGame(gameId);

        if (game == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Game mit ID '" + gameId + "' nicht gefunden.");
        }

        int mrXPosition = game.getMrXPosition(name);
        return ResponseEntity.ok(mrXPosition);
    }


    @GetMapping("/allowedMoves")
    public ResponseEntity<?> getMoves(
            @RequestParam String gameId,
            @RequestParam String name
    ) {
        GameState game = gameManager.getGame(gameId);

        if (game == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Game mit ID '" + gameId + "' nicht gefunden.");
        }

        List<Map.Entry<Integer, String>> allowedMoves = game.getAllowedMoves(name).stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().name())) // Ticket zu String konvertieren
                .toList();
        logger.info("Allowed moves: {}",allowedMoves);
        return ResponseEntity.ok(allowedMoves);
    }

    @PostMapping("/blackTicket")
    public Map<String, String> blackTicket(
            @RequestParam String gameId,
            @RequestParam String name,
            @RequestParam int to,
            @RequestParam String gotTicket
    ){
        Map<String, String> response = new HashMap<>();
        Ticket ticket;
        try {
            ticket = Ticket.valueOf(gotTicket);
        } catch (IllegalArgumentException e) {
            response.put(MESSAGE, "Ungültiges Ticket: " + gotTicket);
            return response;
        }

        GameState game = gameManager.getGame(gameId);
        if (game == null) {
            response.put(MESSAGE, "Spiel nicht gefunden!");
            return response;
        }

        if (!game.getAllPlayers().containsKey(name)) {
            response.put(MESSAGE, "Spieler " + name + " existiert nicht!");
            return response;
        }

        if (!game.movePlayer(name, to, ticket)) {
            response.put(MESSAGE, "Ungültiger Zug!");
            return response;
        }

        if (game.getWinner() != GameState.Winner.NONE) {
            response.put(MESSAGE, getWinner(gameId));
            return response;
        }

        response.put(MESSAGE, "Spieler " + name + " bewegt sich zu " + to + " in Spiel " + gameId);
        return response;
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
            response.put(MESSAGE, "Ungültiges Ticket: " + gotTicket);
            return response;
        }

        // 2. Spiel validieren
        GameState game = gameManager.getGame(gameId);
        if (game == null) {
            response.put(MESSAGE, "Spiel nicht gefunden!");
            return response;
        }

        // 3. Spieler existiert?
        if (!game.getAllPlayers().containsKey(name)) {
            response.put(MESSAGE, "Spieler " + name + " existiert nicht!");
            return response;
        }

        // 4. Zug durchführen
        if (!game.movePlayer(name, to, ticket)) {
            response.put(MESSAGE, "Ungültiger Zug!");
            return response;
        }

        // 5. Gewinner prüfen
        if (game.getWinner() != GameState.Winner.NONE) {
            response.put(MESSAGE, getWinner(gameId));
            return response;
        }

        response.put(MESSAGE, "Spieler " + name + " bewegt sich zu " + to + " in Spiel " + gameId);
        return response;
    }

    @PostMapping("/moveDouble")
    public ResponseEntity<Map<String, Object>> moveDouble(@RequestParam String gameId,
                                                          @RequestParam String name,
                                                          @RequestParam int firstTo,
                                                          @RequestParam Ticket firstTicket,
                                                          @RequestParam int secondTo,
                                                          @RequestParam Ticket secondTicket) {
        Map<String, Object> response = new HashMap<>();
        GameState game = gameManager.getGame(gameId);
        if (game == null) {
            response.put("status", "error");
            response.put("message", "Spiel nicht gefunden!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (!game.moveMrXDouble(name, firstTo, firstTicket, secondTo, secondTicket)) {
            response.put("status", "error");
            response.put("message", "Ungültiger Doppelzug!");
            return ResponseEntity.badRequest().body(response);
        }

        response.put("status", "success");
        response.put("message", "MrX machte einen Doppelzug: " + firstTo + " → " + secondTo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/allowedDoubleMoves")
    public ResponseEntity<?> getDoubleMoves(
            @RequestParam String gameId,
            @RequestParam String name
    ){
        GameState game = gameManager.getGame(gameId);

        if (game == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Game mit ID '" + gameId + "' nicht gefunden.");
        }

        List<MrXDoubleMove> doubleMoves = game.getAllowedDoubleMoves(name);

        List<Map<String, Object>> response = doubleMoves.stream().map(move -> {
            Map<String, Object> map = new HashMap<>();
            map.put("firstTo", move.getFirstMove());
            map.put("firstTicket", move.getFirstTicket().name());
            map.put("secondTo", move.getSecondMove());
            map.put("secondTicket", move.getSecondTicket().name());
            return map;
        }).toList();

        return ResponseEntity.ok(response);

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
