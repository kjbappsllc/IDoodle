package hu.ait.keyshawn.idoodle.data;

import java.util.List;

/**
 * Created by mac on 5/12/17.
 */

public class game {
    private String uid;
    private String gameName;
    private String hostUserID;
    private String drawingUrl;
    private String gameState;
    private List<String> userList;

    public game(String uid, String gameName) {
        this.uid = uid;
        this.drawingUrl = "";
        this.gameName = gameName;
        this.gameState = saveGameState(gamestate.preGamePhase);
    }

    public game() {}

    public String saveGameState(gamestate val) {
        this.gameState = val.toString();
        return val.toString();
    }

    public gamestate getGameState() {
        return gamestate.valueOf(gameState);
    }

    public String getDrawingUrl() {
        return drawingUrl;
    }

    public void setDrawingUrl(String drawingUrl) {
        this.drawingUrl = drawingUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getHostUserID() {
        return hostUserID;
    }

    public void setHostUserID(String hostUserID) {
        this.hostUserID = hostUserID;
    }

    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
    }
}

