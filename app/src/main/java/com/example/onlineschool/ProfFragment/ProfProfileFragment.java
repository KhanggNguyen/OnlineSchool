package com.example.onlineschool.ProfFragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.onlineschool.LoginActivity;
import com.example.onlineschool.MainActivity;
import com.example.onlineschool.Models.Grade;
import com.example.onlineschool.Models.User;
import com.example.onlineschool.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfProfileFragment extends Fragment {
    private static final String TAG ="ProfProfileFragment : ";

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

    public ProfProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.

     * @return A new instance of fragment ProfProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfProfileFragment newInstance() {
        ProfProfileFragment fragment = new ProfProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_prof_profile, container, false);
        verifyUserLogged();
        init(view);
        onModifyProfile(view);
        logOut(view);
        return view;
    }

    private void init(View view) {
        //initialize db
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        //initialize user id
        userID = fAuth.getCurrentUser().getUid();

        //initialize view elements
        etAge = (EditText) view.findViewById(R.id.etAge);
        etName = (EditText) view.findViewById(R.id.etName);
        etUsername = (EditText) view.findViewById(R.id.etUsername);
        etPassword = (EditText) view.findViewById(R.id.etPassword);
        etAddress = (EditText) view.findViewById((R.id.etAddress));
        etEmail = (EditText) view.findViewById(R.id.etEmail);

        //initialize
        final DocumentReference usersReference = fStore.collection("users").document(userID);
        usersReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null ) {
                    System.err.println("Erreur de listener " + e );
                    return ;
                }
                if(documentSnapshot != null){
                    int age = documentSnapshot.getLong("age").intValue();
                    etAge.setText(String.valueOf(age));
                    etName.setText(documentSnapshot.getString("name"));
                    etUsername.setText(documentSnapshot.getString("username"));
                    etAddress.setText(documentSnapshot.getString("address"));
                    etEmail.setText(documentSnapshot.getString("email"));
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
                user.put("age", age);
                user.put("address", address);

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

                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
            }
        });
    }
}
