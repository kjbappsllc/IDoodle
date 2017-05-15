package hu.ait.keyshawn.idoodle.data;

/**
 * Created by mac on 5/13/17.
 */

public enum gamestate {
    preGamePhase, drawingPhase, endGamePhase;

    public static gamestate StringToGameState(String gs){
        return gamestate.valueOf(gs);
    }

    public static String GameStateToString(gamestate gs){
        return gs.toString();
    }
}
