package com.example.onlineschool.StudentFragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

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
import com.example.onlineschool.MainActivity;
import com.example.onlineschool.Models.Formula;
import com.example.onlineschool.Models.Grade;
import com.example.onlineschool.Models.Student;
import com.example.onlineschool.Models.Subscription;
import com.example.onlineschool.Models.User;
import com.example.onlineschool.R;
import com.example.onlineschool.RegisterStudentActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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
 * Use the {@link StudentProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StudentProfileFragment extends Fragment {

    private static final String TAG ="StudentProfileFragment : ";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    //attributes db
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;

    //attributes view elements
    private EditText etName;
    private EditText etUsername;
    private EditText etAge;
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
    private TextView tvDateFin;


    public StudentProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StudentProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StudentProfileFragment newInstance() {
        StudentProfileFragment fragment = new StudentProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_student_profile, container, false);
        verifyUserLogged();
        init(view);
        onModifyProfile(view);
        onPickingGrade(view);
        onPickingFormula(view);
        onExtendSubscription(view);
        logOut(view);
        return view;
    }


    private void init(View view){
        //initialize db
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        //initialize view elements
        etAge = (EditText) view.findViewById(R.id.etAge);
        etName = (EditText) view.findViewById(R.id.etName);
        etUsername = (EditText) view.findViewById(R.id.etUsername);
        etPassword = (EditText) view.findViewById(R.id.etPassword);
        etAddress = (EditText) view.findViewById((R.id.etAddress));
        etEmail = (EditText) view.findViewById(R.id.etEmail);

        //initialize grade list
        //picking grade elements
        CollectionReference collectionGradesRef = fStore.collection("grade");
        collectionGradesRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        int i=0;
                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            Grade grade = documentSnapshot.toObject(Grade.class);
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
        tvDateFin =(TextView) view.findViewById(R.id.tvEndOfSubscription);
        //initialize user id
        userID = fAuth.getCurrentUser().getUid();


        //initialize
        final DocumentReference usersReference = fStore.collection("users").document(userID);
        usersReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot != null){
                    int age = documentSnapshot.getLong("age").intValue();
                    etAge.setText(String.valueOf(age));
                    etName.setText(documentSnapshot.getString("name"));
                    etUsername.setText(documentSnapshot.getString("username"));
                    etAddress.setText(documentSnapshot.getString("address"));
                    etEmail.setText(documentSnapshot.getString("email"));

                    String data = "";
                    User user = documentSnapshot.toObject(User.class);

                    for(Grade grade : user.getGrades()){
                        data += grade.getGrade() + " ";
                        checkedItem = grade.getId();
                    }
                    tvItemsSelected.setText(data);
                }else{
                    return;
                }
            }
        });

        DocumentReference studentDocRef = fStore.collection("students").document(userID);
        studentDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {

            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    System.err.println("Erreur de listener : " + e );
                    return;
                }

                if(documentSnapshot != null) {
                    Student student = documentSnapshot.toObject(Student.class);
                    Subscription s = student.getSubscription();
                    String data = s.getFormula_choisi().getFormula_name();
                    tvItemsFormulaSelected.setText(data);
                    if(s.getDate_fin().toDate().before(new Date())){
                        tvDateFin.setText("Date d'expiration : " + "Expiré");
                    }else{
                        tvDateFin.setText("Date d'expiration : " + s.getDate_fin().toDate().toString());
                    }
                    checkedItemFormula = 0;
                }
            }
        });
    }

    private void verifyUserLogged(){
        if(FirebaseAuth.getInstance().getCurrentUser() == null || !FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

    private void logOut(View view){
        ((Button) view.findViewById(R.id.bLogout)).setOnClickListener(new View.OnClickListener(){

            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v){
                AuthUI.getInstance().signOut(getActivity()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        getActivity().finish();
                    }
                });
            }
        });
    }

    private void onModifyProfile(View view){
        ((Button) view.findViewById(R.id.ModifyProfile)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = etName.getText().toString();
                final String username = etUsername.getText().toString();
                final Integer age = Integer.parseInt(etAge.getText().toString());
                final String address = etAddress.getText().toString();
                final String email = etEmail.getText().toString().trim();

                if(username == "" || name == "" || TextUtils.isEmpty(age + "")){
                    String messageError = getResources().getString(R.string.messageRegisterFillError);
                    if(username == "")
                        etUsername.setError(messageError);

                    if(name == ""){
                        etName.setError(messageError);
                    }

                    if((age + "") == "")
                        etAge.setError(messageError);

                    return;
                }

                final DocumentReference studentDocRef = fStore.collection("users").document(userID);

                final Map<String, Object> user = new HashMap<>();
                user.put("name", name);
                user.put("username", username);
                user.put("email", email);
                user.put("age", age);
                user.put("address", address);

                //Create grade list
                DocumentReference gradeRef = fStore.collection("grade").document(Integer.toString(checkedItem));
                gradeRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(e != null){
                            System.err.println("Erreur de listener : " + e);
                            return;
                        }

                        if(documentSnapshot != null){
                            Grade g = documentSnapshot.toObject(Grade.class);
                            g.setId(Integer.parseInt(documentSnapshot.getId()));
                            mUserItems.add(g);
                            user.put("grades", mUserItems);
                            studentDocRef.update(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @SuppressLint("LongLogTag")
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess : user Profile is modified for " + userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @SuppressLint("LongLogTag")
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure : " + e.toString());
                                }
                            });
                        }
                    }
                });

                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
            }
        });
    }


    private void onPickingGrade(View view) {
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
                        checkedItem = position+1;
                    }
                });

                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        String item = listItems[index[0]].toString();
                        //mUserItems.add(gradesList.get(index[0]));
                        tvItemsSelected.setText(item);
                    }
                });

                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
            }
        });
    }

    private void onPickingFormula(View view) {
        ((Button) view.findViewById(R.id.bPickingFormula)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                mBuilder.setTitle(getResources().getString(R.string.bFormulas_string));
                final int[] index = new int[1];
                mBuilder.setSingleChoiceItems(listItemsFormula, checkedItemFormula, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.OptionMessageRegisterStudentFragment).toString() + listItemsFormula[which].toString(), Toast.LENGTH_SHORT).show();
                        index[0] = which;
                        checkedItemFormula = which + 1;

                    }
                });

                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String item = listItemsFormula[index[0]].toString();
                        mUserItemsFormula.add(formulasList.get(index[0]));
                        tvItemsFormulaSelected.setText(item);

                        //extend
                        final DocumentReference documentReferenceStudent = fStore.collection("students").document(userID);
                        documentReferenceStudent.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Student student = documentSnapshot.toObject(Student.class);
                                Subscription subscription = new Subscription();
                                //initalize formula
                                subscription.setFormula_choisi(mUserItemsFormula.get(0));
                                //expired
                                if(student.getSubscription().getDate_fin().toDate().before(new Date())){
                                    Date dt = new Date();
                                    Calendar c = Calendar.getInstance();
                                    c.setTime(dt);
                                    c.add(Calendar.MONTH, 1);
                                    dt = c.getTime();
                                    subscription.setDate_fin(new Timestamp(dt));

                                }else{
                                    //inscrease incremente current month
                                    if(student.getSubscription().getFormula_choisi().equals(mUserItemsFormula)){
                                        Date dt = student.getSubscription().getDate_fin().toDate();
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(dt);
                                        c.add(Calendar.MONTH, 1);
                                        dt = c.getTime();
                                        subscription.setDate_fin(new Timestamp(dt));
                                    }else{
                                        Date dt = new Date();
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(dt);
                                        c.add(Calendar.MONTH, 1);
                                        dt = c.getTime();
                                        subscription.setDate_fin(new Timestamp(dt));
                                    }
                                }

                                student.setSubscription(subscription);
                                documentReferenceStudent.set(student).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @SuppressLint("LongLogTag")
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess : student Profile is created for " + userID);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @SuppressLint("LongLogTag")
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure : " + e.toString());
                                    }
                                });
                            }
                        });
                    }
                });
                AlertDialog mDialog = mBuilder.create();
                mDialog.show();

            }
        });
    }

    private void onExtendSubscription(View view){
        ((Button) view.findViewById(R.id.bExtend)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DocumentReference documentReferenceStudent = fStore.collection("students").document(userID);
                documentReferenceStudent.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Student student = documentSnapshot.toObject(Student.class);
                        Subscription subscription = student.getSubscription();
                        //initalize formula
                        final Map<String, Object> formulaObject = new HashMap<>();
                        //formulaObject.put("formula_choisi", student.getSubscription().getFormula_choisi());
                        //expired
                        if(student.getSubscription().getDate_fin().toDate().before(new Date())){
                            Date dt = new Date();
                            Calendar c = Calendar.getInstance();
                            c.setTime(dt);
                            c.add(Calendar.MONTH, 1);
                            dt = c.getTime();
                            subscription.setDate_fin(new Timestamp(dt));
                            //formulaObject.put("date_fin", new Timestamp(dt));
                        }else{
                            Date dt = student.getSubscription().getDate_fin().toDate();
                            Calendar c = Calendar.getInstance();
                            c.setTime(dt);
                            c.add(Calendar.MONTH, 1);
                            dt = c.getTime();
                            subscription.setDate_fin(new Timestamp(dt));
                            //formulaObject.put("date_fin", new Timestamp(dt));
                        }
                        student.setSubscription(subscription);
                        //studentObject.put("subscription", formulaObject);
                        documentReferenceStudent.set(student).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @SuppressLint("LongLogTag")
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess : student Profile is created for " + userID);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @SuppressLint("LongLogTag")
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure : " + e.toString());
                            }
                        });
                    }
                });
            }
        });
    }
}
