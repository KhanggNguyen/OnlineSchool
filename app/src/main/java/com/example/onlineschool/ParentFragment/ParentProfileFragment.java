package com.example.onlineschool.ParentFragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.onlineschool.Models.Grade;
import com.example.onlineschool.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

import com.example.onlineschool.Models.User;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ParentProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ParentProfileFragment extends Fragment {
    private static final String TAG = "ParentProfileFragment : ";

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
    private String[] listItems;
    private List<Grade> gradesList = new ArrayList<Grade>();
    private boolean[] checkedItems;
    private List<Grade> mUserItems = new ArrayList<Grade>();
    private TextView tvItemsSelected;


    private List<Grade> mUserGrades = new ArrayList<Grade>();
    private List<Grade> grades;

    public ParentProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ParentProfileFragment newInstance() {
        ParentProfileFragment fragment = new ParentProfileFragment();
        Bundle args = new Bundle();

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
        View view = inflater.inflate(R.layout.fragment_parent_profile, container, false);
        verifyUserLogged();
        init(view);
        registerUser(view);
        onPickingGrade(view);
        addStudent(view);
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
        checkedItems = new boolean[listItems.length];

        //initialize user id
        userID = fAuth.getCurrentUser().getUid();

        verifyUserLogged();

        final DocumentReference usersReference = fStore.collection("users").document(userID);
        usersReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot != null){
                    int age = 0;
                    if(documentSnapshot.getLong("age") != null){
                        age = documentSnapshot.getLong("age").intValue();
                    }

                    etAge.setText(String.valueOf(age));
                    etName.setText(documentSnapshot.getString("name"));
                    etUsername.setText(documentSnapshot.getString("username"));
                    etAddress.setText(documentSnapshot.getString("address"));
                    etEmail.setText(documentSnapshot.getString("email"));

                    String data = "";
                    User user = documentSnapshot.toObject(User.class);

                    for(Grade grade : user.getGrades()){
                        data += grade.getGrade() + ", ";
                    }

                    tvItemsSelected.setText(data);
                }

            }
        });

    }

    private void logOut(View view){
        ((Button) view.findViewById(R.id.bLogout)).setOnClickListener(new View.OnClickListener(){

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

    private void verifyUserLogged(){
        if(FirebaseAuth.getInstance().getCurrentUser() == null || !FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

    /**
     * Initialize button register user
     */
    private void registerUser(View view){
        ((Button) view.findViewById(R.id.ModifyProfile)).setOnClickListener(new View.OnClickListener(){
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

                final DocumentReference userDocRef = fStore.collection("users").document(fAuth.getCurrentUser().getUid());
                final User user = new User();
                user.setAddress(address);
                user.setAge(age);
                user.setEmail(email);
                user.setUsername(username);
                user.setName(name);

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
                        user.setGrades(mUserGrades);

                        userDocRef.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @SuppressLint("LongLogTag")
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getActivity(), "Votre profil a été modifié ", Toast.LENGTH_SHORT).show();
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
                });
            }
        });
    }

    private void addStudent(View view){
        ((Button) view.findViewById(R.id.AddStudent)).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Fragment fragmentRegisterStudent = new RegisterStudentFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragmentRegisterStudent);
                fragmentTransaction.commit();
            }
        });
    }

    private void onPickingGrade(View view){
        ((Button) view.findViewById(R.id.bPickingGrade)).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                mBuilder.setTitle(getResources().getString(R.string.bSchoolYears));
                mBuilder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                        if(isChecked){
                            if(! mUserItems.contains(position)){
                                //mUserItems.add(position);
                                mUserItems.add(gradesList.get(position));
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
                            //item = item + listItems[mUserItems.get(i).getId()];
                            item = item + listItems[mUserItems.get(i).getId()];
                            //grades.add(mUserItems.get(i));
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
