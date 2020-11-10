package com.example.onlineschool.StudentFragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.onlineschool.LoginActivity;
import com.example.onlineschool.Models.Lesson;
import com.example.onlineschool.Models.User;
import com.example.onlineschool.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LessonContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LessonContentFragment extends Fragment {
    //declare db variables
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private String userID;
    private User user;

    private TextView tvTitle, tvContent;

    private Button bPrevious;

    //current lesson
    private Lesson lesson;
    private String lessonIdString;

    public LessonContentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     
     * @return A new instance of fragment LessonContentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LessonContentFragment newInstance() {
        LessonContentFragment fragment = new LessonContentFragment();
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
        View view = inflater.inflate(R.layout.fragment_lesson_content, container, false);

        verifyUserLogged();

        init(view);
        return view;
    }

    private void init(View view) {
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fUser = fAuth.getCurrentUser();
        userID = fAuth.getCurrentUser().getUid();

        tvTitle = view.findViewById(R.id.title);
        tvContent = view.findViewById(R.id.content);

        bPrevious = view.findViewById(R.id.previous_page);

        //get course pass in parameter
        Bundle bundle = getArguments();
        if(bundle != null){
            lessonIdString = bundle.getString("lessonId");
        }

        DocumentReference lessonRef = fStore.collection("courses_sheet").document(lessonIdString);
        lessonRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null ){
                    System.err.println("Erreur de listener : " + e);
                    return;
                }

                if(documentSnapshot != null){
                    lesson = documentSnapshot.toObject(Lesson.class);
                    lesson.setDocumentId(lessonIdString);

                    tvTitle.setText(lesson.getTitle());
                    tvContent.setText(lesson.getContent());

                    bPrevious.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LessonFragment lessonFragment = new LessonFragment();
                            //set chapter clicked in parameter
                            Bundle bundle = new Bundle();
                            bundle.putString("lessonId", lesson.getDocumentId());
                            lessonFragment.setArguments(bundle);
                            //change fragment on click
                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, lessonFragment);
                            fragmentTransaction.commit();
                        }
                    });
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
}
