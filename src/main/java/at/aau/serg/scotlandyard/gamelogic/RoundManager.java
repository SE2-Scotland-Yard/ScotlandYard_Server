package at.aau.serg.scotlandyard.gamelogic;

import at.aau.serg.scotlandyard.gamelogic.player.*;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;


public class RoundManager {
    @Getter
    private final List<Detective>detectives;
    private final MrX mrX;
    private int mrXPosition;
    private final List<Player>turnOrder;

    private Map<String,Integer> currentplayerPosition = new HashMap<>();

    private int currentPlayerTurn = 0; //index that indicates which player is next
    private int currentRound = 1;
    private static final int MAXROUNDS = 24;

    private final List<Integer> revealRounds = Arrays.asList(3,8,13,18,24); //for Mr.X
    private static final Logger logger = LoggerFactory.getLogger(GameState.class);

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

    public void nextTurn(){
        currentPlayerTurn++;
        logger.info("Current Turn: {}", currentRound);
        if(currentPlayerTurn >= turnOrder.size()){
            currentPlayerTurn = 0;
            currentRound++;
        }
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

    public int getCurrentRound() {
        return currentRound;
    }


    public MrX getMrX(){
        return mrX;
    }

    public List<Player> getTurnOrder() {
        return turnOrder;
    }


}
