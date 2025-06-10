package at.aau.serg.scotlandyard.gamelogic;

import at.aau.serg.scotlandyard.gamelogic.player.Detective;
import at.aau.serg.scotlandyard.gamelogic.player.MrX;
import at.aau.serg.scotlandyard.gamelogic.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Map;


class RoundManagerTest {

    @Mock
    private Detective detective1;

    @Mock
    private Detective detective2;

    @Mock
    private MrX mrX;
    @Mock
    private GameState gameState;


    private RoundManager roundManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(detective1.getName()).thenReturn("Detective1");
        when(detective2.getName()).thenReturn("Detective2");
        when(mrX.getName()).thenReturn("MrX");

        //Setup positions
        when(detective1.getPosition()).thenReturn(4);
        when(detective2.getPosition()).thenReturn(0);
        when(mrX.getPosition()).thenReturn(5);

        //Setup Roundmanager
        ArrayList<Detective> detectives = new ArrayList<>();
        detectives.add(detective1);
        detectives.add(detective2);

        roundManager = new RoundManager(detectives,mrX);
        roundManager.setGameState(gameState);

    }

    @Test
    void testGetPlayerPositions() {
        when(detective1.getPosition()).thenReturn(10);
        when(detective2.getPosition()).thenReturn(20);


        Map<String, Integer> positions = roundManager.getPlayerPositions();

        assertEquals(10, positions.get("Detective1"));
        assertEquals(20, positions.get("Detective2"));
        assertNull(positions.get("MrX"));

    }

    @Test
    void testGetCurrentPlayer(){
        Player player = roundManager.getCurrentPlayer();
        assertEquals(mrX, player);
        roundManager.nextTurn();
        player = roundManager.getCurrentPlayer();
        assertEquals(detective1, player);
    }

    @Test
    void testCurrentRoundIncrement() {
        assertEquals(1, roundManager.getCurrentRound());

        completeFullTurnCycle();
        assertEquals(2, roundManager.getCurrentRound());
    }

    @Test
    void testNextTurn(){
        roundManager.nextTurn();
        assertEquals(detective1, roundManager.getCurrentPlayer());
        roundManager.nextTurn();
        assertEquals(detective2, roundManager.getCurrentPlayer());
        roundManager.nextTurn();
        assertEquals(mrX, roundManager.getCurrentPlayer());
    }

    @Test
    void testIsMrXVisible(){
        roundManager = new RoundManager(new ArrayList<>(), mrX);
        roundManager.nextTurn();
        roundManager.nextTurn();
        assertTrue(roundManager.isMrXVisible());

        roundManager = new RoundManager(new ArrayList<>(), mrX);
        roundManager.nextTurn();
        roundManager.nextTurn();
        roundManager.nextTurn();
        assertFalse(roundManager.isMrXVisible());
    }

    @Test
    void testMrXPositionTracking() {
        when(mrX.getPosition()).thenReturn(100);
        advanceToRound(3);

        Map<String, Integer> positions = roundManager.getPlayerPositions();
        assertEquals(100, positions.get("MrX"));

        when(mrX.getPosition()).thenReturn(150);
        advanceToRound(8);
        positions = roundManager.getPlayerPositions();
        assertEquals(150, positions.get("MrX"));
    }

    @Test
    void testIsMrXCaptured(){
        assertFalse(roundManager.isMrXCaptured());
        when(detective1.getPosition()).thenReturn(5);
        assertTrue(roundManager.isMrXCaptured());
    }

    @Test
    void testIsGameOverMrXCaptured(){
        assertFalse(roundManager.isGameOver());
        when(detective1.getPosition()).thenReturn(5);
        assertTrue(roundManager.isGameOver());
    }

    @Test
    void testIsGameOverMaxRounds(){
        assertFalse(roundManager.isGameOver());

        int totalTurns = 3 * 25; // MaxRounds = 24 (smaller than 25), 3 total Players

        for (int i = 0; i < totalTurns; i++) {
            roundManager.nextTurn();
        }

        assertTrue(roundManager.isGameOver());
    }

    @Test
    void testGetters() {
        assertEquals(2, roundManager.getDetectives().size());
        assertEquals(mrX, roundManager.getMrX());
        assertEquals(3, roundManager.getTurnOrder().size());
    }

    private void advanceToRound(int targetRound) {
        while (roundManager.getCurrentRound() < targetRound) {
            completeFullTurnCycle();
        }
    }

    private void completeFullTurnCycle() {
        for (int i = 0; i < roundManager.getTurnOrder().size(); i++) {
            roundManager.nextTurn();
        }
    }
}
