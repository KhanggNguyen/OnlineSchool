package com.example.onlineschool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onlineschool.Models.Grade;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterParentActivity extends AppCompatActivity {
    public static final String TAG = "RegisterActivity : ";

    //private UserController controller;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    private EditText etAge;
    private EditText etName;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etAddress;
    private EditText etEmail;
    //private RadioButton rdStudent;

    //for picking interested grades
    private String[] listItems;
    private List<Grade> gradesList = new ArrayList<Grade>();
    private boolean[] checkedItems;
    private ArrayList<Integer> mUserItems = new ArrayList<Integer>();
    private List<Grade> mUserGrades = new ArrayList<Grade>();
    private TextView tvItemsSelected;
    //private String[] gradeArray;
    private List<Integer> grades = new ArrayList<Integer>();

    //for register user
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_parent);
        init();

        verifyUserLogged();

        registerUser();

        onPickingGrade();//initialize button method

        switchToLogin();
    }

    /**
     * Initialize elements necessary
     */
    private void init(){
        //initialize db
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();

        //initialize view elements
        etAge = (EditText) findViewById(R.id.etAge);
        etName = (EditText) findViewById(R.id.etName);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etAddress = (EditText) findViewById((R.id.etAddress));
        etEmail = (EditText) findViewById(R.id.etEmail);
        //rdStudent = (RadioButton) findViewById(R.id.radio_student);


        //picking grade elements
        CollectionReference collectionGradesRef = fStore.collection("grade");
        collectionGradesRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int i=0;
                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    Grade grade = documentSnapshot.toObject(Grade.class);
                    grade.setId(i+1);
                    gradesList.add(grade);
                    listItems[i] = grade.getGrade();
                    i++;
                }
            }
        });
        tvItemsSelected = (TextView) findViewById(R.id.tvItemsSelected);
        listItems = getResources().getStringArray(R.array.grade_item);
        checkedItems = new boolean[listItems.length];
    }

    /**
     * Initialize button switch from register activity to login activity
     */
    private void switchToLogin(){
        ((TextView) findViewById(R.id.tvLoginLink)).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(RegisterParentActivity.this, LoginActivity.class);
                RegisterParentActivity.this.startActivity(loginIntent);
            }
        });
    }

    /**
     * If user is logged in and their Email is verifed, switch them to dashboard
     */
    private void verifyUserLogged(){
        if(fAuth.getCurrentUser() != null && fAuth.getCurrentUser().isEmailVerified()){
            startActivity(new Intent(RegisterParentActivity.this, MainActivity.class));
            finish();
        }
    }

    /**
     * Initialize button register user
     */
    private void registerUser(){
        ((Button) findViewById(R.id.bRegister)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                final String name = etName.getText().toString();
                final String username = etUsername.getText().toString();
                final Integer age = Integer.parseInt(etAge.getText().toString());
                final String address = etAddress.getText().toString();
                String password = etPassword.getText().toString().trim();
                final String email = etEmail.getText().toString().trim();
                //final Integer role;
                /*
                if(rdStudent.isChecked())
                    role = 2;
                else
                    role = 1;
                */
                if(username == "" || name == "" || TextUtils.isEmpty(age + "") || password == ""){
                    String messageError = getResources().getString(R.string.messageRegisterFillError);
                    if(username == "")
                        etUsername.setError(messageError);

                    if(name == ""){
                        etName.setError(messageError);
                    }

                    if((age + "") == "")
                        etAge.setError(messageError);

                    if(password == "")
                        etPassword.setError(messageError);

                    if(password.length() < 6)
                        etPassword.setError(getResources().getString(R.string.messageErrorPasswordLength));

                    return;
                }

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            fAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(RegisterParentActivity.this, getResources().getString(R.string.CreatedUser), Toast.LENGTH_SHORT).show();
                                        userID = fAuth.getCurrentUser().getUid();

                                        //Create user in collections users
                                        final DocumentReference documentReference = fStore.collection("users").document(userID);
                                        final Map<String, Object> user = new HashMap<>();
                                        user.put("name", name);
                                        user.put("username", username);
                                        user.put("search", username.toLowerCase());
                                        user.put("email", email);
                                        user.put("age", age);
                                        user.put("address", address);
                                        //Create grade list
                                        CollectionReference gradeRef = fStore.collection("grade");
                                        gradeRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                                                    Grade g = documentSnapshot.toObject(Grade.class);
                                                    g.setId(Integer.parseInt(documentSnapshot.getId()));
                                                    if(grades.contains(g.getId()-1)){
                                                        mUserGrades.add(g);
                                                    }
                                                }
                                                user.put("grades", mUserGrades);
                                                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "onSuccess : user Profile is created for " + userID);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d(TAG, "onFailure : " + e.toString());
                                                    }
                                                });
                                            }
                                        });




                                        //Create User Role
                                        DocumentReference documentReferenceParent = fStore.collection("parents").document(userID);
                                        //DocumentReference documentReferenceStudent = fStore.collection("students").document(userID);
                                        //if(role == 1){
                                            Map<String, Object> parent = new HashMap<>();
                                            parent.put("totalStudent", 0);
                                            List<DocumentReference> studentRefList = new ArrayList<DocumentReference>();
                                            parent.put("studentsRef", studentRefList);
                                            documentReferenceParent.set(parent).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "onSuccess : parent Profile is created for " + userID);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure : " + e.toString());
                                                }
                                            });
                                        //}
                                        //Student
                                        /*
                                        if(role == 2){
                                            Map<String, Object> student = new HashMap<>();
                                            student.put("parentReference", null);
                                            documentReferenceStudent.set(student).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "onSuccess : student Profile is created for " + userID);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure : " + e.toString());
                                                }
                                            });
                                        }*/

                                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                    }else{
                                        Toast.makeText(RegisterParentActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(RegisterParentActivity.this, getResources().getString(R.string.CreatedUserError) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void onPickingGrade(){
        ((Button) findViewById(R.id.bPickingGrade)).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(RegisterParentActivity.this);
                mBuilder.setTitle(getResources().getString(R.string.bSchoolYears));
                mBuilder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                        if(isChecked){
                            if(! mUserItems.contains(position)){
                                //mUserItems.add(gradesList.get(position));
                                mUserItems.add(position);
                            }
                        }else if(mUserItems.contains(position)){
                            mUserItems.remove(position);
                        }
                    }
                });

                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String item = "";
                        for(int i=0; i<mUserItems.size(); i++){
                            item = item + listItems[mUserItems.get(i)];
                            //item = item + listItems[mUserItems.get(i).getId()];
                            grades.add(mUserItems.get(i));
                            if(i != mUserItems.size() -1){
                                item = item + ", ";
                            }
                        }
                        tvItemsSelected.setText(item);
                    }
                });

                mBuilder.setNegativeButton(getResources().getString(R.string.bCancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                mBuilder.setNeutralButton(getResources().getString(R.string.bClearAll), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i=0; i< checkedItems.length; i++){
                            checkedItems[i] = false;
                            mUserItems.clear();
                            tvItemsSelected.setText("");
                        }
                    }
                });

                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
            }
        });
    }



}
