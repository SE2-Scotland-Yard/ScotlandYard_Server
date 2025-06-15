package at.aau.serg.scotlandyard.controller;

import at.aau.serg.scotlandyard.dto.GameOverviewDTO;
import at.aau.serg.scotlandyard.gamelogic.GameManager;
import at.aau.serg.scotlandyard.gamelogic.GameState;
import at.aau.serg.scotlandyard.gamelogic.player.Player;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.Map.Entry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameControllerTest {

    @Mock
    private GameManager gameManager;

    @Mock
    private GameState gameState;

    @Mock
    private Player player;

    @InjectMocks
    private GameController gameController;

    private final String gameId = "testGame";
    private final String playerName = "testPlayer";
    private final int position = 42;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getMoves_WhenGameNotFound_ReturnsNotFound() {
        when(gameManager.getGame(gameId)).thenReturn(null);

        ResponseEntity<?> response = gameController.getMoves(gameId, playerName);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Game mit ID '" + gameId + "' nicht gefunden.", response.getBody());
    }

    @Test
    void getMoves_WhenGameExists_ReturnsAllowedMoves() {
        Map<Integer, Ticket> moves = new HashMap<>();
        moves.put(1, Ticket.TAXI);
        moves.put(2, Ticket.BUS);
        moves.put(3, Ticket.UNDERGROUND);
        List<Entry<Integer, Ticket>> expectedMoves = new ArrayList<>(moves.entrySet());

        when(gameManager.getGame(gameId)).thenReturn(gameState);
        when(gameState.getAllowedMoves(playerName)).thenReturn(expectedMoves);

        ResponseEntity<?> response = gameController.getMoves(gameId, playerName);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);

        @SuppressWarnings("unchecked")
        List<Entry<Integer, String>> responseBody = (List<Entry<Integer, String>>) response.getBody();
        assertEquals(3, responseBody.size());
        assertEquals("TAXI", responseBody.get(0).getValue());
        assertEquals("BUS", responseBody.get(1).getValue());
        assertEquals("UNDERGROUND", responseBody.get(2).getValue());
    }

    @Test
    void move_WhenInvalidTicket_ReturnsErrorMessage() {
        Map<String, String> result = gameController.move(gameId, playerName, position, "INVALID_TICKET");

        assertEquals("Ungültiges Ticket: INVALID_TICKET", result.get("message"));
    }

    @Test
    void move_WhenGameNotFound_ReturnsGameNotFound() {
        when(gameManager.getGame(gameId)).thenReturn(null);

        Map<String, String> result = gameController.move(gameId, playerName, position, "TAXI");

        assertEquals("Spiel nicht gefunden!", result.get("message"));
    }

    @Test
    void move_WhenPlayerNotFound_ReturnsErrorMessage() {
        when(gameManager.getGame(gameId)).thenReturn(gameState);

        Map<String, Player> players = new HashMap<>();
        // Leere Map = Spieler nicht vorhanden
        when(gameState.getAllPlayers()).thenReturn(players);

        Map<String, String> result = gameController.move(gameId, playerName, position, "TAXI");

        assertEquals("Spieler " + playerName + " existiert nicht!", result.get("message"));
    }

    @Test
    void move_WhenInvalidMove_ReturnsErrorMessage() {
        Map<String, Player> players = new HashMap<>();
        players.put(playerName, player);

        when(gameManager.getGame(gameId)).thenReturn(gameState);
        when(gameState.getAllPlayers()).thenReturn(players);
        when(gameState.movePlayer(playerName, position, Ticket.TAXI)).thenReturn(false);

        Map<String, String> result = gameController.move(gameId, playerName, position, "TAXI");

        assertEquals("Ungültiger Zug!", result.get("message"));
    }

    @Test
    void move_WhenValidMove_ReturnsSuccessMessage() {
        Map<String, Player> players = new HashMap<>();
        players.put(playerName, player);

        when(gameManager.getGame(gameId)).thenReturn(gameState);
        when(gameState.getAllPlayers()).thenReturn(players);
        when(gameState.movePlayer(playerName, position, Ticket.TAXI)).thenReturn(true);
        when(gameState.getWinner()).thenReturn(GameState.Winner.NONE);

        Map<String, String> result = gameController.move(gameId, playerName, position, "TAXI");

        assertEquals("Spieler " + playerName + " bewegt sich zu " + position + " in Spiel " + gameId, result.get("message"));
    }

    @Test
    void move_WhenMoveWinsGame_ReturnsWinnerMessage() {
        Map<String, Player> players = new HashMap<>();
        players.put(playerName, player);

        when(gameManager.getGame(gameId)).thenReturn(gameState);
        when(gameState.getAllPlayers()).thenReturn(players);
        when(gameState.movePlayer(playerName, position, Ticket.TAXI)).thenReturn(true);
        when(gameState.getWinner()).thenReturn(GameState.Winner.MR_X);

        Map<String, String> result = gameController.move(gameId, playerName, position, "TAXI");

        assertEquals("Mr.X hat gewonnen!", result.get("message"));
    }

    @Test
    void moveDouble_WhenGameNotFound_ReturnsGameNotFound() {
        when(gameManager.getGame(gameId)).thenReturn(null);

        Map<String, String> response = gameController.moveDouble(
                gameId, playerName, 42, "TAXI+BUS");

        assertEquals("Spiel nicht gefunden!", response.get("message"));
    }

    @Test
    void moveDouble_WhenInvalidMove_ReturnsErrorMessage() {
        when(gameManager.getGame(gameId)).thenReturn(gameState);

        Map<String, Player> players = new HashMap<>();
        players.put(playerName, null); // Oder mock(Player.class)
        when(gameState.getAllPlayers()).thenReturn(players);

        when(gameState.moveMrXDouble(playerName, 41,42,Ticket.TAXI, Ticket.BUS)).thenReturn(false);

        Map<String, String> response = gameController.moveDouble(
                gameId, playerName, 42, "TAXI+BUS");

        assertEquals("Ungültiger Zug!", response.get("message"));
    }

    @Test
    void moveDouble_WhenValidMove_ReturnsSuccessMessage() {
        when(gameManager.getGame(gameId)).thenReturn(gameState);

        Player mockPlayer = mock(Player.class);
        Map<String, Player> players = new HashMap<>();
        players.put(playerName, mockPlayer);
        when(gameState.getAllPlayers()).thenReturn(players);

        when(gameState.moveMrXDouble(playerName, 17,42, Ticket.TAXI, Ticket.BUS)).thenReturn(true);
        when(gameState.getWinner()).thenReturn(GameState.Winner.NONE);

        Map<String, String> response = gameController.moveDouble(
                gameId, playerName, 42, "TAXI+BUS+17"); //See method

        assertEquals("Spieler " + playerName + " bewegt sich zu 42 in Spiel " + gameId, response.get("message"));
    }



    @Test
    void getMrXHistory_WhenGameNotFound_ReturnsGameNotFound() {
        when(gameManager.getGame(gameId)).thenReturn(null);

        List<String> result = gameController.getMrXHistory(gameId);

        assertEquals(List.of(GameController.GAME_NOT_FOUND), result);
    }

    @Test
    void getMrXHistory_WhenGameExists_ReturnsHistory() {
        List<String> expectedHistory = List.of("Move 1", "Move 2");
        when(gameManager.getGame(gameId)).thenReturn(gameState);
        when(gameState.getMrXMoveHistory()).thenReturn(expectedHistory);

        List<String> result = gameController.getMrXHistory(gameId);

        assertEquals(expectedHistory, result);
    }

    @Test
    void getAllGames_ReturnsListOfGameOverviewDTOs() {
        Set<String> gameIds = new HashSet<>();
        gameIds.add("game1");
        gameIds.add("game2");
        when(gameManager.getAllGameIds()).thenReturn(gameIds);

        GameState gameState1 = mock(GameState.class);
        GameState gameState2 = mock(GameState.class);

        when(gameManager.getGame("game1")).thenReturn(gameState1);
        when(gameManager.getGame("game2")).thenReturn(gameState2);
        when(gameState1.getAllPlayers()).thenReturn(new HashMap<>());
        when(gameState2.getAllPlayers()).thenReturn(new HashMap<>());

        List<GameOverviewDTO> result = gameController.getAllGames();

        assertEquals(2, result.size());

        List<String> resultIds = new ArrayList<>();
        resultIds.add(result.get(0).getGameId());
        resultIds.add(result.get(1).getGameId());
        Collections.sort(resultIds);
        assertEquals(Arrays.asList("game1", "game2"), resultIds);
    }

    @Test
    void getWinner_WhenGameNotFound_ReturnsGameNotFound() {
        when(gameManager.getGame(gameId)).thenReturn(null);

        String result = gameController.getWinner(gameId);

        assertEquals(GameController.GAME_NOT_FOUND, result);
    }

    @Test
    void getWinner_WhenMrXWins_ReturnsMrXWonMessage() {
        when(gameManager.getGame(gameId)).thenReturn(gameState);
        when(gameState.getWinner()).thenReturn(GameState.Winner.MR_X);

        String result = gameController.getWinner(gameId);

        assertEquals("Mr.X hat gewonnen!", result);
    }

    @Test
    void getWinner_WhenDetectivesWin_ReturnsDetectivesWonMessage() {
        when(gameManager.getGame(gameId)).thenReturn(gameState);
        when(gameState.getWinner()).thenReturn(GameState.Winner.DETECTIVE);

        String result = gameController.getWinner(gameId);

        assertEquals("Detektive haben gewonnen!", result);
    }

    @Test
    void getWinner_WhenGameStillRunning_ReturnsGameStillRunningMessage() {
        when(gameManager.getGame(gameId)).thenReturn(gameState);
        when(gameState.getWinner()).thenReturn(GameState.Winner.NONE);

        String result = gameController.getWinner(gameId);

        assertEquals("Spiel läuft noch.", result);
    }

    @Test
    void blackTicket_WhenInvalidTicket_ReturnsInvalidTicketMessage() {
        // Kein Mocking nötig, da WertOf scheitert
        Map<String, String> response = gameController.blackTicket(gameId, playerName, position, "NOT_A_TICKET");
        assertEquals("Ungültiges Ticket: NOT_A_TICKET", response.get("message"));
    }

    @Test
    void blackTicket_WhenGameNotFound_ReturnsGameNotFoundMessage() {
        when(gameManager.getGame(gameId)).thenReturn(null);
        Map<String, String> response = gameController.blackTicket(gameId, playerName, position, "TAXI");
        assertEquals("Spiel nicht gefunden!", response.get("message"));
    }

    @Test
    void blackTicket_WhenPlayerNotInGame_ReturnsPlayerNotFoundMessage() {
        when(gameManager.getGame(gameId)).thenReturn(gameState);
        when(gameState.getAllPlayers()).thenReturn(Collections.emptyMap());
        Map<String, String> response = gameController.blackTicket(gameId, playerName, position, "TAXI");
        assertEquals("Spieler " + playerName + " existiert nicht!", response.get("message"));
    }

    @Test
    void blackTicket_WhenMoveInvalid_ReturnsInvalidMoveMessage() {
        when(gameManager.getGame(gameId)).thenReturn(gameState);
        when(gameState.getAllPlayers()).thenReturn(Map.of(playerName, player));
        when(gameState.moveBlackTicket(playerName, position, Ticket.TAXI)).thenReturn(false);

        Map<String, String> response = gameController.blackTicket(gameId, playerName, position, "TAXI");
        assertEquals("Ungültiger Zug!", response.get("message"));
    }

    @Test
    void blackTicket_WhenMoveWinsGame_ReturnsWinnerMessage() {
        when(gameManager.getGame(gameId)).thenReturn(gameState);
        when(gameState.getAllPlayers()).thenReturn(Map.of(playerName, player));
        when(gameState.moveBlackTicket(playerName, position, Ticket.TAXI)).thenReturn(true);
        when(gameState.getWinner()).thenReturn(GameState.Winner.MR_X);

        Map<String, String> response = gameController.blackTicket(gameId, playerName, position, "TAXI");
        assertEquals("Mr.X hat gewonnen!", response.get("message"));
    }

    @Test
    void blackTicket_WhenMoveSucceedsAndNoWinner_ReturnsSuccessMessage() {
        when(gameManager.getGame(gameId)).thenReturn(gameState);
        when(gameState.getAllPlayers()).thenReturn(Map.of(playerName, player));
        when(gameState.moveBlackTicket(playerName, position, Ticket.TAXI)).thenReturn(true);
        when(gameState.getWinner()).thenReturn(GameState.Winner.NONE);

        Map<String, String> response = gameController.blackTicket(gameId, playerName, position, "TAXI");
        assertEquals(
                "Spieler " + playerName + " bewegt sich zu " + position + " in Spiel " + gameId,
                response.get("message")
        );
    }
}