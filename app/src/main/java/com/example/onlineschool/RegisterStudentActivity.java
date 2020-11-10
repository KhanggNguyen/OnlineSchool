package com.example.onlineschool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onlineschool.Models.Formula;
import com.example.onlineschool.Models.Grade;
import com.example.onlineschool.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterStudentActivity extends AppCompatActivity {
    public static final String TAG = "RegisterActivity : ";

    //Database variable;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    //for register user
    private String userID;

    private EditText etAge;
    private EditText etName;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etAddress;
    private EditText etEmail;

    //for picking interested grades
    private CharSequence[] listItems;
    private List<Grade> gradesList = new ArrayList<Grade>();
    private int checkedItem;
    private List<Grade> mUserItems = new ArrayList<Grade>();
    private TextView tvItemsSelected;

    //for picking formula
    private CharSequence[] listItemsFormula;
    private List<Formula> formulasList = new ArrayList<>();
    private int checkedItemFormula;
    private List<Formula> mUserItemsFormula = new ArrayList<>();
    private TextView tvItemsFormulaSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_student);

        init();

        verifyUserLogged();

        registerUser();

        switchToLogin();

        onPickingGrade();//initialize button method

        onPickingFormula();
    }

    private void init() {
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

        //picking grade elements
        CollectionReference collectionGradesRef = fStore.collection("grade");
        collectionGradesRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int i=0;
                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    Grade grade = documentSnapshot.toObject(Grade.class);
                    grade.setId(Integer.parseInt(documentSnapshot.getId()));
                    gradesList.add(grade);
                    listItems[i] = grade.getGrade();
                    i++;
                }
            }
        });
        tvItemsSelected = (TextView) findViewById(R.id.tvItemsSelected);
        listItems = getResources().getStringArray(R.array.grade_item);

        //picking formulas elements
        CollectionReference formulasCollectionref = fStore.collection("formula");
        formulasCollectionref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int i=0;
                for(QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots){
                    Formula formula = queryDocumentSnapshot.toObject(Formula.class);
                    formula.setDocumentID(queryDocumentSnapshot.getId());
                    formulasList.add(formula);
                    listItemsFormula[i] = "Formule " + formula.getFormula_name() + " - Prix/mois : " + formula.getPrice() + "€";
                    i++;

                }
            }
        });
        tvItemsFormulaSelected = (TextView) findViewById(R.id.tvItemsFormulaSelected);
        listItemsFormula = getResources().getStringArray(R.array.formula);

    }

    private void switchToLogin() {
        ((TextView) findViewById(R.id.tvLoginLink)).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(RegisterStudentActivity.this, LoginActivity.class);
                RegisterStudentActivity.this.startActivity(loginIntent);
            }
        });
    }

    private void verifyUserLogged() {
        if(fAuth.getCurrentUser() != null && fAuth.getCurrentUser().isEmailVerified()){
            startActivity(new Intent(RegisterStudentActivity.this, MainActivity.class));
            finish();
        }
    }

    private void registerUser() {
        ((Button) findViewById(R.id.bRegister)).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                final String name = etName.getText().toString();
                final String username = etUsername.getText().toString();
                final Integer age = Integer.parseInt(etAge.getText().toString());
                final String address = etAddress.getText().toString();
                String password = etPassword.getText().toString().trim();
                final String email = etEmail.getText().toString().trim();

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
                                        Toast.makeText(RegisterStudentActivity.this, getResources().getString(R.string.CreatedUser), Toast.LENGTH_SHORT).show();
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
                                        user.put("grades", mUserItems);
                                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "onSuccess : user Profile is created for " + userID);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure : " + e.toString());
                                                return;
                                            }
                                        });



                                        //Create User Role
                                        final DocumentReference documentReferenceStudent = fStore.collection("students").document(userID);
                                        final Map<String, Object> studentObject = new HashMap<>();
                                        //initalize formula
                                        final Map<String, Object> formulaObject = new HashMap<>();
                                        formulaObject.put("formula_choisi", mUserItemsFormula.get(0));
                                        Date dt = new Date();
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(dt);
                                        c.add(Calendar.MONTH, 1);
                                        dt = c.getTime();
                                        formulaObject.put("date_fin", new Timestamp(dt));
                                        studentObject.put("subscription", formulaObject);
                                        studentObject.put("parentReference", null);
                                        documentReferenceStudent.set(studentObject).addOnSuccessListener(new OnSuccessListener<Void>() {
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

                                        startActivity(new Intent(RegisterStudentActivity.this, LoginActivity.class));
                                        finish();
                                    }else{
                                        Toast.makeText(RegisterStudentActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(RegisterStudentActivity.this, getResources().getString(R.string.CreatedUserError) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    private void onPickingGrade() {
        ((Button) findViewById(R.id.bPickingGrade)).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(RegisterStudentActivity.this);
                mBuilder.setTitle(getResources().getString(R.string.bSchoolYears));
                final int[] index = new int[1];// to avoid global variable
                mBuilder.setSingleChoiceItems(listItems, checkedItem, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        Toast.makeText(RegisterStudentActivity.this, getResources().getString(R.string.OptionMessageRegisterStudentFragment).toString() +  listItems[position].toString(), Toast.LENGTH_SHORT).show();
                        index[0] = position;
                        checkedItem = position+1;
                    }
                });

                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        String item = listItems[index[0]].toString();
                        mUserItems.add(gradesList.get(index[0]));
                        tvItemsSelected.setText(item);
                    }
                });

                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
            }
        });
    }

    private void onPickingFormula(){
        ((Button) findViewById(R.id.bPickingFormula)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(RegisterStudentActivity.this);
                mBuilder.setTitle(getResources().getString(R.string.bFormulas_string));
                final int[] index = new int[1];
                mBuilder.setSingleChoiceItems(listItemsFormula, checkedItemFormula, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(RegisterStudentActivity.this, getResources().getString(R.string.OptionMessageRegisterStudentFragment).toString() +  listItemsFormula[which].toString(), Toast.LENGTH_SHORT).show();
                        index[0] = which;
                        checkedItemFormula = which+1;

                    }
                });

                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String item = listItemsFormula[index[0]].toString();
                        mUserItemsFormula.add(formulasList.get(index[0]));
                        tvItemsFormulaSelected.setText(item);
                    }
                });
                AlertDialog mDialog = mBuilder.create();
                mDialog.show();

            }
        });
    }




}
