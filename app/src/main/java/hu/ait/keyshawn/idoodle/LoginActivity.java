package hu.ait.keyshawn.idoodle;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import hu.ait.keyshawn.idoodle.constants.constants;
import hu.ait.keyshawn.idoodle.data.user;

import static android.Manifest.permission.READ_CONTACTS;

public class LoginActivity extends AppCompatActivity{

    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    DatabaseReference mDatabase;
    private EditText etusername;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etusername = (EditText) findViewById(R.id.etusername);

        mPasswordView = (EditText) findViewById(R.id.password);

        firebaseAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        setUpButtons();

    }

    private void setUpButtons() {
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button btnregister = (Button) findViewById(R.id.registerButton);
        btnregister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });
    }


    private  void showProgressDialog() {
        if(progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Attempting Login/Register");
        }

        progressDialog.show();
    }

    private void hideProgressDialog(){
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.hide();
        }
    }

    private void attemptLogin() {
        if (!isFormValid()) {
            return;
        }

        showProgressDialog();
        SignIn();

    }

    private void attemptRegister() {
        if (!isFormValid()) {
            return;
        }

        showProgressDialog();
        Register();
        SignIn();

    }

    private void Register() {
        firebaseAuth.createUserWithEmailAndPassword(
                getString(R.string.Username_to_Email, etusername.getText().toString()),
                mPasswordView.getText().toString()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    addNewUser(task);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressDialog();
                Toast.makeText(LoginActivity.this,
                        "Unable to Register: " + e.getLocalizedMessage() ,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNewUser(@NonNull Task<AuthResult> task) {
        FirebaseUser firebaseUser = task.getResult().getUser();
        firebaseUser.updateProfile(
                new UserProfileChangeRequest.Builder().
                        setDisplayName(etusername.getText().toString()).build());

        user newUser = new user(etusername.getText().toString(), firebaseUser.getUid());
        mDatabase.child(constants.db_Users).child(newUser.getUid()).setValue(newUser);
    }

    private void SignIn() {
        firebaseAuth.signInWithEmailAndPassword(
                getString(R.string.Username_to_Email,etusername.getText().toString()),
                mPasswordView.getText().toString()
                ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {

                    saveCurrentUser(task);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressDialog();
                Toast.makeText(LoginActivity.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCurrentUser(@NonNull Task<AuthResult> task) {
        mDatabase.child(constants.db_Users).child(task.getResult().getUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user currentUser = dataSnapshot.getValue(user.class);
                ((MainApplication)getApplication()).setCurrentUser(currentUser);

                hideProgressDialog();
                //startActivity(new Intent(LoginActivity.this, GameActivity.class));
                startActivity(new Intent(LoginActivity.this, LobbyActivity.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean isFormValid(){
        etusername.setError(null);
        mPasswordView.setError(null);

        String username = etusername.getText().toString();
        String password = mPasswordView.getText().toString();

        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            return false;
        }

        if (TextUtils.isEmpty(username) || !isUserNameValid(username)) {
            if(!isUserNameValid(username)){
                etusername.setError("Name cannot contain spaces");
            }

            if(TextUtils.isEmpty(username)) {
                etusername.setError(getString(R.string.error_field_required));
            }
            return false;
        }

        return true;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    private boolean isUserNameValid(String username){
        if (username.split(" ")[0].length() != username.length()) {
            return false;
        }
        else {
            return true;
        }
    }
}

