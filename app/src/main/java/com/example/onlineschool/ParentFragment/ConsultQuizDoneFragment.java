package com.example.onlineschool.ParentFragment;

import android.annotation.SuppressLint;
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
import android.widget.TextView;

import com.example.onlineschool.Adapter.MessagesAdapter;
import com.example.onlineschool.Adapter.QuizDoneAdapter;
import com.example.onlineschool.Models.Quiz;
import com.example.onlineschool.Models.Student;
import com.example.onlineschool.Models.User;
import com.example.onlineschool.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConsultQuizDoneFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConsultQuizDoneFragment extends Fragment {
    public static final String TAG = "Parent on student's history ** : ";

    //Currently user
    private String userID;
    private User currentUser;

    private String studentID;

    //Database variable;
    private FirebaseAuth fAuth;
    private FirebaseAuth fAuthStudent;
    private FirebaseFirestore fStore;

    TextView tvTitle;



    RecyclerView recyclerView;
    QuizDoneAdapter quizDoneAdapter;
    ArrayList<Quiz> quizList = new ArrayList<Quiz>();
    public ConsultQuizDoneFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RecentQuizDoneFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConsultQuizDoneFragment newInstance() {
        ConsultQuizDoneFragment fragment = new ConsultQuizDoneFragment();
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
        View view = inflater.inflate(R.layout.fragment_recent_quiz_done, container, false);
        init(view);
        return view;
    }

    private void init(View view){
        //initialize db
        this.fAuth = FirebaseAuth.getInstance();
        this.fAuthStudent = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();

        //Currently user
        userID = fAuth.getCurrentUser().getUid();

        tvTitle = view.findViewById(R.id.title);

        recyclerView = view.findViewById(R.id.recycler_quiz_done);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Bundle bundle = getArguments();
        if(bundle != null){
            studentID = bundle.getString("studentID");
        }

        ((Button) view.findViewById(R.id.previous_page)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParentStudentActionListFragment parentStudentActionListFragment = new ParentStudentActionListFragment();
                Bundle bundle2 = new Bundle();
                bundle2.putString("studentID", studentID);
                parentStudentActionListFragment.setArguments(bundle2);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, parentStudentActionListFragment);
                fragmentTransaction.commit();
            }
        });

        DocumentReference userDocRef = fStore.collection("users").document(studentID);
        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                tvTitle.setText("Les QCM réalisés récemment de " + user.getUsername());
            }
        });

        DocumentReference studentDocRef = fStore.collection("students").document(studentID);
        studentDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Student student = documentSnapshot.toObject(Student.class);
                for(Quiz quiz : student.getQuizDone()){
                    quizList.add(quiz);
                }
                QuizDoneAdapter quizDoneAdapter = new QuizDoneAdapter(getActivity(), quizList);
                recyclerView.setAdapter(quizDoneAdapter);
            }
        });

    }
}
