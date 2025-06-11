package at.aau.serg.scotlandyard.websocket;

import at.aau.serg.scotlandyard.gamelogic.Lobby;
import at.aau.serg.scotlandyard.gamelogic.LobbyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

class LobbyInactivityMonitorTest {

    private LobbyManager lobbyManager;
    private SimpMessagingTemplate messaging;
    private LobbyInactivityMonitor monitor;

    @BeforeEach
    void setUp() {
        lobbyManager = mock(LobbyManager.class);
        messaging = mock(SimpMessagingTemplate.class);
        monitor = new LobbyInactivityMonitor(lobbyManager, messaging);
    }

    @Test
    void testNoInactivePlayers() {
        Lobby lobby = mock(Lobby.class);
        when(lobby.getGameId()).thenReturn("game1");
        when(lobby.getPlayers()).thenReturn(Set.of("Alice"));
        when(lobby.getLastActivity("Alice")).thenReturn(System.currentTimeMillis());
        when(lobbyManager.getAllLobbies()).thenReturn(List.of(lobby));

        monitor.checkInactivePlayers();

        verify(lobby, never()).removePlayer(any());
        verify(lobbyManager, never()).removeLobby(any());
    }

    @Test
    void testRemoveInactivePlayer() {
        Lobby lobby = mock(Lobby.class);
        when(lobby.getGameId()).thenReturn("game2");

        // simulate initial players
        when(lobby.getPlayers()).thenReturn(Set.of("Charlie")); // 1. Aufruf
        when(lobby.getLastActivity("Charlie")).thenReturn(System.currentTimeMillis() - (5 * 60 * 1000));

        when(lobbyManager.getAllLobbies()).thenReturn(List.of(lobby));


        when(lobby.getPlayers())
                .thenReturn(Set.of("Charlie"))   // initial state
                .thenReturn(Set.of());           // after Charlie was removed

        monitor.checkInactivePlayers();

        verify(lobby).removePlayer("Charlie");
        verify(messaging).convertAndSend(eq("/topic/lobby/game2"), any(Object.class));
        verify(lobbyManager).removeLobby("game2");
    }


    @Test
    void testLobbyWithOnlyBotsIsRemoved() {
        Lobby lobby = mock(Lobby.class);
        when(lobby.getGameId()).thenReturn("game3");
        when(lobby.getPlayers()).thenReturn(Set.of("[BOT] A", "[BOT] B"));
        when(lobbyManager.getAllLobbies()).thenReturn(List.of(lobby));

        monitor.checkInactivePlayers();

        verify(lobbyManager).removeLobby("game3");
    }

    @Test
    void testBotIsNotRemoved() {
        Lobby lobby = mock(Lobby.class);
        when(lobby.getGameId()).thenReturn("game4");
        when(lobby.getPlayers()).thenReturn(Set.of("[BOT]Tom"));
        when(lobby.getLastActivity("[BOT]Tom")).thenReturn(System.currentTimeMillis() - (5 * 60 * 1000));
        when(lobbyManager.getAllLobbies()).thenReturn(List.of(lobby));

        monitor.checkInactivePlayers();

        verify(lobby, never()).removePlayer(any());
        verify(lobbyManager).removeLobby("game4"); // nur Bot → entfernt
    }
}
