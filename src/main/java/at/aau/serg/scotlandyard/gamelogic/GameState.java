package at.aau.serg.scotlandyard.gamelogic;


import at.aau.serg.scotlandyard.bot.BotFactory;
import at.aau.serg.scotlandyard.bot.BotPlayer;
import at.aau.serg.scotlandyard.dto.GameMapper;

import at.aau.serg.scotlandyard.gamelogic.board.Board;
import at.aau.serg.scotlandyard.gamelogic.board.Edge;
import at.aau.serg.scotlandyard.gamelogic.player.Detective;
import at.aau.serg.scotlandyard.gamelogic.player.MrX;
import at.aau.serg.scotlandyard.gamelogic.player.Player;
import at.aau.serg.scotlandyard.gamelogic.player.tickets.Ticket;
import at.aau.serg.scotlandyard.bot.BotLogic;


import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;


public class GameState {
    private final SimpMessagingTemplate messaging;
    @Getter
    private final String gameId;
    @Getter
    private final Board board;
    private final Map<String, Player> players = new HashMap<>();
    @Getter
    private RoundManager roundManager;
    private final List<Integer> revealRounds = List.of(3, 8, 13, 18, 24); // Sichtbarkeitsrunden
    private final Map<Integer, MrXMove> mrXHistory = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(GameState.class);
    private static final String TOPIC_GAME = "/topic/game/";

    Map<String, Integer> playerPositions = new HashMap<>();
    private final Map<String, Long> lastActivityMap = new HashMap<>();

    private GameManager gameManager;

    public GameState(String gameId, SimpMessagingTemplate messaging) {
        this.board = new Board();
        this.gameId = gameId;
        this.messaging = messaging;
    }

    public GameState(String gameId, SimpMessagingTemplate messaging, GameManager gameManager) {
        this.board = new Board();
        this.gameId = gameId;
        this.messaging = messaging;
        this.gameManager = gameManager;
    }

    public void initRoundManager(List<Detective>detectives, MrX mrX){ //nicht ideal
        this.roundManager = new RoundManager(detectives, mrX);
        this.roundManager.setGameState(this);
    }

    public void cantMove(String gameId) {
        roundManager.nextTurn();
        messaging.convertAndSend(TOPIC_GAME + gameId,
                GameMapper.mapToGameUpdate(
                        gameId,
                        playerPositions,
                        getCurrentPlayerName(),
                        getWinner().toString(),
                        null,
                        players



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
        int position;
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
            roundManager.setLastPlayerMoved(p);

            playerPositions = roundManager.getPlayerPositions();

            String nextPlayer = getCurrentPlayerName();
            logger.info("➡️ currentRound: {}, nextPlayer: {}", roundManager.getCurrentRound(), nextPlayer);
            messaging.convertAndSend(TOPIC_GAME + gameId,
                    GameMapper.mapToGameUpdate(
                            gameId,
                            playerPositions,
                            getCurrentPlayerName(),
                            getWinner().toString(),
                            Ticket.BLACK,
                            players
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
            roundManager.setLastPlayerMoved(p);


            playerPositions = roundManager.getPlayerPositions();
            String winner = getWinner().toString();
            String nextPlayer = getCurrentPlayerName();
            logger.info("➡️ currentRound: {}, nextPlayer: {}, WINNER: {}", roundManager.getCurrentRound(), nextPlayer,winner);
            messaging.convertAndSend(TOPIC_GAME + gameId,
                    GameMapper.mapToGameUpdate(
                            gameId,
                            playerPositions,
                            getCurrentPlayerName(),
                            winner,
                            ticket,
                            players



                    )
            );
            return true;
        }
        if (p != null && p.isValidMove(to, ticket, board)) {
            p.move(to, ticket, board);
            playerPositions = roundManager.getPlayerPositions();
            roundManager.nextTurn();
            roundManager.setLastPlayerMoved(p);

            roundManager.addMrXTicket(ticket);

            String nextPlayer = getCurrentPlayerName();
            String winner = getWinner().toString();
            logger.info("➡️ currentRound: {}, nextPlayer: {}", roundManager.getCurrentRound(), nextPlayer);
            logger.info("WINNER: {}", winner);
            messaging.convertAndSend(TOPIC_GAME + gameId,
                    GameMapper.mapToGameUpdate(
                            gameId,
                            playerPositions,
                            getCurrentPlayerName(),
                            winner,
                            ticket,
                            players

                    )
            );

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
        if(roundManager.mrXwinByNoMoves()){
            return Winner.MR_X;
        }
        if(roundManager.isMrXCaptured()){
            return Winner.DETECTIVE;
        }
        if(!roundManager.isGameOver()){
            return Winner.NONE; //Game still running
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

    public boolean moveMrXDouble(String name, int toFirst,int to, Ticket ticket1, Ticket ticket2) {
        Player p = players.get(name);
        if (p instanceof MrX mrX) {
            try {
                logger.info("Ticket 1: {}", ticket1);
                int round = roundManager.getCurrentRound();
                mrX.useDouble(Ticket.DOUBLE);
                mrX.moveDouble(toFirst,ticket1);

                mrXHistory.put(round, new MrXMove(toFirst, ticket1));
                roundManager.nextRound();
                round = roundManager.getCurrentRound();
                mrX.moveDouble(to,ticket2);
                mrXHistory.put(round, new MrXMove(to, ticket2));
                roundManager.nextTurn();

            } catch (IllegalArgumentException e) {
                logger.info("Ungültiger Doppelzug von MrX: {}" , e.getMessage());
            }
            roundManager.setLastPlayerMoved(p);
            playerPositions = roundManager.getPlayerPositions();
            String winner = getWinner().toString();
            String nextPlayer = getCurrentPlayerName();
            logger.info("➡️ currentRound: {}, nextPlayer: {}, WINNER: {}", roundManager.getCurrentRound(), nextPlayer, winner);
            messaging.convertAndSend(TOPIC_GAME + gameId,
                    GameMapper.mapToGameUpdate(
                            gameId,
                            playerPositions,
                            getCurrentPlayerName(),
                            winner,
                            Ticket.DOUBLE,
                            players

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
                        String combinedTickets = firstMove.getTicket() + "+" + secondMove.getTicket()+"+" + firstMove.getTo();
                        result.add(Map.entry(secondMove.getTo(), combinedTickets));
                    }
                }
            }
        }

        return result;
    }

    public List<String> getMrXMoveHistory() {
        List<String> history = new ArrayList<>();
        for (int i = 1; i <= roundManager.getCurrentRound(); i++) {
            MrXMove move = mrXHistory.get(i);
            if (move == null) continue;

            String pos = revealRounds.contains(i) ? String.valueOf(move.getPosition()) : "?";
            String ticket = move.getTicket().name();

            history.add("Runde " + i + ": " + pos + " (" + ticket + ")");
        }

        return history;
    }


    public void updateLastActivity(String playerId) {
        lastActivityMap.put(playerId, System.currentTimeMillis());
    }

    public Map<String, Long> getLastActivityMap() {
        return new HashMap<>(lastActivityMap);
    }

    public Player replaceWithBot(String playerName) {
        Player original = players.get(playerName);
        if (original == null) return null;

        // Wenn MrX geht, Spiel abbrechen (nicht durch Bot ersetzen)
        if (original instanceof MrX) {
            messaging.convertAndSend(TOPIC_GAME + gameId + "/system", "mrX");

            if (gameManager != null) {
                gameManager.removeGame(gameId);
            }
            return null;
        }



        // Bot erzeugen und Spieler ersetzen
        Player bot = BotFactory.createBotReplacement(original);

            players.remove(original.getName());
            playerPositions.remove(original.getName());
            players.put(bot.getName(), bot);

            if (roundManager != null) {
                roundManager.replacePlayer(original, bot);
            }

            messaging.convertAndSend(
                    TOPIC_GAME + gameId + "/system",
                    "🤖 Spieler '" + original.getName() + "' wurde durch den Bot '" + bot.getName() + "' ersetzt."
            );

            // Bot sofort handeln lassen, falls er am Zug ist
            if (bot.getName().equals(getCurrentPlayerName())) {
                var move = BotLogic.decideMove(bot.getName(), this);
                if (move != null) {
                    movePlayer(bot.getName(), move.getKey(), move.getValue());
                } else {
                    cantMove(gameId);
                }
            }

            if (onlyBotsLeft()) {
                gameManager.removeGame(gameId);
            }

        return null;
    }

    public boolean onlyBotsLeft() {
        return players.values().stream().allMatch(BotPlayer.class::isInstance);
    }

    public List <Map.Entry<Integer, Ticket>> getShortestMoveTo(String playerName) {
        if (playerName == null) {
            return new ArrayList<>();
        }

        Player player = players.get(playerName);
        if (player == null) {
            return new ArrayList<>();
        }
        int to = getMrXPosition();
        int from = player.getPosition();

        Map<Integer, Integer> distances = new HashMap<>();
        Map<Integer, Map.Entry<Integer, Ticket>> predecessors = new HashMap<>();
        PriorityQueue<Integer> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        distances.put(from, 0);
        queue.add(from);

        while (!queue.isEmpty()) {
            int current = queue.poll();

            if (current == to) {
                break;
            }

            for (Edge edge : board.getConnectionsFrom(current)) {
                if ((!player.getTickets().hasTicket(edge.getTicket()))||(player instanceof Detective && isPositionOccupied(edge.getTo()))) {
                    continue;
                }

                int newDist = distances.getOrDefault(current, Integer.MAX_VALUE) + 1;

                if (newDist < distances.getOrDefault(edge.getTo(), Integer.MAX_VALUE)) {
                    distances.put(edge.getTo(), newDist);
                    predecessors.put(edge.getTo(), Map.entry(current, edge.getTicket()));
                    queue.remove(edge.getTo());
                    queue.add(edge.getTo());
                }
            }
        }

        if (!distances.containsKey(to)) {
            return new ArrayList<>();
        }

        if (from == to) {
            return new ArrayList<>();
        }

        int current = to;
        while (predecessors.get(current).getKey() != from) {
            current = predecessors.get(current).getKey();
        }

        Map.Entry<Integer, Ticket> nextMove = Map.entry(current, predecessors.get(current).getValue());

        return Collections.singletonList(nextMove);
    }
    public Integer getMrXPosition() {
        for (Player p : players.values()) {
            if (p instanceof MrX) {
                return p.getPosition();
            }
        }
        return null;
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