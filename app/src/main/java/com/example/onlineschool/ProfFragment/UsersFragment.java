package com.example.onlineschool.ProfFragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.onlineschool.Adapter.UsersAdapter;
import com.example.onlineschool.LoginActivity;
import com.example.onlineschool.Models.User;
import com.example.onlineschool.R;
import com.example.onlineschool.StudentFragment.LessonFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UsersFragment extends Fragment {
    //declare db variables
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private String userID;
    private User user;
    private Button bPrevious;
    private RecyclerView recyclerView;

    private UsersAdapter userAdapter;
    private List<User> mUsers;

    EditText search_users;

    public UsersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UsersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UsersFragment newInstance() {
        UsersFragment fragment = new UsersFragment();
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
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        verifyUserLogged();
        init(view);
        return view;
    }

    private void init(View view) {
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fUser = fAuth.getCurrentUser();
        userID = fUser.getUid();

        bPrevious = view.findViewById(R.id.previous_page);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        bPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeProfFragment homeProfFragment = new HomeProfFragment();
                //change fragment on click
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, homeProfFragment);
                fragmentTransaction.commit();
            }
        });

        mUsers = new ArrayList<>();

        readUsers();

        search_users = view.findViewById(R.id.search_users);

        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }




    private void verifyUserLogged(){
        if(FirebaseAuth.getInstance().getCurrentUser() == null || !FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

    private void readUsers() {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        CollectionReference usersCollectionRef = fStore.collection("users");
        usersCollectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(search_users.getText().toString().equals("")) {
                    mUsers.clear();

                    for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                        User user = queryDocumentSnapshot.toObject(User.class);
                        user.setDocumentId(queryDocumentSnapshot.getId());
                        if (!user.getDocumentId().equals(userID)) {
                            mUsers.add(user);
                        }

                    }

                    userAdapter = new UsersAdapter(getContext(), mUsers, false);
                    recyclerView.setAdapter(userAdapter);
                }

            }
        });


    }

    private void searchUsers(String s) {
        CollectionReference usersCollectionRef = fStore.collection("users");

        usersCollectionRef.orderBy("search").startAt(s).endAt(s+"\uf8ff").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                mUsers.clear();

                assert queryDocumentSnapshots != null;
                for(QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots){
                    User user = queryDocumentSnapshot.toObject(User.class);

                    assert user != null;
                    assert userID != null;
                    if(!user.getDocumentId().equals(userID)){
                        mUsers.add(user);
                    }
                }

                userAdapter = new UsersAdapter(getActivity(), mUsers, false);
                recyclerView.setAdapter(userAdapter);
            }
        });


    }
}
