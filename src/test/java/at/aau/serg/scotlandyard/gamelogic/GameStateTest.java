package at.aau.serg.scotlandyard.gamelogic;


import at.aau.serg.scotlandyard.bot.BotPlayer;
import at.aau.serg.scotlandyard.gamelogic.player.Detective;
import at.aau.serg.scotlandyard.gamelogic.player.MrX;
import at.aau.serg.scotlandyard.gamelogic.player.Player;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.PlayerTickets;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.lang.reflect.Field;
import java.util.*;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class GameStateTest {

    private GameState gameState;
    private MrX mrX;
    private Detective detective;

    @BeforeEach
    void setUp() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        GameManager mockGameManager = mock(GameManager.class);
        gameState = new GameState("testGame", messagingTemplate, mockGameManager);

        mrX = createMockMrX("MrX");
        detective = createMockDetective("Detective");

        gameState.addPlayer("MrX", mrX);
        gameState.addPlayer("Detective", detective);
        gameState.initRoundManager(List.of(detective), mrX);
    }

    private MrX createMockMrX(String name) {
        MrX mockMrX = mock(MrX.class);
        lenient().when(mockMrX.getName()).thenReturn(name);
        return mockMrX;
    }

    private Detective createMockDetective(String name) {
        Detective mockDetective = mock(Detective.class);
        lenient().when(mockDetective.getName()).thenReturn(name);
        return mockDetective;
    }

    private static PlayerTickets getDefaultTickets() {
        Map<Ticket, Integer> initialTickets = new EnumMap<>(Ticket.class);
        initialTickets.put(Ticket.TAXI, 4);
        initialTickets.put(Ticket.BUS, 3);
        initialTickets.put(Ticket.UNDERGROUND, 3);
        initialTickets.put(Ticket.BLACK, 5);
        initialTickets.put(Ticket.DOUBLE, 2);
        return new PlayerTickets(initialTickets);
    }

    @Test
    void testAddPlayer() {
        assertEquals(2, gameState.getAllPlayers().size());
        assertTrue(gameState.getAllPlayers().containsKey("MrX"));
        assertTrue(gameState.getAllPlayers().containsKey("Detective"));
    }

    @Test
    void testGetAllowedMoves() {
        when(mrX.getPosition()).thenReturn(1);
        when(mrX.getTickets()).thenReturn(getDefaultTickets());
        var allowedMoves = gameState.getAllowedMoves("MrX");
        assertFalse(allowedMoves.isEmpty());
    }

    @Test
    void testGetAllowedMovesPlayerIsNull() {
        var allowedMoves = gameState.getAllowedMoves("Unknown");
        assertTrue(allowedMoves.isEmpty());
    }

    @Test
    void testMovePlayerMrX() {
        when(mrX.isValidMove(anyInt(), any(), any())).thenReturn(true);
        when(mrX.getTickets()).thenReturn(getDefaultTickets());
        when(detective.getTickets()).thenReturn(getDefaultTickets());
        boolean result = gameState.movePlayer("MrX", 1, Ticket.TAXI);
        verify(mrX).move(eq(1), eq(Ticket.TAXI), any());
        assertTrue(result);
    }

    @Test
    void testMovePlayerMrXInvalid() {
        when(mrX.isValidMove(anyInt(), any(), any())).thenReturn(false);
        boolean result = gameState.movePlayer("MrX", 1, Ticket.TAXI);
        assertFalse(result);
        verify(mrX, never()).move(anyInt(), any(), any());
    }

    @Test
    void testMovePlayerDetective() {
        when(detective.isValidMove(anyInt(), any(), any())).thenReturn(true);
        when(detective.getTickets()).thenReturn(getDefaultTickets());
        when(mrX.getTickets()).thenReturn(getDefaultTickets());
        boolean result = gameState.movePlayer("Detective", 1, Ticket.TAXI);
        verify(detective).move(eq(1), eq(Ticket.TAXI), any());
        assertTrue(result);
    }

    @Test
    void testMovePlayerDetectivePositionTaken() {
        Detective detective2 = mock(Detective.class);
        gameState.addPlayer("Detective2", detective2);
        when(detective2.isValidMove(anyInt(), any(), any())).thenReturn(false);
        when(detective.getPosition()).thenReturn(1);

        boolean result = gameState.movePlayer("Detective2", 1, Ticket.TAXI);
        assertFalse(result);
        verify(detective2, never()).move(anyInt(), any(), any());
    }

    @Test
    void testMovePlayerDetectiveInvalid() {
        when(detective.isValidMove(anyInt(), any(), any())).thenReturn(false);
        boolean result = gameState.movePlayer("Detective", 1, Ticket.TAXI);
        assertFalse(result);
        verify(detective, never()).move(anyInt(), any(), any());
    }

    @Test
    void testMoveMrXDouble() {
        MrX realMrX = new MrX("MrX");
        realMrX.getTickets().addTicket(Ticket.TAXI);
        realMrX.getTickets().addTicket(Ticket.DOUBLE);
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        gameState = new GameState("testGame", messagingTemplate);
        gameState.addPlayer("MrX", realMrX);
        gameState.initRoundManager(List.of(), realMrX);
        boolean moved = gameState.moveMrXDouble("MrX", 2, 3, Ticket.TAXI, Ticket.TAXI);
        assertTrue(moved);
        assertEquals(2, gameState.getMrXMoveHistory().size());
    }

    @Test
    void testMoveMrXDoubleInvalid() {
        boolean result = gameState.moveMrXDouble("Detective", 2, 3, Ticket.TAXI, Ticket.TAXI);
        assertFalse(result);
    }

    @Test
    void testGetMrXMoveHistory() {
        when(mrX.isValidMove(anyInt(), any(), any())).thenReturn(true);
        when(mrX.getTickets()).thenReturn(getDefaultTickets());
        when(detective.isValidMove(anyInt(), any(), any())).thenReturn(true);
        when(detective.getTickets()).thenReturn(getDefaultTickets());

        gameState.movePlayer("MrX", 1, Ticket.TAXI);
        gameState.movePlayer("Detective", 2, Ticket.TAXI);
        gameState.movePlayer("MrX", 3, Ticket.BUS);
        gameState.movePlayer("Detective", 4, Ticket.TAXI);

        var history = gameState.getMrXMoveHistory();
        assertEquals(2, history.size());
    }

    @Test
    void testGetMrXMoveHistoryNullValue() throws Exception {
        Field field = GameState.class.getDeclaredField("mrXHistory");
        field.setAccessible(true);
        Map<Integer, MrXMove> mockMap = new HashMap<>();
        mockMap.put(1, null);
        field.set(gameState, mockMap);

        var result = gameState.getMrXMoveHistory();
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void testGetWinnerNone() throws Exception {
        RoundManager mockRM = mock(RoundManager.class);
        when(mockRM.isGameOver()).thenReturn(false);

        Field field = GameState.class.getDeclaredField("roundManager");
        field.setAccessible(true);
        field.set(gameState, mockRM);

        assertEquals(GameState.Winner.NONE, gameState.getWinner());
    }

    @Test
    void testGetWinnerMrX() throws Exception {
        RoundManager mockRM = mock(RoundManager.class);
        when(mockRM.isGameOver()).thenReturn(true);
        when(mockRM.isMrXCaptured()).thenReturn(false);

        Field field = GameState.class.getDeclaredField("roundManager");
        field.setAccessible(true);
        field.set(gameState, mockRM);

        assertEquals(GameState.Winner.MR_X, gameState.getWinner());
    }

    @Test
    void testRevealRounds() {
        assertEquals(List.of(3, 8, 13, 18, 24), gameState.getRevealRounds());
    }

    @Test
    void testBoardNotNull() {
        assertNotNull(gameState.getBoard());
    }

    @Test
    void testIsPositionOccupied() {
        when(detective.getPosition()).thenReturn(10);
        assertTrue(gameState.isPositionOccupied(10));
        assertFalse(gameState.isPositionOccupied(99));
    }

    @Test
    void testGetMrXPositionKnown() {
        when(mrX.getPosition()).thenReturn(77);
        assertEquals(77, gameState.getMrXPosition("MrX"));
    }

    @Test
    void testGetMrXPositionUnknown() {
        assertEquals(0, gameState.getMrXPosition("Ghost"));
    }

    @Test
    void testGetMrXPosition() {
        lenient().when(mrX.getPosition()).thenReturn(1);
        int pos = gameState.getMrXPosition("MrX");
        assertEquals(1, pos);
    }

    @Test
    void testGetMrXPositionNull() {
        int pos = gameState.getMrXPosition("Nothing");
        assertEquals(0, pos);
    }

    @Test
    void testGetAllowedDoubleMovesNotAllowed() {
        lenient().when(detective.getTickets()).thenReturn(getDefaultTickets());
        assertEquals(0, gameState.getAllowedDoubleMoves("Detective").size());
    }

    @Test
    void testGetAllowedDoubleMovesPos1Taxi() {
        lenient().when(mrX.getPosition()).thenReturn(1);

        Map<Ticket, Integer> initialTickets = new EnumMap<>(Ticket.class);
        initialTickets.put(Ticket.TAXI, 4);
        initialTickets.put(Ticket.DOUBLE, 1);

        lenient().when(mrX.getTickets()).thenReturn(new PlayerTickets(initialTickets));
        assertEquals(4, gameState.getAllowedDoubleMoves("MrX").size());
    }

    @Test
    void testGetAllowedDoubleMovesPos2Taxi() {
        lenient().when(mrX.getPosition()).thenReturn(2);

        Map<Ticket, Integer> initialTickets = new EnumMap<>(Ticket.class);
        initialTickets.put(Ticket.TAXI, 4);
        initialTickets.put(Ticket.DOUBLE, 1);

        lenient().when(mrX.getTickets()).thenReturn(new PlayerTickets(initialTickets));
        assertEquals(5, gameState.getAllowedDoubleMoves("MrX").size());
    }

    @Test
    void testGetAllowedDoubleMovesMissing2ndTicket() {
        lenient().when(mrX.getPosition()).thenReturn(1);
        Map<Ticket, Integer> initialTickets = new EnumMap<>(Ticket.class);
        initialTickets.put(Ticket.TAXI, 1);
        initialTickets.put(Ticket.DOUBLE, 1);

        lenient().when(mrX.getTickets()).thenReturn(new PlayerTickets(initialTickets));
        assertEquals(0, gameState.getAllowedDoubleMoves("MrX").size());
    }

    @Test
    void testGetAllowedDoubleMovesMissingDoubleTicket() {
        Map<Ticket, Integer> initialTickets = new EnumMap<>(Ticket.class);
        initialTickets.put(Ticket.TAXI, 4);
        initialTickets.put(Ticket.DOUBLE, 0); // kein Double Ticket

        lenient().when(mrX.getTickets()).thenReturn(new PlayerTickets(initialTickets));
        assertEquals(0, gameState.getAllowedDoubleMoves("MrX").size());
    }

    @Test
    void testGetCurrentPlayerNameNull() throws Exception {
        Field nameField = GameState.class.getDeclaredField("roundManager");
        nameField.setAccessible(true);
        nameField.set(gameState, null);

        assertNull(gameState.getCurrentPlayerName());
    }

    @Test
    void testGetCurrentPlayerNameNoPlayer() throws Exception {
        RoundManager roundManager = mock(RoundManager.class);
        lenient().when(roundManager.getCurrentPlayer()).thenReturn(null);

        Field nameField = GameState.class.getDeclaredField("roundManager");
        nameField.setAccessible(true);
        nameField.set(gameState, roundManager);

        assertNull(gameState.getCurrentPlayerName());
    }

    @Test
    void testUpdateLastActivity_AddsNewTimestamp() {
        String playerId = "player1";
        long beforeUpdate = System.currentTimeMillis();

        gameState.updateLastActivity(playerId);
        Map<String, Long> result = gameState.getLastActivityMap();

        assertTrue(result.containsKey(playerId));
        assertTrue(result.get(playerId) >= beforeUpdate);
    }

    @Test
    void testUpdateLastActivity_UpdatesExistingTimestamp() {
        String playerId = "player1";
        gameState.updateLastActivity(playerId);
        long firstTimestamp = gameState.getLastActivityMap().get(playerId);

        await().atMost(10, SECONDS) // Timeout nach 100ms
                .until(() -> {
                    gameState.updateLastActivity(playerId);
                    return gameState.getLastActivityMap().get(playerId) > firstTimestamp;
                });
        gameState.updateLastActivity(playerId);
        long secondTimestamp = gameState.getLastActivityMap().get(playerId);

        assertTrue(secondTimestamp > firstTimestamp);
    }

    @Test
    void testGetLastActivityMap_ReturnsCopyNotOriginal() {
        String playerId = "player1";
        gameState.updateLastActivity(playerId);

        Map<String, Long> firstCopy = gameState.getLastActivityMap();
        gameState.updateLastActivity("player2");
        Map<String, Long> secondCopy = gameState.getLastActivityMap();

        assertEquals(1, firstCopy.size());
        assertEquals(2, secondCopy.size());
    }


    @Test
    void testReplaceWithBot_MrX_RemovesGame() {
        String playerName = "MrX";
        GameManager mockGameManager = mock(GameManager.class);
        gameState = new GameState("testGame", mock(SimpMessagingTemplate.class), mockGameManager);
        gameState.addPlayer(playerName, mrX);

        Player result = gameState.replaceWithBot(playerName);

        assertNull(result);
        verify(mockGameManager).removeGame("testGame");
    }

    @Test
    void testReplaceWithBot_NonExistentPlayer() {
        Player result = gameState.replaceWithBot("GhostPlayer");
        assertNull(result);
    }
    @Test
    void testMoveBlackTicket_Success() {

        when(mrX.isValidMove(eq(42), eq(Ticket.BLACK), any())).thenReturn(true);
        doNothing().when(mrX).moveBlack(eq(42), eq(Ticket.BLACK), any());


        when(mrX.getTickets()).thenReturn(getDefaultTickets());


        lenient().when(detective.getTickets()).thenReturn(getDefaultTickets());
        lenient().when(detective.isValidMove(anyInt(), any(), any())).thenReturn(true);


        boolean result = gameState.moveBlackTicket("MrX", 42, Ticket.BLACK);


        assertTrue(result);
        verify(mrX).moveBlack(eq(42), eq(Ticket.BLACK), any());
    }

    @Test
    void testGetShortestMoveTo_WithValidPath() {
        when(mrX.getPosition()).thenReturn(3); // Zielposition
        when(detective.getPosition()).thenReturn(1); // Startposition
        when(detective.getTickets()).thenReturn(getDefaultTickets());

        var result = gameState.getShortestMoveTo("Detective");
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(result.get(0).getKey() > 0);
    }

    @Test
    void testGetShortestMoveTo_PlayerCompletelyTrapped() {
        when(detective.getPosition()).thenReturn(1);

        Map<Ticket, Integer> noTickets = new EnumMap<>(Ticket.class);
        for (Ticket t : Ticket.values()) {
            noTickets.put(t, 0);
        }
        when(detective.getTickets()).thenReturn(new PlayerTickets(noTickets));

        var result = gameState.getShortestMoveTo("Detective");

        //keine Moves möglich
        assertTrue(result.isEmpty());
    }




    @Test
    void testGetShortestMoveTo_NoPathUsesFallback() {
        when(mrX.getPosition()).thenReturn(99); // Kein Pfad dorthin
        when(detective.getPosition()).thenReturn(1);
        when(detective.getTickets()).thenReturn(getDefaultTickets());

        var result = gameState.getShortestMoveTo("Detective");
        assertFalse(result.isEmpty());
    }

    @Test
    void testOnlyBotsLeft_ReturnsTrue() {
        gameState.getAllPlayers().clear();

        BotPlayer bot1 = mock(BotPlayer.class);
        gameState.addPlayer("Bot1", bot1);

        BotPlayer bot2 = mock(BotPlayer.class);
        gameState.addPlayer("Bot2", bot2);

        assertTrue(gameState.onlyBotsLeft());
    }


    @Test
    void testOnlyBotsLeft_ReturnsFalse() {
        assertFalse(gameState.onlyBotsLeft());
    }








}
