package hu.ait.keyshawn.idoodle.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class game {
    private String uid;
    private String gameName;
    private String hostUserID;
    private String drawingUrl;
    private int roundNumber;
    private String gameState;
    private String currentDrawer;
    private Map userList;

    public game(String uid, String gameName) {
        this.uid = uid;
        this.drawingUrl = "";
        this.currentDrawer = "";
        this.gameName = gameName;
        this.roundNumber = 0;
        this.userList = new HashMap<>();
        this.hostUserID = "";
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

    public Map getUserList() {
        return userList;
    }

    public void setUserList(Map userList) {
        this.userList = userList;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public String getCurrentDrawer() {
        return currentDrawer;
    }

    public void setCurrentDrawer(String currentDrawer) {
        this.currentDrawer = currentDrawer;
    }
}

