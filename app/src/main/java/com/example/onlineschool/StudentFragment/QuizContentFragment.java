package com.example.onlineschool.StudentFragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.onlineschool.LoginActivity;
import com.example.onlineschool.Models.Lesson;
import com.example.onlineschool.Models.QuestionQuiz;
import com.example.onlineschool.Models.Quiz;
import com.example.onlineschool.Models.Student;
import com.example.onlineschool.Models.User;
import com.example.onlineschool.R;
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
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QuizContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QuizContentFragment extends Fragment {
    //declare db variables
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private String userID;
    private User user;

    private Button bPrevious, bOption1, bOption2, bOption3, bOption4;

    private TextView tvQuestion,tvTotalQuestionNumber, tvCorrectAnswerNumber;

    //curent lesson
    private Lesson lesson;
    private String lessonIdString;

    int totalQuestion = 0;
    int currentQuestion = 1;
    int correct = 0;
    int wrong = 0;

    public QuizContentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment QuizContentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QuizContentFragment newInstance() {
        QuizContentFragment fragment = new QuizContentFragment();
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
        View view = inflater.inflate(R.layout.fragment_quiz_content, container, false);
        verifyUserLogged();
        init(view);
        return view;
    }

    private void init(View view) {
        //initialize variables
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fUser = fAuth.getCurrentUser();
        userID = fAuth.getCurrentUser().getUid();

        tvQuestion = view.findViewById(R.id.questionTxt);
        tvTotalQuestionNumber = view.findViewById(R.id.total_Questions_number);
        tvCorrectAnswerNumber = view.findViewById(R.id.correct_answer_number);

        bPrevious = view.findViewById(R.id.previous_page);
        bOption1 = view.findViewById(R.id.bOption1);
        bOption2 = view.findViewById(R.id.bOption2);
        bOption3 = view.findViewById(R.id.bOption3);
        bOption4 = view.findViewById(R.id.bOption4);

        //get course pass in parameter
        Bundle bundle = getArguments();
        if(bundle != null){
            lessonIdString = bundle.getString("lessonId");
        }

        //get object courses_sheet
        DocumentReference lessonRef = fStore.collection("courses_sheet").document(lessonIdString);
        lessonRef.addSnapshotListener(new EventListener<DocumentSnapshot>(){

            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    System.err.println("Erreur de listener : " + e);
                    return;
                }

                if(documentSnapshot != null){
                    lesson = documentSnapshot.toObject(Lesson.class);
                    lesson.setDocumentId(lessonIdString);

                    //back to previous page
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

                    //Quiz object
                    final CollectionReference quizRef = fStore.collection("quiz");
                    quizRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                final Quiz quiz = documentSnapshot.toObject(Quiz.class);
                                quiz.setDocumentId(documentSnapshot.getId());

                                quiz.getSheetRef().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Lesson lesson_tempo = documentSnapshot.toObject(Lesson.class);
                                        if(lesson_tempo != null){
                                            lesson_tempo.setDocumentId(documentSnapshot.getId());
                                            if(lesson_tempo.getDocumentId().equals(lesson.getDocumentId())){
                                                totalQuestion = quiz.getQuestions().size();

                                                if(totalQuestion == 0){
                                                    tvQuestion.setText("Il n'y a pas de quiz pour cet leçon");
                                                }

                                                tvTotalQuestionNumber.setText(String.valueOf(totalQuestion));
                                                tvCorrectAnswerNumber.setText(String.valueOf(correct));

                                                updateQuestion(quiz);
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateQuestion(final Quiz quiz){
        if(currentQuestion > totalQuestion){
            //end of quiz
            tvQuestion.setText("Fin d'évaluation! Veuillez Cliquer la bouton de retour");
            bOption1.setClickable(false);
            bOption2.setClickable(false);
            bOption3.setClickable(false);
            bOption4.setClickable(false);

            //add quiz to quizDone list of student
            final DocumentReference studentRef = fStore.collection("students").document(userID);
            studentRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Student student = documentSnapshot.toObject(Student.class);
                    if(correct == totalQuestion){
                        List<Quiz> quizDone = student.getQuizDone();
                        //add time when activity done
                        Date date = new Date();
                        Timestamp newTimestamp = new Timestamp(date);
                        quiz.setTimeQuizDone(newTimestamp);

                        //add to list
                        quizDone.add(quiz);
                        student.setQuizDone(quizDone);

                        studentRef.set(student).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("Quiz Content *** :", "onSuccess : Quiz done is added to list for " + userID);
                            }
                        });
                    }
                }
            });

        }else{
            final QuestionQuiz question = quiz.getQuestions().get(currentQuestion-1);
            tvQuestion.setText(String.valueOf(currentQuestion) + ". " + question.getQuestion());
            bOption1.setText(question.getOption1());
            bOption2.setText(question.getOption2());
            bOption3.setText(question.getOption3());
            bOption4.setText(question.getOption4());

            bOption1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentQuestion++;
                    if(bOption1.getText().toString().equals(question.getAnswer())){
                        bOption1.setBackgroundColor(Color.GREEN);//change color to green and change bakc after 1,5s
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                correct++;
                                tvCorrectAnswerNumber.setText(String.valueOf(correct));
                                bOption1.setBackgroundColor(Color.parseColor("#03A9F4"));
                                updateQuestion(quiz);
                            }
                        }, 1500);
                    }else{
                        wrong++;
                        bOption1.setBackgroundColor(Color.RED);
                        /*
                        if(bOption2.getText().toString().equals(question.getAnswer())){
                            bOption2.setBackgroundColor(Color.GREEN);
                        }else if(bOption3.getText().toString().equals(question.getAnswer())){
                            bOption3.setBackgroundColor(Color.GREEN);
                        }else if(bOption4.getText().toString().equals(question.getAnswer())){
                            bOption4.setBackgroundColor(Color.GREEN);
                        }*/

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable(){

                            @Override
                            public void run() {
                                bOption1.setBackgroundColor(Color.parseColor("#03A9F4"));
                                bOption2.setBackgroundColor(Color.parseColor("#03A9F4"));
                                bOption3.setBackgroundColor(Color.parseColor("#03A9F4"));
                                bOption4.setBackgroundColor(Color.parseColor("#03A9F4"));
                                updateQuestion(quiz);
                            }
                        },1500);
                    }
                }
            });

            bOption2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentQuestion++;
                    if(bOption2.getText().toString().equals(question.getAnswer())){
                        bOption2.setBackgroundColor(Color.GREEN);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                correct++;
                                tvCorrectAnswerNumber.setText(String.valueOf(correct));
                                bOption2.setBackgroundColor(Color.parseColor("#03A9F4"));
                                updateQuestion(quiz);
                            }
                        }, 1500);
                    }else{
                        wrong++;
                        bOption2.setBackgroundColor(Color.RED);
                        /*
                        if(bOption1.getText().toString().equals(question.getAnswer())){
                            bOption1.setBackgroundColor(Color.GREEN);
                        }else if(bOption3.getText().toString().equals(question.getAnswer())){
                            bOption3.setBackgroundColor(Color.GREEN);
                        }else if(bOption4.getText().toString().equals(question.getAnswer())){
                            bOption4.setBackgroundColor(Color.GREEN);
                        }*/

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){

                            @Override
                            public void run() {
                                bOption1.setBackgroundColor(Color.parseColor("#03A9F4"));
                                bOption2.setBackgroundColor(Color.parseColor("#03A9F4"));
                                bOption3.setBackgroundColor(Color.parseColor("#03A9F4"));
                                bOption4.setBackgroundColor(Color.parseColor("#03A9F4"));
                                updateQuestion(quiz);

                            }
                        },1500);
                    }
                }
            });

            bOption3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentQuestion++;
                    if(bOption3.getText().toString().equals(question.getAnswer())){
                        bOption3.setBackgroundColor(Color.GREEN);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                correct++;
                                tvCorrectAnswerNumber.setText(String.valueOf(correct));
                                bOption3.setBackgroundColor(Color.parseColor("#03A9F4"));
                                updateQuestion(quiz);
                            }
                        }, 1500);
                    }else{
                        wrong++;
                        bOption3.setBackgroundColor(Color.RED);
                        /*
                        if(bOption2.getText().toString().equals(question.getAnswer())){
                            bOption2.setBackgroundColor(Color.GREEN);
                        }else if(bOption1.getText().toString().equals(question.getAnswer())){
                            bOption1.setBackgroundColor(Color.GREEN);
                        }else if(bOption4.getText().toString().equals(question.getAnswer())){
                            bOption4.setBackgroundColor(Color.GREEN);
                        }*/

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable(){

                            @Override
                            public void run() {
                                bOption1.setBackgroundColor(Color.parseColor("#03A9F4"));
                                bOption2.setBackgroundColor(Color.parseColor("#03A9F4"));
                                bOption3.setBackgroundColor(Color.parseColor("#03A9F4"));
                                bOption4.setBackgroundColor(Color.parseColor("#03A9F4"));
                                updateQuestion(quiz);
                            }
                        },1500);
                    }
                }
            });

            bOption4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentQuestion++;
                    if(bOption4.getText().toString().equals(question.getAnswer())){
                        bOption4.setBackgroundColor(Color.GREEN);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                correct++;
                                tvCorrectAnswerNumber.setText(String.valueOf(correct));
                                bOption4.setBackgroundColor(Color.parseColor("#03A9F4"));
                                updateQuestion(quiz);
                            }
                        }, 1500);
                    }else{
                        wrong++;
                        bOption4.setBackgroundColor(Color.RED);
                        /*
                        if(bOption2.getText().toString().equals(question.getAnswer())){
                            bOption2.setBackgroundColor(Color.GREEN);
                        }else if(bOption3.getText().toString().equals(question.getAnswer())){
                            bOption3.setBackgroundColor(Color.GREEN);
                        }else if(bOption1.getText().toString().equals(question.getAnswer())){
                            bOption1.setBackgroundColor(Color.GREEN);
                        }*/

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable(){

                            @Override
                            public void run() {
                                bOption1.setBackgroundColor(Color.parseColor("#03A9F4"));
                                bOption2.setBackgroundColor(Color.parseColor("#03A9F4"));
                                bOption3.setBackgroundColor(Color.parseColor("#03A9F4"));
                                bOption4.setBackgroundColor(Color.parseColor("#03A9F4"));
                                updateQuestion(quiz);
                            }
                        },1500);
                    }
                }
            });
        }
    }

    private void verifyUserLogged(){
        if(FirebaseAuth.getInstance().getCurrentUser() == null || !FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }
}
