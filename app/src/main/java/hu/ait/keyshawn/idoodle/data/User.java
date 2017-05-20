package hu.ait.keyshawn.idoodle.data;

/**
 * Created by mac on 5/13/17.
 */

public class User {
    private String username;
    private int gamesPlayed;
    private String uid;
    private int totalPointsEarned;
    private String currentGameID;

    public User(String username, String uid){
        this.username = username;
        this.gamesPlayed = 0;
        this.totalPointsEarned = 0;
        this.currentGameID = "";
        this.uid = uid;
    }

    public User() {}

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public void addGamesPlayed(){ this.gamesPlayed += 1; }

    public String getCurrentGameID() {
        return currentGameID;
    }

    public void setCurrentGameID(String currentGameID) {
        this.currentGameID = currentGameID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getTotalPointsEarned() {
        return totalPointsEarned;
    }

    public void setTotalPointsEarned(int totalPointsEarned) {
        this.totalPointsEarned = totalPointsEarned;
    }

    public void addTotalPointsEarned(int num){
        this.totalPointsEarned += num;
    }
}
