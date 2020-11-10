package com.example.onlineschool.StudentFragment;

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
import com.example.onlineschool.Models.Chat;
import com.example.onlineschool.Models.ChatList;
import com.example.onlineschool.Models.Student;
import com.example.onlineschool.Models.Subscription;
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
 * Use the {@link StudentChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StudentChatFragment extends Fragment {
    private static final String TAG = "StudentChatFragment ***:";

    CircleImageView profile_image;
    TextView username;

    //declare db variables
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private String userID;
    private String profID;

    Button bPrevious;
    ImageButton btn_send;
    EditText text_send;

    RecyclerView recyclerView;
    MessagesAdapter messagesAdapter;
    List<Chat> mChat;

    public StudentChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StudentChatFragment newInstance() {
        StudentChatFragment fragment = new StudentChatFragment();
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
        View view = inflater.inflate(R.layout.fragment_student_chat, container, false);
        verifyIfUserSubscription(view);
        init(view);
        return view;
    }



    private void init(View view) {
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        bPrevious = view.findViewById(R.id.previous_page);
        btn_send = view.findViewById(R.id.btn_send);
        text_send = view.findViewById(R.id.text_send);

        recyclerView = view.findViewById(R.id.recycler_student_chat);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        CollectionReference profCollectionRef = fStore.collection("professeurs");
        profCollectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots){
                    final User prof = queryDocumentSnapshot.toObject(User.class);
                    prof.setDocumentId(queryDocumentSnapshot.getId());
                    profID = queryDocumentSnapshot.getId();

                    btn_send.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String msg = text_send.getText().toString();
                            if(!msg.equals("")){
                                SendMessage(userID, prof.getDocumentId(), msg);
                            }else{
                                Toast.makeText(getActivity(), "Vous ne pouvez pas envoyer un message vide", Toast.LENGTH_SHORT).show();
                            }
                            text_send.setText("");
                        }
                    });

                    SeenMessage(prof.getDocumentId());
                }
            }
        });

        bPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeStudentFragment selectedFragment = new HomeStudentFragment();

                //change fragment on click
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
                fragmentTransaction.commit();
            }
        });

        ReadMessage(userID);


    }

    private void SeenMessage(final String receiverID){
        CollectionReference chatCollectionRef = fStore.collection("chats");
        chatCollectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null ){
                    System.err.println("Listen failed:" + e);
                    return;
                }

                if(queryDocumentSnapshots != null){
                    for(QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots){
                        Chat chat = queryDocumentSnapshot.toObject(Chat.class);
                        if(chat.getReceiver().equals(userID) && chat.getSender().equals(receiverID)){
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isSeen", true);
                            queryDocumentSnapshot.getReference().update(hashMap);
                        }
                    }
                }
            }
        });

    }

    private void SendMessage(final String sender, final String receiver, String message){
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
    }

    private void ReadMessage(final String myid){
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
                for(QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots){
                    Chat chat = queryDocumentSnapshot.toObject(Chat.class);
                    if(chat.getReceiver().equals(myid) || chat.getSender().equals(myid)){
                        mChat.add(chat);
                    }
                    messagesAdapter = new MessagesAdapter(getActivity(), mChat);
                    recyclerView.setAdapter(messagesAdapter);
                }
            }
        });
    }

    private void verifyIfUserSubscription(View view) {
        DocumentReference studentDocumentRef = FirebaseFirestore.getInstance().collection("students").document(FirebaseAuth.getInstance().getUid());
        studentDocumentRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot != null){
                    Student student = documentSnapshot.toObject(Student.class);

                    assert student != null;
                    Subscription subscription = student.getSubscription();
                    if(!subscription.getFormula_choisi().getFormula_name().equals("Accompagné") || subscription.getDate_fin().toDate().before(new Date())){
                        ChangeSubscriptionNeededFragment selectedFragment = new ChangeSubscriptionNeededFragment().newInstance();
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
                        fragmentTransaction.commit();


                    }
                }
            }
        });
    }
}
