package at.aau.serg.scotlandyard.gamelogic;


import at.aau.serg.scotlandyard.dto.GameMapper;

import at.aau.serg.scotlandyard.gamelogic.board.Board;
import at.aau.serg.scotlandyard.gamelogic.board.Edge;
import at.aau.serg.scotlandyard.gamelogic.player.Detective;
import at.aau.serg.scotlandyard.gamelogic.player.MrX;
import at.aau.serg.scotlandyard.gamelogic.player.Player;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;


public class GameState {
    private final SimpMessagingTemplate messaging;
    private final String gameId;
    private final Board board;
    private final Map<String, Player> players = new HashMap<>();
    @Getter
    private RoundManager roundManager;
    private int currentRound = 1;
    private final List<Integer> revealRounds = List.of(3, 8, 13, 18, 24); // Sichtbarkeitsrunden
    private final Map<Integer, MrXMove> mrXHistory = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(GameState.class);

    Map<String, Integer> playerPositions = new HashMap<>();




    public GameState(String gameId, SimpMessagingTemplate messaging) {
        this.board = new Board();
        this.gameId = gameId;
        this.messaging = messaging;
    }

    public void initRoundManager(List<Detective>detectives, MrX mrX){ //nicht ideal
        this.roundManager = new RoundManager(detectives, mrX);
    }

    public void cantMove(String gameId) {
        roundManager.nextTurn();
        messaging.convertAndSend("/topic/game/" + gameId,
                GameMapper.mapToGameUpdate(
                        gameId,
                        playerPositions,
                        getCurrentPlayerName(),
                        getWinner().toString(),
                        null
                )
        );
    }
    public void addPlayer(String name, Player player) {
        players.put(name, player);
    }

    public List<Map.Entry<Integer, Ticket>> getAllowedMoves(String name) {
        Player p = players.get(name);
        if (p == null) {
            return List.of();
        }

        List<Edge> connections = board.getConnectionsFrom(p.getPosition());

        return connections.stream()
                .filter(edge -> p.getTickets().hasTicket(edge.getTicket()))
                .filter(edge -> !isPositionOccupied(edge.getTo()))
                .map(edge -> Map.entry(edge.getTo(), edge.getTicket()))
                .toList();
    }

    public Integer getMrXPosition(String name) {
        int position=0;
        Player p = players.get(name);
        if (p == null) {
            return 0;
        }

        position = p.getPosition();
        return position;
    }

    public boolean moveBlackTicket(String name, int to, Ticket ticket) {
        Player p = players.get(name);
        if (p instanceof MrX mrX && mrX.isValidMove(to, ticket, board)) {
            mrX.moveBlack(to, ticket, board);
            int roundBefore = roundManager.getCurrentRound();
            mrXHistory.put(roundBefore, new MrXMove(to, Ticket.BLACK));
            roundManager.nextTurn();


            playerPositions = roundManager.getPlayerPositions();

            String nextPlayer = getCurrentPlayerName();
            logger.info("➡️ currentRound: {}, nextPlayer: {}", currentRound, nextPlayer);
            messaging.convertAndSend("/topic/game/" + gameId,
                    GameMapper.mapToGameUpdate(
                            gameId,
                            playerPositions,
                            getCurrentPlayerName(),
                            getWinner().toString(),
                            Ticket.BLACK
                    )
            );
            return true;
        }
        return false;
    }

    public boolean movePlayer(String name, int to, Ticket ticket) {
        Player p = players.get(name);

        if (p instanceof MrX mrX && mrX.isValidMove(to, ticket, board)) {
            mrX.move(to, ticket, board);
            int roundBefore = roundManager.getCurrentRound();
            mrXHistory.put(roundBefore, new MrXMove(to, ticket));
            roundManager.nextTurn();



            playerPositions = roundManager.getPlayerPositions();
            String winner = getWinner().toString();
            String nextPlayer = getCurrentPlayerName();
            logger.info("➡️ currentRound: {}, nextPlayer: {}, WINNER: {}", currentRound, nextPlayer,getWinner().toString());
            messaging.convertAndSend("/topic/game/" + gameId,
                    GameMapper.mapToGameUpdate(
                            gameId,
                            playerPositions,
                            getCurrentPlayerName(),
                            winner,
                            ticket



                    )
            );
            if(winner == "DETECTIVE"||winner == "MRX") {
                roundManager.gameOver(gameId);
            }
            return true;
        }
        if (p != null && p.isValidMove(to, ticket, board)) {
            p.move(to, ticket, board);
            playerPositions = roundManager.getPlayerPositions();
            roundManager.nextTurn();


            roundManager.addMrXTicket(ticket);

            String nextPlayer = getCurrentPlayerName();
            String winner = getWinner().toString();
            logger.info("➡️ currentRound: {}, nextPlayer: {}", currentRound, nextPlayer);
            logger.info("WINNER: {}", getWinner().toString());
            messaging.convertAndSend("/topic/game/" + gameId,
                    GameMapper.mapToGameUpdate(
                            gameId,
                            playerPositions,
                            getCurrentPlayerName(),
                            winner,
                            ticket

                    )
            );
            if(winner == "DETECTIVE"||winner == "MRX") {
                roundManager.gameOver(gameId);
            }
            return true;
        }

        if (p instanceof Detective) {
            for (Player other : players.values()) {
                if (other != p && other instanceof Detective && other.getPosition() == to) {
                    return false; // Ziel ist von anderem Detective besetzt
                }
            }
        }

        return false;
    }

    public Board getBoard() {
        return board;
    }
    public List<Integer> getRevealRounds(){return List.copyOf(revealRounds);}

    public Map<String, Player> getAllPlayers() {
        return players;
    }

    public String getCurrentPlayerName() {
        if (roundManager == null || roundManager.getCurrentPlayer() == null) {
            return null;
        }
        return roundManager.getCurrentPlayer().getName();
    }



    //Winning Condition
    public enum Winner{ MR_X, DETECTIVE, NONE}

    public Winner getWinner(){
        if(!roundManager.isGameOver()){
            return Winner.NONE; //Game still running
        }
        if(roundManager.isMrXCaptured()){
            return Winner.DETECTIVE;
        }
        return Winner.MR_X;
    }

    public boolean isPositionOccupied(int position) {
        Map<String, Player> detectives = new HashMap<>();
        for (Player p : players.values()) {
            if(p instanceof Detective){
                detectives.put(p.getName(), p);
            }
        }
        return detectives.values().stream()
                .anyMatch(p -> p.getPosition() == position);
    }

    public boolean moveMrXDouble(String name, int to, Ticket ticket1, Ticket ticket2) {
        Player p = players.get(name);
        if (p instanceof MrX mrX) {
            try {

                int round = roundManager.getCurrentRound();

                mrX.moveDouble(to, ticket1, ticket2, board);

                mrXHistory.put(round, new MrXMove(to, ticket1));
                roundManager.nextRound();
                round = roundManager.getCurrentRound();
                mrXHistory.put(round, new MrXMove(to, ticket2));
                roundManager.nextTurn();

            } catch (IllegalArgumentException e) {
                logger.info("Ungültiger Doppelzug von MrX: {}" , e.getMessage());
            }
            playerPositions = roundManager.getPlayerPositions();
            String winner = getWinner().toString();
            String nextPlayer = getCurrentPlayerName();
            logger.info("➡️ currentRound: {}, nextPlayer: {}, WINNER: {}", currentRound, nextPlayer,getWinner().toString());
            messaging.convertAndSend("/topic/game/" + gameId,
                    GameMapper.mapToGameUpdate(
                            gameId,
                            playerPositions,
                            getCurrentPlayerName(),
                            winner,
                            Ticket.DOUBLE



                    )
            );
            return true;
        }
        return false;
    }

    public List<Map.Entry<Integer, String>> getAllowedDoubleMoves(String name) {
        Player p = players.get(name);
        if (p == null||!p.getTickets().hasTicket(Ticket.DOUBLE)) {
            return List.of();
        }
        List<Map.Entry<Integer, String>> result = new ArrayList<>();
        int currentPos = p.getPosition();

        List<Edge> firstMoves = board.getConnectionsFrom(currentPos)
                .stream()
                .filter(edge -> p.getTickets().hasTicket(edge.getTicket()))
                .filter(edge -> !isPositionOccupied(edge.getTo()))
                .toList();

        for (Edge firstMove : firstMoves) {
            List<Edge> secondMoves = board.getConnectionsFrom(firstMove.getTo())
                    .stream()
                    .filter(edge -> p.getTickets().hasTicket(edge.getTicket()))
                    .filter(edge -> !isPositionOccupied(edge.getTo()))
                    .toList();

            for (Edge secondMove : secondMoves) {
                if (p.getTickets().getTicketCount(firstMove.getTicket()) >= 1 &&
                        p.getTickets().getTicketCount(secondMove.getTicket()) >= 1) {

                    if (firstMove.getTicket() == secondMove.getTicket() &&
                            p.getTickets().getTicketCount(firstMove.getTicket()) < 2) {
                        continue;
                    }

                    if (secondMove.getTo() != currentPos) {
                        String combinedTickets = firstMove.getTicket() + "+" + secondMove.getTicket();
                        result.add(Map.entry(secondMove.getTo(), combinedTickets));
                    }
                }
            }
        }

        return result;
    }

    /*
    public List<MrXDoubleMove> getAllowedDoubleMoves(String name){
        Player p = players.get(name);
        if (!(p instanceof MrX mrX)) {
            return Collections.emptyList();
        }
        List<MrXDoubleMove> doubleMoves = new ArrayList<>();

        if (!mrX.getTickets().hasTicket(Ticket.DOUBLE)) return doubleMoves;

        int originalPos = mrX.getPosition();

        //Erster möglicher Zug
        for (Edge firstEdge : board.getConnectionsFrom(originalPos)) {
            int firstTo = firstEdge.getTo();
            Ticket firstTicket = firstEdge.getTicket();

            mrX.setPos(firstTo);
            //Zweiter möglicher Zug
            if (mrX.getTickets().hasTicket(firstTicket)) {

                for (Edge secondEdge : board.getConnectionsFrom(firstTo)) {
                    int secondTo = secondEdge.getTo();
                    Ticket secondTicket = secondEdge.getTicket();

                    if (mrX.getTickets().has2Tickets(firstTicket, secondTicket)) {
                        doubleMoves.add(new MrXDoubleMove(firstTo, firstTicket, secondTo, secondTicket));
                    }
                }
            }
            mrX.setPos(originalPos);
        }
        return doubleMoves;
    }

     */

    public String getVisibleMrXPosition() {
        MrX mrX = null;
        for (Player p : players.values()) {
            if (p instanceof MrX mrx) {
                mrX = mrx;
                break;
            }
        }
        if (mrX == null) return "MrX nicht im Spiel";

        if (revealRounds.contains(currentRound - 1)) {
            return String.valueOf(mrX.getPosition()); // letzte sichtbare Position
        } else {
            return "?";
        }
    }

    public List<String> getMrXMoveHistory() {
        List<String> history = new ArrayList<>();
        int currentRound = roundManager.getCurrentRound();

        for (int i = 1; i < currentRound; i++) {
            MrXMove move = mrXHistory.get(i);
            if (move == null) continue;

            String pos = revealRounds.contains(i) ? String.valueOf(move.getPosition()) : "?";
            String ticket = move.getTicket().name();

            history.add("Runde " + i + ": " + pos + " (" + ticket + ")");
        }

        return history;
    }


}

class MrXMove {
    private final int position;
    private final Ticket ticket;


    public MrXMove(int position, Ticket ticket) {
        this.position = position;
        this.ticket = ticket;
    }

    public int getPosition() {
        return position;
    }

    public Ticket getTicket() {
        return ticket;
    }
}