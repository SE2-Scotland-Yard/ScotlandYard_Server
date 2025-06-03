package at.aau.serg.scotlandyard.dto;

import lombok.Setter;

public class AvatarSelectionMessage {
    @Setter
    private String gameId;
    private String playerId;
    private int avatarResId;

    public String getGameId() {
        return gameId;
    }

    public String getPlayerId() {
        return playerId;
    }


    public int getAvatarResId() {
        return avatarResId;
    }

}
