package com.example.onlineschool;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onlineschool.Models.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity ***: ";
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private EditText etEmail;
    private EditText etPassword;
    FirebaseAuth.AuthStateListener mAuthListener;
    private boolean isParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

        verifyUserLogged();

        userLogin();

        switchToRegister();

        switchToResetPassword();
    }

    private void init(){
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
    }

    private void userLogin(){
        ((Button) findViewById(R.id.bSignIn)).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if(email == "" || password == ""){
                    String messageError = getResources().getString(R.string.messageRegisterFillError);

                    if(TextUtils.isEmpty(email))
                        etEmail.setError(messageError);

                    if(password == "")
                        etPassword.setError(messageError);

                    if(password.length() < 6)
                        etPassword.setError(getResources().getString(R.string.messageErrorPasswordLength));

                    return;
                }

                //authenticate the user
                fAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                            if(fAuth.getCurrentUser().isEmailVerified()){
                                Toast.makeText(LoginActivity.this, getResources().getString(R.string.LoginUserSuccessful), Toast.LENGTH_SHORT).show();

                                //save connection
                                final String userID = fAuth.getCurrentUser().getUid();
                                final DocumentReference userRef = fStore.document("students/"+userID);
                                userRef.get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                if(documentSnapshot.exists()){
                                                    isParent = false;

                                                }else{
                                                    isParent = true;

                                                }

                                                //is a student
                                                if(!isParent){
                                                    Log.d("Login Activity *** :", "onSuccess : is a student with ID = " + userID);
                                                    userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                            Student student = documentSnapshot.toObject(Student.class);
                                                            List<Timestamp> studentConnectionList = student.getStudentConnection();
                                                            Collections.sort(studentConnectionList);
                                                            if(studentConnectionList.size() > 5){
                                                                studentConnectionList.remove(0);
                                                            }
                                                            Date date = new Date();
                                                            Timestamp newConnection = new Timestamp(date);
                                                            studentConnectionList.add(newConnection);
                                                            userRef.set(student).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d("Login Activity *** :", "onSuccess : student connection time is added to list for " + userID);
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d("Login Activity **** :", "onFailure : " + e.toString());
                                                                }
                                                            });;
                                                        }
                                                    });
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(LoginActivity.this, "Error ! ", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                ;


                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }else{
                                fAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.LoginUserNotVerified), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        /*}else{
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.CreatedUserError) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }*/
                    }
                });
            }
        });
    }

    private void verifyUserLogged(){

        if(fAuth.getCurrentUser() != null && fAuth.getCurrentUser().isEmailVerified()){
            Log.d(TAG, fAuth.getCurrentUser().getUid());
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    private void switchToRegister(){
        ((TextView) findViewById(R.id.tvRegisterLinkParent)).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterParentActivity.class);
                LoginActivity.this.startActivity(registerIntent);
                finish();
            }
        });

        ((TextView) findViewById(R.id.tvRegisterLinkStudent)).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterStudentActivity.class);
                LoginActivity.this.startActivity(registerIntent);
                finish();
            }
        });
    }

    private void switchToResetPassword(){
        ((TextView) findViewById(R.id.tvResetPassword)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resetPasswordIntent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(resetPasswordIntent);
                finish();
            }
        });
    }
}
