package com.example.onlineschool.ProfFragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onlineschool.Adapter.MessagesAdapter;
import com.example.onlineschool.Adapter.UsersAdapter;
import com.example.onlineschool.Models.Chat;
import com.example.onlineschool.Models.ChatList;
import com.example.onlineschool.Models.User;
import com.example.onlineschool.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfChatFragment extends Fragment {
    private static final String TAG = "ProfChatFragment : ";
    //declare db variables
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private String userID;
    private String targetID;

    private RecyclerView recyclerView;
    private UsersAdapter usersAdapter;
    private List<User> usersList;
    private List<ChatList> usersChatList;
    private MessagesAdapter messagesAdapter;
    private List<Chat> mChat;

    CircleImageView profile_image;
    TextView username;
    private String previous_page;
    Button bPrevious;
    ImageButton btn_send;
    EditText text_send;

    public ProfChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ProfChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfChatFragment newInstance() {
        ProfChatFragment fragment = new ProfChatFragment();
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
        View view = inflater.inflate(R.layout.fragment_prof_chat, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        profile_image = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);
        btn_send = view.findViewById(R.id.btn_send);
        text_send = view.findViewById(R.id.text_send);
        bPrevious = view.findViewById(R.id.previous_page);

        //get studentID from bundle args
        Bundle bundle = getArguments();
        if(bundle != null){
            previous_page = bundle.getString("previous_page");
            targetID = bundle.getString("studentID");
        }

        DocumentReference userDocumentRef = fStore.collection("users").document(targetID);
        userDocumentRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                user.setDocumentId(documentSnapshot.getId());

                username.setText(user.getUsername());
            }
        });

        recyclerView = view.findViewById(R.id.recycler_prof_chat);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image.setImageResource(R.mipmap.ic_launcher);

        bPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment selectedFragment = null;
                if(previous_page.equals("UsersFragment")){
                    selectedFragment = new UsersFragment();
                }
                else{
                    selectedFragment = new ProfChatListFragment();
                }

                //change fragment on click
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
                fragmentTransaction.commit();
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = text_send.getText().toString();
                if(!msg.equals("")){
                    SendMessage(userID, targetID, msg);
                }else{
                    Toast.makeText(getActivity(), "Vous ne pouvez pas envoyer un message vide", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");


            }
        });

        SeenMessage(targetID);

        ReadMessage(userID);

    }

    private void SeenMessage(final String receiverID){
        CollectionReference chatCollectionRef = fStore.collection("chats");
        chatCollectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots){
                    Chat chat = queryDocumentSnapshot.toObject(Chat.class);
                    if(chat.getReceiver().equals(userID) && chat.getSender().equals(receiverID)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", true);
                        queryDocumentSnapshot.getReference().update(hashMap);
                    }
                }
            }
        });

    }

    private void SendMessage(final String sender, final String receiver, String message) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("timeSent", new Timestamp(new Date()));
        hashMap.put("isSeen", false);

        CollectionReference chatCollectionRef = fStore.collection("chats");
        chatCollectionRef.add(hashMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "Message a été envoyé ! ");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Echec de l'envoi du message ! ");
            }
        });

        //add user to chat list if not added
        final CollectionReference chatListCollectionRef = fStore.collection("chatlist");

        chatListCollectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                boolean exist = false;
                for(QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots){
                    ChatList chatList = queryDocumentSnapshot.toObject(ChatList.class);

                    if(chatList.getIdSender().equals(sender) && chatList.getIdReceiver().equals(receiver)){
                        exist = true;
                    }
                }
                if(!exist){
                    HashMap<String, Object> hashMap1 = new HashMap<>();
                    hashMap1.put("idSender", sender);
                    hashMap1.put("idReceiver", receiver);

                    chatListCollectionRef.add(hashMap1).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @SuppressLint("LongLogTag")
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "Chat list a été enregistré! ");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @SuppressLint("LongLogTag")
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Echec de l'envoi du message ! ");
                        }
                    });
                }
            }
        });
        ReadMessage(userID);
    }

    private void ReadMessage(final String myid) {
        mChat = new ArrayList<>();
        CollectionReference chatCollectionRef = fStore.collection("chats");
        chatCollectionRef.orderBy("timeSent", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    System.err.println("Listen failed:" + e);
                    return;
                }

                mChat.clear();
                if(queryDocumentSnapshots != null){
                    for(QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots){
                        Chat chat = queryDocumentSnapshot.toObject(Chat.class);
                        if(chat.getReceiver().equals(myid) && chat.getSender().equals(targetID) || chat.getSender().equals(myid) && chat.getReceiver().equals(targetID)){
                            mChat.add(chat);
                        }
                        messagesAdapter = new MessagesAdapter(getActivity(), mChat);
                        recyclerView.setAdapter(messagesAdapter);
                    }
                }
            }
        });
    }
}
