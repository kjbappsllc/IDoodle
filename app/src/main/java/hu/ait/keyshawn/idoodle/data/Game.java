package hu.ait.keyshawn.idoodle.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Game {
    private String uid;
    private String gameName;
    private String hostUserID;
    private String drawingUrl;
    private int roundNumber;
    private String gameState;
    private String currentDrawer;
    private String currentWord;
    private HashMap<String, String> userList;
    private List<Message> messageList;

    public Game(String uid, String gameName) {
        this.uid = uid;
        this.currentWord = "";
        this.drawingUrl = "";
        this.currentDrawer = "";
        this.gameName = gameName;
        this.roundNumber = 1;
        this.userList = new HashMap<>();
        this.hostUserID = "";
        this.messageList = new ArrayList<>();
        this.gameState = saveGameState(Gamestate.preGamePhase);
    }

    public Game() {}

    public String saveGameState(Gamestate val) {
        this.gameState = val.toString();
        return val.toString();
    }

    public String getGameState() {
        return this.gameState;
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

    public HashMap<String, String> getUserList() {
        return userList;
    }

    public void setUserList(HashMap<String, String> userList) {
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

    public List<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    public String getCurrentWord() {
        return currentWord;
    }

    public void setCurrentWord(String currentWord) {
        this.currentWord = currentWord;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }
}

