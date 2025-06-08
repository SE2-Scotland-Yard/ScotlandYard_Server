package at.aau.serg.scotlandyard.websocket;

import at.aau.serg.scotlandyard.dto.*;
import at.aau.serg.scotlandyard.gamelogic.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.messaging.simp.SimpMessagingTemplate;



import static org.mockito.Mockito.*;

class LobbySocketControllerTest {

    private LobbyManager lobbyManager;
    private GameManager gameManager;
    private SimpMessagingTemplate messaging;
    private LobbySocketController controller;
    private Lobby lobby;


    @BeforeEach
    void setUp() {
        lobbyManager = mock(LobbyManager.class);
        gameManager = mock(GameManager.class);
        messaging = mock(SimpMessagingTemplate.class);
        controller = new LobbySocketController(lobbyManager, gameManager, messaging);

        lobby = mock(Lobby.class);

    }

    @Test
    void testHandleReady_LobbyNotFound() {
        when(lobbyManager.getLobby("game1")).thenReturn(null);

        ReadyMessage msg = new ReadyMessage();
        msg.setGameId("game1");
        msg.setPlayerId("Anna");

        controller.handleReady(msg);

        verifyNoInteractions(messaging);
    }

    @Test
    void testHandleReady_NormalFlow() {
        when(lobbyManager.getLobby("game2")).thenReturn(lobby);
        when(lobby.allReady()).thenReturn(false);
        when(lobby.hasEnoughPlayers()).thenReturn(true);
        when(lobby.isStarted()).thenReturn(false);
        when(lobby.getGameId()).thenReturn("game2");

        ReadyMessage msg = new ReadyMessage();
        msg.setGameId("game2");
        msg.setPlayerId("Bob");

        try (MockedStatic<LobbyMapper> mocked = mockStatic(LobbyMapper.class)) {
            LobbyState mockLobbyState = mock(LobbyState.class);
            mocked.when(() -> LobbyMapper.toLobbyState(any(Lobby.class))).thenReturn(mockLobbyState);

            controller.handleReady(msg);

            verify(lobby).markReady("Bob");
            verify(messaging).convertAndSend("/topic/lobby/game2", mockLobbyState);
        }
    }



    @Test
    void testSelectRole_LobbyNotFound() {
        when(lobbyManager.getLobby("gX")).thenReturn(null);
        RoleSelectionMessage msg = new RoleSelectionMessage();
        msg.setGameId("gX");
        msg.setPlayerId("Anna");
        msg.setRole(Role.MRX);

        controller.selectRole(msg);

        verifyNoInteractions(messaging);
    }

    @Test
    void testSelectRole_NormalFlow() {
        when(lobbyManager.getLobby("game4")).thenReturn(lobby);
        when(lobby.getGameId()).thenReturn("game4");

        RoleSelectionMessage msg = new RoleSelectionMessage();
        msg.setGameId("game4");
        msg.setPlayerId("Bob");
        msg.setRole(Role.DETECTIVE);

        try (MockedStatic<LobbyMapper> mocked = mockStatic(LobbyMapper.class)) {
            LobbyState mockLobbyState = mock(LobbyState.class);
            mocked.when(() -> LobbyMapper.toLobbyState(any(Lobby.class))).thenReturn(mockLobbyState);

            controller.selectRole(msg);

            verify(lobby).selectRole("Bob", Role.DETECTIVE);
            verify(messaging).convertAndSend("/topic/lobby/game4", mockLobbyState);
        }
    }


}
