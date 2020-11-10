package com.example.onlineschool.ProfFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.onlineschool.Adapter.UsersAdapter;
import com.example.onlineschool.Models.ChatList;
import com.example.onlineschool.Models.User;
import com.example.onlineschool.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfChatListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfChatListFragment extends Fragment {
    private static final String TAG = "";
    private RecyclerView recyclerView;
    private UsersAdapter usersAdapter;
    private List<User> mUsers;
    private List<ChatList> usersList;

    private Button bPrevious;
    //declare db variables
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private String userID;



    public ProfChatListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ChatListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfChatListFragment newInstance() {
        ProfChatListFragment fragment = new ProfChatListFragment();
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
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        init(view);

        return view;
    }

    private void init(View view) {
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();



        bPrevious = view.findViewById(R.id.previous_page);

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

        recyclerView = view.findViewById(R.id.recycler_chat_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        usersList = new ArrayList<>();

        CollectionReference ChatCollectionRef = fStore.collection("chatlist");
        ChatCollectionRef.whereEqualTo("idReceiver", userID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>(){
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                usersList.clear();
                for(QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots){
                    ChatList chatList = queryDocumentSnapshot.toObject(ChatList.class);
                    usersList.add(chatList);
                }
                chatList();
            }
        });
    }

    private void chatList() {
        mUsers = new ArrayList<>();

        CollectionReference usersCollectionRef = fStore.collection("users");
        usersCollectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                mUsers.clear();

                for(QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots){
                    User user = queryDocumentSnapshot.toObject(User.class);
                    user.setDocumentId(queryDocumentSnapshot.getId());

                    for(ChatList chatList : usersList ){
                        if(user.getDocumentId().equals(chatList.getIdSender())){

                            mUsers.add(user);
                        }
                    }

                }
                usersAdapter = new UsersAdapter(getActivity(), mUsers, true);
                recyclerView.setAdapter(usersAdapter);

            }
        });

    }
}
