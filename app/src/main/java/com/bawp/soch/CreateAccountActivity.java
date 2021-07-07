package com.bawp.soch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import Util.JournalApi;

public class CreateAccountActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // firestore collection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");

    private EditText emailEditText,passwordEditText,usernameEditText;
    private ProgressBar progressBar;
    private Button createAcctButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        firebaseAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.create_acct_progress);
        createAcctButton = findViewById(R.id.create_acct_button);
        usernameEditText = findViewById(R.id.username_account);
        emailEditText = findViewById(R.id.email_account);
        passwordEditText = findViewById(R.id.password_account);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if(currentUser!=null){
                    // user is already loggedin
                }else{
                    // no user yet

                }
            }
        };

        createAcctButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(emailEditText.getText().toString())
                 && !TextUtils.isEmpty(passwordEditText.getText().toString())
                 && !TextUtils.isEmpty(usernameEditText.getText().toString())) {

                    String email = emailEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();
                    String username = usernameEditText.getText().toString().trim();

                    createUserEmailAccont(email, password, username);
                }else {
                    Toast.makeText(CreateAccountActivity.this
                            ,"Empty Field Is Not Allowed"
                            ,Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    // creating new account
    private void createUserEmailAccont(String email, String password, final String username){
        if(!TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(password)
              && !TextUtils.isEmpty(username)){
            progressBar.setVisibility(View.VISIBLE);


              firebaseAuth.createUserWithEmailAndPassword(email,password)
                      .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                          @Override
                          public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    // we take user to the add journal activity
                                 //   progressBar.setVisibility(View.INVISIBLE);
                                //    Toast.makeText(CreateAccountActivity.this,"Done",Toast.LENGTH_LONG).show();
                                    currentUser = firebaseAuth.getCurrentUser();
                                    assert currentUser != null;
                                    final String currentUserId = currentUser.getUid();

                                    // create a map so that we can create a user in user collection
                                    Map<String,String> userObj = new HashMap<>();
                                    userObj.put("userId",currentUserId);
                                    userObj.put("username",username);

                                    //firestore connection
                                    collectionReference.add(userObj)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    documentReference.get()
                                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if(Objects.requireNonNull(task.getResult()).exists()){
                                                                        progressBar.setVisibility(View.INVISIBLE);

                                                                        String name = task.getResult()
                                                                                .getString("username");
                                                                        JournalApi journalApi = JournalApi.getInstance();//global class
                                                                        journalApi.setUserId(currentUserId);
                                                                        journalApi.setUsername(name);

                                                                        Intent intent = new Intent(CreateAccountActivity.this,
                                                                                PostJournalActivity.class);
                                                                        // now we pass name,userId to next activity
                                                                        intent.putExtra("username",name);
                                                                        intent.putExtra("userId",currentUserId);
                                                                        startActivity(intent);

                                                                    }else {
                                                                        progressBar.setVisibility(View.INVISIBLE);
                                                                    }
                                                                }
                                                            });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                }
                                            });




                                }else{

                                }
                          }
                      })
                      .addOnFailureListener(new OnFailureListener() {
                          @Override
                          public void onFailure(@NonNull Exception e) {

                          }
                      });

        }
        else{

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}
