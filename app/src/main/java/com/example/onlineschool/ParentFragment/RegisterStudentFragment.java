package com.example.onlineschool.ParentFragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onlineschool.LoginActivity;
import com.example.onlineschool.Models.Formula;
import com.example.onlineschool.Models.Grade;
import com.example.onlineschool.Models.Parent;
import com.example.onlineschool.Models.User;
import com.example.onlineschool.R;
import com.example.onlineschool.RegisterStudentActivity;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterStudentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterStudentFragment extends Fragment {
    public static final String TAG = "Register Student : ";

    //Currently user
    private String userID;
    private User currentUser;

    //student id
    private String studentID;

    //Database variable;
    private FirebaseAuth fAuth;
    private FirebaseAuth fAuthStudent;
    private FirebaseFirestore fStore;

    //view elements
    private EditText etUsername;
    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etAge;

    //elements for picking interested grades
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

    public RegisterStudentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment RegisterStudentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterStudentFragment newInstance() {
        RegisterStudentFragment fragment = new RegisterStudentFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_student, container, false);
        verifyUserLogged();
        init(view);
        registerStudent(view);
        onPickingGrade(view);
        onPickingFormula(view);
        return view;
    }

    private void init(View view){
        //initialize db
        this.fAuth = FirebaseAuth.getInstance();
        this.fAuthStudent = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();

        //Currently user
        userID = fAuth.getCurrentUser().getUid();

        //initialize view elements
        etAge = (EditText) view.findViewById(R.id.etAge);
        etName = (EditText) view.findViewById(R.id.etName);
        etUsername = (EditText) view.findViewById(R.id.etUsername);
        etEmail = (EditText) view.findViewById(R.id.etEmail);
        etPassword = (EditText) view.findViewById(R.id.etPassword);

        //initialize user
        DocumentReference userReference = fStore.collection("users").document(userID);
        userReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    currentUser = documentSnapshot.toObject(User.class);
                }
            }
        });

        //picking grade elements
        CollectionReference collectionGradesRef = fStore.collection("grade");
        collectionGradesRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
        tvItemsSelected = (TextView) view.findViewById(R.id.tvItemsSelected);
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
        tvItemsFormulaSelected = (TextView) view.findViewById(R.id.tvItemsFormulaSelected);
        listItemsFormula = getResources().getStringArray(R.array.formula);
    }

    /**
     * If user is not logged in or their Email is not verifed, switch them to login
     */
    private void verifyUserLogged(){
        if(FirebaseAuth.getInstance().getCurrentUser() == null || !FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

    private void registerStudent(View view){
        ((Button) view.findViewById(R.id.bRegister)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = etName.getText().toString();
                final String username = etUsername.getText().toString();
                final Integer age;
                if(!TextUtils.isEmpty(etAge.getText().toString())){
                     age = Integer.parseInt(etAge.getText().toString());
                }else{
                    age = null;
                }

                final String password = etPassword.getText().toString().trim();
                final String email = etEmail.getText().toString().trim();

                if(username.equals("") || name.equals("") || age == null || password.equals("")){
                    String messageError = getResources().getString(R.string.messageRegisterFillError);

                    if(username.equals(""))
                        etUsername.setError(messageError);

                    if(name.equals("")){
                        etName.setError(messageError);
                    }

                    if(age == null)
                        etAge.setError(messageError);

                    if(password.equals(""))
                        etPassword.setError(messageError);

                    if(password.length() < 6)
                        etPassword.setError(getResources().getString(R.string.messageErrorPasswordLength));

                    return;
                }

                fAuthStudent.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            fAuthStudent.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(getActivity(), getResources().getString(R.string.CreatedUser), Toast.LENGTH_SHORT).show();
                                        studentID = fAuthStudent.getCurrentUser().getUid();

                                        //Create user in collections users
                                        DocumentReference studentReference = fStore.collection("users").document(studentID);
                                        Map<String, Object> userMap = new HashMap<>();
                                        userMap.put("name", name);
                                        userMap.put("username", username);
                                        userMap.put("search", username.toLowerCase());
                                        userMap.put("email", email);
                                        userMap.put("age", age);
                                        userMap.put("address", currentUser.getAddress());
                                        //Create grade list
                                        userMap.put("grades", mUserItems);

                                        studentReference.set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
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

                                        //Create User Role
                                        final DocumentReference documentReferenceParent = fStore.collection("parents").document(userID);
                                        final DocumentReference documentReferenceStudent = fStore.collection("students").document(studentID);

                                        //add parent ref to student
                                        Map<String, Object> student = new HashMap<>();
                                        //initalize formula
                                        final Map<String, Object> formulaObject = new HashMap<>();
                                        formulaObject.put("formula_choisi", mUserItemsFormula.get(0));
                                        Date dt = new Date();
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(dt);
                                        c.add(Calendar.MONTH, 1);
                                        dt = c.getTime();
                                        formulaObject.put("date_fin", new Timestamp(dt));
                                        student.put("subscription", formulaObject);
                                        student.put("parentReference", documentReferenceParent);
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

                                        //add student ref to parent

                                        documentReferenceParent.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                Parent parent = documentSnapshot.toObject(Parent.class);
                                                List<DocumentReference> studentRef = parent.getStudentsRef();
                                                studentRef.add(documentReferenceStudent);
                                                parent.setStudentsRef(studentRef);
                                                documentReferenceParent.set(parent).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "onSuccess : student reference is added to list for " + userID);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d(TAG, "onFailure : " + e.toString());
                                                    }
                                                });
                                            }
                                        });
                                        ParentProfileFragment parentProfileFragment = new ParentProfileFragment();
                                        FragmentManager fragmentManager = getFragmentManager();
                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                        fragmentTransaction.replace(R.id.fragment_container, parentProfileFragment);
                                        fragmentTransaction.commit();

                                    }else{
                                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(getActivity(), getResources().getString(R.string.CreatedUserError) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void onPickingGrade(View view){
        ((Button) view.findViewById(R.id.bPickingGrade)).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                mBuilder.setTitle(getResources().getString(R.string.bSchoolYears));
                final int[] index = new int[1];// to avoid global variable
                mBuilder.setSingleChoiceItems(listItems, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.OptionMessageRegisterStudentFragment).toString() +  listItems[position].toString(), Toast.LENGTH_SHORT).show();
                        index[0] = position;
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

    private void onPickingFormula(View view){
        ((Button) view.findViewById(R.id.bPickingFormula)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                mBuilder.setTitle(getResources().getString(R.string.bFormulas_string));
                final int[] index = new int[1];
                mBuilder.setSingleChoiceItems(listItemsFormula, checkedItemFormula, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.OptionMessageRegisterStudentFragment).toString() +  listItemsFormula[which].toString(), Toast.LENGTH_SHORT).show();
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
