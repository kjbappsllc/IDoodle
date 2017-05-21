package hu.ait.keyshawn.idoodle.firebase;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import hu.ait.keyshawn.idoodle.GameActivity;
import hu.ait.keyshawn.idoodle.R;
import hu.ait.keyshawn.idoodle.adapter.GameUsersAdapter;
import hu.ait.keyshawn.idoodle.adapter.MessagesAdapter;
import hu.ait.keyshawn.idoodle.constants.constants;
import hu.ait.keyshawn.idoodle.data.Game;
import hu.ait.keyshawn.idoodle.data.Gamestate;
import hu.ait.keyshawn.idoodle.data.Message;
import hu.ait.keyshawn.idoodle.data.User;

import static hu.ait.keyshawn.idoodle.R.id.tvWordDraw;

public class FirebaseGameHandler {
    private Context context;
    private DatabaseReference mDatabase;
    private ValueEventListener gameHostEventListener;
    private ValueEventListener drawingEventListener;
    private ChildEventListener gameUserListChildListener;
    private ValueEventListener currentDrawerEventListener;
    private ValueEventListener gameStateEventListener;
    private ValueEventListener roundNumberEventListener;
    private ChildEventListener messageEventListener;
    private ValueEventListener currentWordEventListener;


    public FirebaseGameHandler(Context context) {
        this.context = context;
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private User getCurrentUser(){ return((GameActivity)context).getCurrentUser(); }

    public DatabaseReference getCurrentGameReference() {return mDatabase.child(constants.db_Games).
            child(getCurrentUser().getCurrentGameID());}

    public void initGameHostIDEventListener(final Game game,
                                            final GameUsersAdapter gmAdapter) {
        gameHostEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                game.setHostUserID(dataSnapshot.getValue(String.class));
                gmAdapter.setCurrentHostID(game.getHostUserID());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        getCurrentGameReference().
                child(constants.db_Games_hostID).
                addValueEventListener(gameHostEventListener);
    }

    public void initGameDrawingEventListener(final Game game,
                                           final ImageView ivProjectedCanvas) {

        drawingEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String currentGameURL = dataSnapshot.getValue(String.class);
                loadImageWithGlide(currentGameURL, game, ivProjectedCanvas);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        getCurrentGameReference().
                child(constants.db_Games_DrawingURL).
                addValueEventListener(drawingEventListener);
    }

    private void loadImageWithGlide(String currentGameURL,
                                    final Game game,
                                    final ImageView ivProjectedCanvas) {
        if(!TextUtils.isEmpty(currentGameURL)){
            try {
                Glide
                        .with(context)
                        .load(currentGameURL)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .dontAnimate()
                        .into(new SimpleTarget<Bitmap>() {

                            @Override
                            public void onResourceReady(Bitmap arg0, GlideAnimation<? super Bitmap> arg1) {
                                if(game.getGameState().
                                        equals(Gamestate.GameStateToString(Gamestate.drawingPhase))) {
                                    ivProjectedCanvas.setImageBitmap(arg0);
                                }
                            }
                        });
            }
            catch (Exception e){}
        }
        else{
            ivProjectedCanvas.setImageBitmap(null);
        }
    }

    public void initGameUserListEventListener(final Game game,
                                              final GameUsersAdapter gmAdapter,
                                              final List<String> gameUserIDS) {

        gameUserListChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                handleAddedUser(dataSnapshot, gameUserIDS, gmAdapter);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                handleUserCorrectGuess(dataSnapshot, gmAdapter, game);
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                handleUserLeaving(dataSnapshot, gameUserIDS, game, gmAdapter);
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        getCurrentGameReference().
                child(constants.db_Games_Userlist).
                addChildEventListener(gameUserListChildListener);
    }

    private void handleUserLeaving(DataSnapshot dataSnapshot,
                                   List<String> gameUserIDS,
                                   Game game,
                                   GameUsersAdapter gmAdapter) {

        String uID = dataSnapshot.getKey();
        String userInfo = dataSnapshot.getValue(String.class);

        int index = gameUserIDS.indexOf(uID);
        gameUserIDS.remove(index);

        Log.d("greattest", uID);

        if(uID.equals(game.getHostUserID())) {
            if(!getCurrentUser().getCurrentGameID().equals("")) {
                Toast.makeText(context, "HOST HAS LEFT THE GAME", Toast.LENGTH_LONG).show();
            }

            if(!getCurrentUser().getUid().equals(uID)){
                ((GameActivity) context).leaveGame();
                ((GameActivity)context).removeFromBackstack();
            }
        }
        gmAdapter.removeUser(uID, userInfo);
    }

    private void handleUserCorrectGuess(DataSnapshot dataSnapshot,
                                        GameUsersAdapter gmAdapter,
                                        Game game) {
        String uID = dataSnapshot.getKey();
        String userInfo = dataSnapshot.getValue(String.class);
        String[] data = userInfo.split(",");
        gmAdapter.updateUser(uID,userInfo);

        showCorrectGuessToast(game, uID, data);
    }

    private void showCorrectGuessToast(Game game, String uID, String[] data) {
        if(!getCurrentUser().getUid().equals(uID) &&
                !game.getCurrentDrawer().equals(uID)) {
            Toast.makeText(context, data[0] + " has guessed the word!", Toast.LENGTH_SHORT).show();
        }
        else if(!game.getCurrentDrawer().equals(getCurrentUser().getUid()) &&
                getCurrentUser().getUid().equals(uID)) {
            Toast.makeText(context, "You have guessed the word!", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleAddedUser(DataSnapshot dataSnapshot,
                                 List<String> gameUserIDS,
                                 GameUsersAdapter gmAdapter) {
        String ID = dataSnapshot.getKey();
        String userInfo = dataSnapshot.getValue(String.class);
        gameUserIDS.add(ID);
        gmAdapter.addUser(ID, userInfo);
        ((GameActivity)context).scrollUsersRecycler();
    }

    public void initCurrentDrawerEventListener(final Game game,
                                               final GameUsersAdapter gmAdapter) {

        currentDrawerEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                game.setCurrentDrawer(dataSnapshot.getValue(String.class));
                gmAdapter.setCurrentDrawerID(game.getCurrentDrawer());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        getCurrentGameReference().
                child(constants.db_Games_currentDrawer).
                addValueEventListener(currentDrawerEventListener);
    }

    public void initGameStateEventListener(final Game game) {

        gameStateEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                game.setGameState(dataSnapshot.getValue(String.class));
                handleGameStateLogic(game);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        getCurrentGameReference()
                .child(constants.db_Games_gameState).
                addValueEventListener(gameStateEventListener);
    }

    private void handleGameStateLogic(Game game) {
        if(!TextUtils.isEmpty(game.getGameState())) {
            Gamestate gs = Gamestate.valueOf(game.getGameState());

            switch (gs) {
                case preGamePhase:
                    ((GameActivity)context).doPreGamePhase();
                    break;
                case drawingPhase:
                    ((GameActivity)context).doDrawingPhase();
                    break;
                case endRoundPhase:
                    ((GameActivity)context).doEndRoundPhase();
                    break;
                case endGamePhase:
                    ((GameActivity)context).doEndGamePhase();
                    break;
            }
        }
    }

    public void initRoundNumberEventListener(final Game game) {

        roundNumberEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                handleRoundChange(dataSnapshot, game);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        if(!getCurrentUser().getCurrentGameID().isEmpty()) {
            getCurrentGameReference().
                    child(constants.db_Games_roundNumber).
                    addValueEventListener(roundNumberEventListener);
        }
    }

    private void handleRoundChange(DataSnapshot dataSnapshot, Game game) {
        if(dataSnapshot.exists()) {
            game.setRoundNumber(dataSnapshot.getValue(int.class));
            if(game.getRoundNumber() >= 6) {
                if(game.getHostUserID().equals(getCurrentUser().getUid())) {
                    getCurrentGameReference().
                            child(constants.db_Games_roundNumber).
                            setValue(1);

                    String newGs = Gamestate.GameStateToString(Gamestate.endGamePhase);
                    getCurrentGameReference().
                            child(constants.db_Games_gameState).
                            setValue(newGs);
                }
            }
            if(game.getRoundNumber() < 6) {
                ((GameActivity)context).setHeaderText();
            }
        }
    }

    public void initMessageEventListener(final MessagesAdapter mAdapter) {

        messageEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message newMessage = dataSnapshot.getValue(Message.class);
                mAdapter.addMessage(newMessage);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        getCurrentGameReference().
                child(constants.db_Games_messages).
                addChildEventListener(messageEventListener);
    }

    public void initCurrentWordListener(final Game game) {
        currentWordEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    game.setCurrentWord(dataSnapshot.getValue(String.class));
                    String [] available = ((GameActivity)context).splitCurrentWord();
                    ((GameActivity)context).setCurrentWordUI(available);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        getCurrentGameReference().
                child(constants.db_Games_currentWord).
                addValueEventListener(currentWordEventListener);
    }

    public void deinit() {
        getCurrentGameReference().
                child(constants.db_Games_hostID).
                removeEventListener(gameHostEventListener);

        getCurrentGameReference().
                child(constants.db_Games_DrawingURL).
                removeEventListener(drawingEventListener);

        getCurrentGameReference().
                child(constants.db_Games_Userlist).
                removeEventListener(gameUserListChildListener);

        getCurrentGameReference().
                child(constants.db_Games_currentDrawer).
                removeEventListener(currentDrawerEventListener);

        getCurrentGameReference()
                .child(constants.db_Games_gameState).
                removeEventListener(gameStateEventListener);

        getCurrentGameReference().
                child(constants.db_Games_roundNumber).
                removeEventListener(roundNumberEventListener);

        getCurrentGameReference().
                child(constants.db_Games_messages).
                removeEventListener(messageEventListener);

        getCurrentGameReference().
                child(constants.db_Games_currentWord).
                removeEventListener(currentWordEventListener);
    }

}
