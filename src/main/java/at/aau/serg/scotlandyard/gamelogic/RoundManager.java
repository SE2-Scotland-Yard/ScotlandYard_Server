package at.aau.serg.scotlandyard.gamelogic;

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
    private static final Logger logger = LoggerFactory.getLogger(GameState.class);
    @Setter
    private Player lastPlayerMoved;
    private boolean mrXwinByNoMoves = false;

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
    public void nextTurn(){
        Player currentPlayer = getCurrentPlayer();


        currentPlayerTurn++;
        logger.info("Current Turn: {}", currentRound);
        if(currentPlayerTurn >= turnOrder.size()){
            currentPlayerTurn = 0;
            currentRound++;
        }
        if (currentPlayer==lastPlayerMoved) {
                    mrXwinByNoMoves = true;
        }

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
    public void gameOver(String gameId) {
        logger.info("Game {} is over",gameId);
        gameManager.removeGame(gameId);

    }

    public void addMrXTicket(Ticket ticket){
        mrX.addTicket(ticket);
    }


}