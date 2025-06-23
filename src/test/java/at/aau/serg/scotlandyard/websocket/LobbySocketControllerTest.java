package at.aau.serg.scotlandyard.websocket;

import at.aau.serg.scotlandyard.dto.*;
import at.aau.serg.scotlandyard.gamelogic.*;



import at.aau.serg.scotlandyard.gamelogic.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
    @Test
    void testHandleAvatarSelection_DetectiveWithAvailableAvatar() {
        when(lobbyManager.getLobby("game5")).thenReturn(lobby);
        when(lobby.getSelectedRole("Alice")).thenReturn(Role.DETECTIVE);
        when(lobby.getAvatars()).thenReturn(new HashMap<>());

        AvatarSelectionMessage msg = new AvatarSelectionMessage();
        msg.setGameId("game5");
        msg.setPlayerId("Alice");
        msg.setAvatarResId(101);

        try (MockedStatic<LobbyMapper> mocked = mockStatic(LobbyMapper.class)) {
            LobbyState mockState = mock(LobbyState.class);
            mocked.when(() -> LobbyMapper.toLobbyState(lobby)).thenReturn(mockState);

            controller.handleAvatarSelection(msg);

            verify(lobby).selectAvatar("Alice", 101);
            verify(messaging).convertAndSend("/topic/lobby/game5", mockState);
        }
    }

    @Test
    void testHandleLeave_RemovesPlayerAndSendsLobbyUpdate() {
        when(lobbyManager.getLobby("game6")).thenReturn(lobby);

        LeaveRequest req = new LeaveRequest();
        req.setGameId("game6");
        req.setPlayerId("Bob");

        try (MockedStatic<LobbyMapper> mocked = mockStatic(LobbyMapper.class)) {
            LobbyState mockState = mock(LobbyState.class);
            mocked.when(() -> LobbyMapper.toLobbyState(lobby)).thenReturn(mockState);

            controller.handleLeave(req);

            verify(lobbyManager).leaveLobby("game6", "Bob");
            verify(messaging).convertAndSend("/topic/lobby/game6", mockState);
        }
    }

    @Test
    void testHandlePing_LobbyExists_UpdatesActivity() {
        when(lobbyManager.getLobby("game7")).thenReturn(lobby);

        Map<String, String> payload = Map.of(
                "gameId", "game7",
                "playerId", "Clara"
        );

        controller.handlePing(payload);

        verify(lobby).updateLastActivity("Clara");
    }

    @Test
    void testHandleReady_AllReadyButNoMrX_IgnoresBots() {
        when(lobbyManager.getLobby("gameX")).thenReturn(lobby);
        when(lobby.allReady()).thenReturn(true);
        when(lobby.hasEnoughPlayers()).thenReturn(true);
        when(lobby.isStarted()).thenReturn(false);
        when(lobby.hasExactlyOneMrX()).thenReturn(false);
        when(lobby.getPlayers()).thenReturn(Set.of("Human1", "Bot1"));

        when(lobby.isHuman("Human1")).thenReturn(true);
        when(lobby.isHuman("Bot1")).thenReturn(false);


        ReadyMessage msg = new ReadyMessage();
        msg.setGameId("gameX");
        msg.setPlayerId("Human1");

        try (MockedStatic<LobbyMapper> mocked = mockStatic(LobbyMapper.class)) {
            LobbyState mockState = mock(LobbyState.class);
            mocked.when(() -> LobbyMapper.toLobbyState(lobby)).thenReturn(mockState);

            controller.handleReady(msg);

            // Es wird eine Fehlermeldung gesendet
            verify(messaging).convertAndSend(
                    eq("/topic/lobby/gameX/error"),
                    (Object) argThat(obj -> obj instanceof Map<?, ?> errorMap &&
                            "Es konnte kein Verbrecher ausgemacht werden".equals(errorMap.get("error")))
            );
            verify(lobby).markNotReady("Human1");
            verify(lobby, never()).markNotReady("Bot1");


            // Lobby-State erneut gesendet
            verify(messaging, atLeastOnce()).convertAndSend(eq("/topic/lobby/gameX"), eq(mockState));
        }
    }


    @Test
    void testHandleAddBot_AddsBotAndMarksReady() {
        when(lobbyManager.getLobby("game9")).thenReturn(lobby);
        when(lobby.getPlayers()).thenReturn(new HashSet<>(Set.of("Player1")));

        AddBotMessage msg = new AddBotMessage();
        msg.setGameId("game9");

        try (MockedStatic<LobbyMapper> mocked = mockStatic(LobbyMapper.class)) {
            LobbyState mockState = mock(LobbyState.class);
            mocked.when(() -> LobbyMapper.toLobbyState(lobby)).thenReturn(mockState);

            controller.handleAddBot(msg);

            verify(lobby).addPlayer("[BOT] 1");
            verify(lobby).selectRole("[BOT] 1", Role.DETECTIVE);
            verify(lobby).markReady("[BOT] 1");
            verify(messaging).convertAndSend("/topic/lobby/game9", mockState);
        }
    }

    @Test
    void testHandleRemoveBot_RemovesLastBot() {
        when(lobbyManager.getLobby("game10")).thenReturn(lobby);
        when(lobby.getPlayers()).thenReturn(Set.of("Player1", "[BOT] 1", "[BOT] 2"));

        RemoveBotMessage msg = new RemoveBotMessage();
        msg.setGameId("game10");

        try (MockedStatic<LobbyMapper> mocked = mockStatic(LobbyMapper.class)) {
            LobbyState mockState = mock(LobbyState.class);
            mocked.when(() -> LobbyMapper.toLobbyState(lobby)).thenReturn(mockState);

            controller.handleRemoveBot(msg);

            verify(lobby).removePlayer("[BOT] 2");
            verify(messaging).convertAndSend("/topic/lobby/game10", mockState);
        }
    }

    @Test
    void testHandleReady_StartsGameWhenAllReadyAndOneMrX() {
        when(lobbyManager.getLobby("game11")).thenReturn(lobby);
        when(lobby.allReady()).thenReturn(true);
        when(lobby.hasEnoughPlayers()).thenReturn(true);
        when(lobby.isStarted()).thenReturn(false);
        when(lobby.hasExactlyOneMrX()).thenReturn(true);
        when(lobby.getGameId()).thenReturn("game11");
        when(lobby.getPlayers()).thenReturn(Set.of("MrX", "Det1"));

        when(lobby.getSelectedRole("MrX")).thenReturn(Role.MRX);
        when(lobby.getSelectedRole("Det1")).thenReturn(Role.DETECTIVE);

        GameState game = mock(GameState.class);
        when(gameManager.getOrCreateGame("game11")).thenReturn(game);
        when(game.getCurrentPlayerName()).thenReturn("Det1");
        when(game.getAllPlayers()).thenReturn(Map.of("MrX", mock(Player.class), "Det1", mock(Player.class)));

        RoundManager roundManager = mock(RoundManager.class);
        when(game.getRoundManager()).thenReturn(roundManager);
        when(roundManager.getPlayerPositions()).thenReturn(Map.of("MrX", 50, "Det1", 60));

        ReadyMessage msg = new ReadyMessage();
        msg.setGameId("game11");
        msg.setPlayerId("Det1");

        try (MockedStatic<LobbyMapper> mockedLobby = mockStatic(LobbyMapper.class);
             MockedStatic<GameMapper> mockedGame = mockStatic(GameMapper.class)) {

            LobbyState lobbyState = mock(LobbyState.class);
            mockedLobby.when(() -> LobbyMapper.toLobbyState(any())).thenReturn(lobbyState);

            GameUpdate update = mock(GameUpdate.class);
            mockedGame.when(() -> GameMapper.mapToGameUpdate(anyString(), anyMap(), anyString(), any(), any(), any()))
                    .thenReturn(update);

            controller.handleReady(msg);

            verify(gameManager).getOrCreateGame("game11");
            verify(game).initRoundManager(anyList(), any());
            verify(messaging).convertAndSend("/topic/game/game11", update);
            verify(messaging, atLeast(1)).convertAndSend("/topic/lobby/game11", lobbyState);
        }
    }

    @Test
    void testHandleAvatarSelection_AlreadyTakenAvatar_NoUpdate() {
        when(lobbyManager.getLobby("game12")).thenReturn(lobby);
        when(lobby.getSelectedRole("Eve")).thenReturn(Role.DETECTIVE);

        Map<String, Integer> avatars = new HashMap<>();
        avatars.put("Eve", 101);
        avatars.put("Other", 102);

        when(lobby.getAvatars()).thenReturn(avatars);

        AvatarSelectionMessage msg = new AvatarSelectionMessage();
        msg.setGameId("game12");
        msg.setPlayerId("Eve");
        msg.setAvatarResId(102); // Already taken

        controller.handleAvatarSelection(msg);

        verify(lobby, never()).selectAvatar(any(), anyInt());
        verifyNoInteractions(messaging);
    }
    @Test
    void testInitializeGame_MrXAndDetectiveSamePosition_ChangesMrXPosition() {
        when(lobbyManager.getLobby("game42")).thenReturn(lobby);
        when(lobby.getPlayers()).thenReturn(Set.of("MrX", "Det1"));
        when(lobby.getSelectedRole("MrX")).thenReturn(Role.MRX);
        when(lobby.getSelectedRole("Det1")).thenReturn(Role.DETECTIVE);
        when(lobby.allReady()).thenReturn(true);
        when(lobby.hasEnoughPlayers()).thenReturn(true);
        when(lobby.isStarted()).thenReturn(false);
        when(lobby.hasExactlyOneMrX()).thenReturn(true);
        when(lobby.getGameId()).thenReturn("game42");


        GameState gameState = new GameState("game42", messaging);
        when(gameManager.getOrCreateGame("game42")).thenReturn(gameState);

        ReadyMessage msg = new ReadyMessage();
        msg.setGameId("game42");
        msg.setPlayerId("Det1");

        try (MockedStatic<LobbyMapper> mockedLobby = mockStatic(LobbyMapper.class);
             MockedStatic<GameMapper> mockedGame = mockStatic(GameMapper.class)) {

            LobbyState lobbyState = mock(LobbyState.class);
            mockedLobby.when(() -> LobbyMapper.toLobbyState(any())).thenReturn(lobbyState);

            GameUpdate update = mock(GameUpdate.class);
            mockedGame.when(() -> GameMapper.mapToGameUpdate(any(), any(), any(), any(), any(), any()))
                    .thenReturn(update);

            controller.handleReady(msg);
        }

        // Nun kannst du aus dem echten GameState auslesen
        Integer mrXPos = gameState.getAllPlayers().get("MrX").getPosition();
        Integer detPos = gameState.getAllPlayers().get("Det1").getPosition();

        assertNotEquals(mrXPos, detPos, "MrX sollte nicht auf derselben Position wie der Detective sein.");
    }















}
