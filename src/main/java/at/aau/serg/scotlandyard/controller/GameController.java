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
    private static final String MESSAGE = "message";
    private static final String GAME_MIT_ID = "Game mit ID '";
    private static final String NICHT_GEFUNDEN = "' nicht gefunden.";
    private static final String UNGUELTIGES_TICKET = "Ungütliges Ticket: ";
    private static final String SPIEL_NICHT_GEFUNDEN = "Spiel nicht gefunden!";
    private static final String EXISTIERT_NICHT = " existiert nicht!";
    private static final String SPIELER = "Spieler ";
    private static final String UNGUELTIGER_ZUG = "Ungültiger Zug!";
    private static final String BEWEGT_SICH_ZU = " bewegt sich zu ";
    private static final String IN_SPIEL = " in Spiel ";





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
                    .body(GAME_MIT_ID + gameId + NICHT_GEFUNDEN);
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
                    .body(GAME_MIT_ID + gameId + NICHT_GEFUNDEN);
        }

        List<Map.Entry<Integer, String>> allowedMoves = game.getAllowedMoves(name).stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().name())) // Ticket zu String konvertieren
                .toList();
        logger.info("Allowed moves: {}", allowedMoves);
        if (allowedMoves.isEmpty()) {
            game.cantMove(gameId);

        }
        return ResponseEntity.ok(allowedMoves);
    }

    @PostMapping("/blackMove")
    public Map<String, String> blackTicket(
            @RequestParam String gameId,
            @RequestParam String name,
            @RequestParam int to,
            @RequestParam String gotTicket
    ) {
        Map<String, String> response = new HashMap<>();
        Ticket ticket;
        try {
            ticket = Ticket.valueOf(gotTicket);
        } catch (IllegalArgumentException e) {
            response.put(MESSAGE, UNGUELTIGES_TICKET + gotTicket);
            return response;
        }
        GameState game = gameManager.getGame(gameId);
        if (game == null) {
            response.put(MESSAGE, SPIEL_NICHT_GEFUNDEN);
            return response;
        }
        if (!game.getAllPlayers().containsKey(name)) {
            response.put(MESSAGE, SPIELER + name + EXISTIERT_NICHT);
            return response;
        }
        if (!game.moveBlackTicket(name, to, ticket)) {
            response.put(MESSAGE, UNGUELTIGER_ZUG);
            return response;
        }
        if (game.getWinner() != GameState.Winner.NONE) {
            response.put(MESSAGE, getWinner(gameId));
            return response;
        }

        response.put(MESSAGE, SPIELER + name + BEWEGT_SICH_ZU + to + IN_SPIEL + gameId);
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
            response.put(MESSAGE, UNGUELTIGES_TICKET + gotTicket);
            return response;
        }

        // 2. Spiel validieren
        GameState game = gameManager.getGame(gameId);
        if (game == null) {
            response.put(MESSAGE, SPIEL_NICHT_GEFUNDEN);
            return response;
        }

        // 3. Spieler existiert?
        if (!game.getAllPlayers().containsKey(name)) {
            response.put(MESSAGE, SPIELER + name + EXISTIERT_NICHT);
            return response;
        }

        // 4. Zug durchführen
        if (!game.movePlayer(name, to, ticket)) {
            response.put(MESSAGE, UNGUELTIGER_ZUG);
            return response;
        }

        // 5. Gewinner prüfen
        if (game.getWinner() != GameState.Winner.NONE) {
            response.put(MESSAGE, getWinner(gameId));
            gameManager.removeGame(gameId);
            return response;
        }

        response.put(MESSAGE, SPIELER + name + BEWEGT_SICH_ZU + to + IN_SPIEL + gameId);
        return response;
    }

    @PostMapping("/moveDouble")
    public Map<String, String> moveDouble(
            @RequestParam String gameId,
            @RequestParam String name,
            @RequestParam int to,
            @RequestParam String gotTicket
    ) {

        Map<String, String> response = new HashMap<>();
        Ticket ticket1 = null;
        Ticket ticket2 = null;
        int toFirst = 0;
        String[] ticketsArray = gotTicket.split("\\+");

        if (ticketsArray.length == 3) {
            String ticketType1 = ticketsArray[0];
            String ticketType2 = ticketsArray[1];
            toFirst = Integer.parseInt(ticketsArray[2]); //change signature to include Firstto, dont put in Array
            try {
                ticket1 = Ticket.valueOf(ticketType1);
                ticket2 = Ticket.valueOf(ticketType2);
            } catch (IllegalArgumentException e) {
                response.put(MESSAGE, UNGUELTIGES_TICKET + gotTicket);
                return response;
            }
        } else {
            response.put(MESSAGE, "ERROR, falsches Ticket Format");

        }

        GameState game = gameManager.getGame(gameId);
        if (game == null) {
            response.put(MESSAGE, SPIEL_NICHT_GEFUNDEN);
            return response;
        }
        if (!game.getAllPlayers().containsKey(name)) {
            response.put(MESSAGE, SPIELER + name + EXISTIERT_NICHT);
            return response;
        }
        if (!game.moveMrXDouble(name,toFirst, to, ticket1, ticket2)) {
            response.put(MESSAGE, UNGUELTIGER_ZUG);
            return response;
        }
        if (game.getWinner() != GameState.Winner.NONE) {
            response.put(MESSAGE, getWinner(gameId));
            return response;
        }

        response.put(MESSAGE, SPIELER + name + BEWEGT_SICH_ZU + to + IN_SPIEL + gameId);
        return response;
    }

    @GetMapping("/allowedDoubleMoves")
    public ResponseEntity<?> getDoubleMoves(
            @RequestParam String gameId,
            @RequestParam String name
    ) {
        GameState game = gameManager.getGame(gameId);

        if (game == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(GAME_MIT_ID + gameId + NICHT_GEFUNDEN);
        }

        List<Map.Entry<Integer, String>> doubleMoves = game.getAllowedDoubleMoves(name).stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue())) // Bereits in String-Form
                .toList();
        logger.info("Allowed double moves: {}", doubleMoves);


        return ResponseEntity.ok(doubleMoves);

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