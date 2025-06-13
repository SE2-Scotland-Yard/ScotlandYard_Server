package at.aau.serg.scotlandyard.gamelogic;

import at.aau.serg.scotlandyard.bot.BotLogic;
import at.aau.serg.scotlandyard.dto.GameOverviewDTO;
import at.aau.serg.scotlandyard.gamelogic.player.*;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import at.aau.serg.scotlandyard.gamelogic.GameManager;
import org.slf4j.LoggerFactory;




import java.util.*;


public class RoundManager {
    @Getter
    private final List<Detective>detectives;
    @Getter
    private final MrX mrX;
    private int mrXPosition;
    @Getter
    private final List<Player>turnOrder;

    private Map<String,Integer> currentplayerPosition = new HashMap<>();

    private int currentPlayerTurn = 0; //index that indicates which player is next
    @Getter
    private int currentRound = 1;
    private static final int MAXROUNDS = 24;

    private final List<Integer> revealRounds = Arrays.asList(3,8,13,18,24); //for Mr.X
    private static final Logger logger = LoggerFactory.getLogger(RoundManager.class);
    @Setter
    private Player lastPlayerMoved;
    private boolean mrXwinByNoMoves = false;
    private GameState gameState;

    public RoundManager(List<Detective> detectives, MrX mrX) {
        this.detectives = detectives;
        this.mrX = mrX;
        this.turnOrder = new ArrayList<>();
        this.turnOrder.add(mrX); //Mr.X starts
        this.turnOrder.addAll(detectives);
    }

    public Player getCurrentPlayer() {
        return turnOrder.get(currentPlayerTurn);
    }


    public Map<String,Integer> getPlayerPositions(){

        for(Player p : turnOrder){
            if(p instanceof Detective){
                currentplayerPosition.put(p.getName(),p.getPosition());
            }
            else if(p instanceof MrX){
                if(revealRounds.contains(currentRound)){
                    mrXPosition=p.getPosition();

                }
                if(currentRound>=3) {
                    currentplayerPosition.put(p.getName(), mrXPosition);
                }
            }


        }
        logger.info("Current Player Positions: {}", currentplayerPosition);
        return currentplayerPosition;
    }
    public void nextRound() {
        for(Player p : turnOrder){
            if(p instanceof MrX){
                if(revealRounds.contains(currentRound)){
                    mrXPosition=p.getPosition();

                }
                if(currentRound>=3) {
                    currentplayerPosition.put(p.getName(), mrXPosition);
                }
            }


        }

        currentRound++;
    }
    public void nextTurn() {

        currentPlayerTurn++;
        logger.info("Current Turn: {}", currentRound);

        if (currentPlayerTurn >= turnOrder.size()) {
            currentPlayerTurn = 0;
            currentRound++;
        }

        Player currentPlayer = getCurrentPlayer();


        if (allDetectivesBlocked()) {
            mrXwinByNoMoves = true;
            logger.info("Alle Detectives sind blockiert – MrX gewinnt.");
        }

        // Bot automatisch bewegen
        if (currentPlayer.getName().startsWith("[BOT]")) {
            logger.info("🤖 Bot '{}' ist an der Reihe – führe automatischen Zug aus", currentPlayer.getName());

            new Thread(() -> {
                try {
                    Thread.sleep(3000); // Denkzeit
                } catch (InterruptedException ignored) {}

                BotLogic.executeBotMove(currentPlayer, gameState);
            }).start();
        }
    }

    private boolean allDetectivesBlocked() {
        for (Detective d : detectives) {
            List<Map.Entry<Integer, Ticket>> moves = gameState.getAllowedMoves(d.getName());
            if (moves != null && !moves.isEmpty()) {
                return false; // Mindestens ein Detective kann sich bewegen
            }
        }
        return true; //alle blockiert
    }




    public boolean mrXwinByNoMoves() {
        return mrXwinByNoMoves;
    }

    public boolean isMrXVisible() {
        return revealRounds.contains(currentRound);
    }

    public boolean isMrXCaptured(){
        for(Detective detective : detectives){
            if(detective.getPosition() == mrX.getPosition()){

                return true;
            }
        }

        return false;

    }

    public boolean isGameOver(){
        return currentRound > MAXROUNDS || isMrXCaptured();
    }

    public void addMrXTicket(Ticket ticket){
        mrX.addTicket(ticket);
    }

    public void replacePlayer(Player original, Player replacement) {
        // In turnOrder ersetzen
        for (int i = 0; i < turnOrder.size(); i++) {
            if (turnOrder.get(i).equals(original)) {
                turnOrder.set(i, replacement);

                // Wenn der aktuelle Spieler ersetzt wurde, Index beibehalten
                if (i == currentPlayerTurn) {
                    currentPlayerTurn = i;
                }
                break;
            }
        }

        // Falls es ein Detective ist, auch in der Liste der Detectives ersetzen
        if (original instanceof Detective && replacement instanceof Detective) {
            for (int i = 0; i < detectives.size(); i++) {
                if (detectives.get(i).equals(original)) {
                    detectives.set(i, (Detective) replacement);
                    break;
                }
            }
        }

        logger.info("🔁 Spieler '{}' wurde in der Runde ersetzt durch '{}'.", original.getName(), replacement.getName());
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }







}