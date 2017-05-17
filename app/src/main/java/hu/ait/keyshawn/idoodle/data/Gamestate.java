package hu.ait.keyshawn.idoodle.data;

/**
 * Created by mac on 5/13/17.
 */

public enum Gamestate {
    preGamePhase, drawingPhase, endGamePhase;

    public static Gamestate StringToGameState(String gs){
        return Gamestate.valueOf(gs);
    }

    public static String GameStateToString(Gamestate gs){
        return gs.toString();
    }
}
