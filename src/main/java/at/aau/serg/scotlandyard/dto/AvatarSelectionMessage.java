package at.aau.serg.scotlandyard.dto;

import lombok.Setter;

@Setter
public class AvatarSelectionMessage {
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
