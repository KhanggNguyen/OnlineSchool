package com.example.onlineschool.StudentFragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.onlineschool.LoginActivity;
import com.example.onlineschool.Models.ActivityHistory;
import com.example.onlineschool.Models.Chapter;
import com.example.onlineschool.Models.Grade;
import com.example.onlineschool.Models.Lesson;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LessonFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LessonFragment extends Fragment {
    public static final String TAG = "LessonFragment  : ";
    //declare db variables
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private String userID;
    private User user;
    private Grade userGrade;

    //current lesson
    Lesson lesson;
    private String lessonIdString;

    //Declare cardview variable
    private CardView cvQuiz, cvSheet, cvExercices, cvVideo;

    private Button bPrevious;

    private TextView tvTitle;


    public LessonFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LessonFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LessonFragment newInstance(String param1, String param2) {
        LessonFragment fragment = new LessonFragment();
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
        View view =  inflater.inflate(R.layout.fragment_lesson, container, false);
        verifyUserLogged();
        init(view);
        OnClickExercices(view);
        OnClickLessonSheet(view);
        OnClickVideo(view);
        OnClickQuiz(view);
        return view;
    }

    private void init(View view) {
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fUser = fAuth.getCurrentUser();
        userID = fAuth.getCurrentUser().getUid();

        cvSheet = view.findViewById(R.id.lesson_sheet);
        cvExercices = view.findViewById(R.id.exercices);
        cvQuiz = view.findViewById(R.id.quiz);
        cvVideo = view.findViewById(R.id.video);

        tvTitle = view.findViewById(R.id.title);

        bPrevious = view.findViewById(R.id.previous_page);

        //get course pass in parameter
        Bundle bundle = getArguments();
        if(bundle != null){
            lessonIdString = bundle.getString("lessonId");
        }

        final DocumentReference currentLessonReference = fStore.collection("courses_sheet").document(lessonIdString);
        currentLessonReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

                    lesson.getCourseRef().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            final Chapter chapter = documentSnapshot.toObject(Chapter.class);
                            //retour
                            bPrevious.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    LessonsListFragment lessonsListFragment = new LessonsListFragment();
                                    //set chapter clicked in parameter
                                    Bundle bundle = new Bundle();
                                    bundle.putString("chapter", chapter.getTitle());
                                    lessonsListFragment.setArguments(bundle);
                                    //change fragment on click
                                    FragmentManager fragmentManager = getFragmentManager();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.fragment_container, lessonsListFragment);
                                    fragmentTransaction.commit();
                                }
                            });
                        }
                    });
                }
            }
        });

    }

    private void OnClickLessonSheet(View view){
        ((CardView) view.findViewById(R.id.lesson_sheet)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LessonContentFragment selectedFragment = new LessonContentFragment().newInstance();
                FragmentManager fragmentManager = getFragmentManager();
                //set course clicked in parameter
                Bundle bundle = new Bundle();
                bundle.putString("lessonId", lesson.getDocumentId());
                selectedFragment.setArguments(bundle);

                //search if this activity is already exists
                CollectionReference activityColRef = fStore.collection("activity_history");
                activityColRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        boolean activity_exists = false;
                        for(QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots){
                            ActivityHistory activity_tempo = queryDocumentSnapshot.toObject(ActivityHistory.class);
                            activity_tempo.setDocumentId(queryDocumentSnapshot.getId());
                            if(activity_tempo.getLessonId().equals(lesson.getDocumentId()) && activity_tempo.isLessonSheet()){
                                activity_tempo.setActivityTime(new Timestamp(new Date()));
                                activity_exists = true;
                                DocumentReference activityDocRef = fStore.collection("activity_history").document(activity_tempo.getDocumentId());
                                activityDocRef.set(activity_tempo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess : activity added for " + userID);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure : " + e.toString());
                                    }
                                });

                            }
                        }
                        if(!activity_exists){
                            //add activity history
                            DocumentReference activityDocRef = fStore.collection("activity_history").document();
                            ActivityHistory activity = new ActivityHistory();
                            activity.setStudentId(userID);
                            activity.setLessonId(lesson.getDocumentId());
                            activity.setLessonSheet(true);
                            activity.setActivityTime(new Timestamp(new Date()));
                            activityDocRef.set(activity).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess : activity added for " + userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure : " + e.toString());
                                }
                            });
                        }

                    }
                });

                //change fragment
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
                fragmentTransaction.commit();
            }
        });
    }

    private void OnClickExercices(View view){
        ((CardView) view.findViewById(R.id.exercices)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExerciceContentFragment selectedFragment = new ExerciceContentFragment().newInstance();
                FragmentManager fragmentManager = getFragmentManager();
                //set course clicked in parameter
                Bundle bundle = new Bundle();
                bundle.putString("lessonId", lesson.getDocumentId());
                selectedFragment.setArguments(bundle);

                //add activity history
                DocumentReference activityDocRef = fStore.collection("activity_history").document();
                ActivityHistory activity = new ActivityHistory();
                activity.setStudentId(userID);
                activity.setLessonId(lesson.getDocumentId());
                activity.setExercice(true);
                activity.setActivityTime(new Timestamp(new Date()));
                activityDocRef.set(activity).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess : activity added for " + userID);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure : " + e.toString());
                    }
                });

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
                fragmentTransaction.commit();

            }
        });
    }

    private void OnClickVideo(View view){
        ((CardView) view.findViewById(R.id.video)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoContentFragment selectedFragment = new VideoContentFragment().newInstance();
                FragmentManager fragmentManager = getFragmentManager();
                //set course clicked in parameter
                Bundle bundle = new Bundle();
                bundle.putString("lessonId", lesson.getDocumentId());
                selectedFragment.setArguments(bundle);

                //add activity history
                DocumentReference activityDocRef = fStore.collection("activity_history").document();
                ActivityHistory activity = new ActivityHistory();
                activity.setStudentId(userID);
                activity.setLessonId(lesson.getDocumentId());
                activity.setVideo(true);
                activity.setActivityTime(new Timestamp(new Date()));
                activityDocRef.set(activity).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess : activity added for " + userID);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure : " + e.toString());
                    }
                });

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
                fragmentTransaction.commit();

            }
        });
    }

    private void OnClickQuiz(View view){
        ((CardView) view.findViewById(R.id.quiz)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuizContentFragment selectedFragment = new QuizContentFragment().newInstance();
                FragmentManager fragmentManager = getFragmentManager();
                //set course clicked in parameter
                Bundle bundle = new Bundle();
                bundle.putString("lessonId", lesson.getDocumentId());
                selectedFragment.setArguments(bundle);

                //add activity history
                DocumentReference activityDocRef = fStore.collection("activity_history").document();
                ActivityHistory activity = new ActivityHistory();
                activity.setStudentId(userID);
                activity.setLessonId(lesson.getDocumentId());
                activity.setQuiz(true);
                activity.setActivityTime(new Timestamp(new Date()));
                activityDocRef.set(activity).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess : activity added for " + userID);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure : " + e.toString());
                    }
                });

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
                fragmentTransaction.commit();

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
