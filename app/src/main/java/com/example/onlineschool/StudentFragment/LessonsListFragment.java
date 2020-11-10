package com.example.onlineschool.StudentFragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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

import com.example.onlineschool.LoginActivity;
import com.example.onlineschool.Models.Chapter;
import com.example.onlineschool.Models.Grade;
import com.example.onlineschool.Models.Lesson;
import com.example.onlineschool.Models.Quiz;
import com.example.onlineschool.Models.Student;
import com.example.onlineschool.Models.Subject;
import com.example.onlineschool.Models.User;
import com.example.onlineschool.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LessonsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LessonsListFragment extends Fragment {

    //declare db variables
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private String userID;
    private User user;
    private Grade userGrade;

    //current Chapter
    Chapter chapter;

    private Button bPrevious;

    //chapters list
    ArrayList<Lesson> lessons = new ArrayList<Lesson>();

    //list layout variable
    private RecyclerView recyclerView;
    private LessonsListFragment.LessonsAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    public LessonsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LessonsListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LessonsListFragment newInstance(String param1, String param2) {
        LessonsListFragment fragment = new LessonsListFragment();
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
        View view = inflater.inflate(R.layout.fragment_lessons_list, container, false);
        verifyUserLogged();
        init(view);
        return view;
    }

    private void init(View view) {
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fUser = fAuth.getCurrentUser();
        userID = fAuth.getCurrentUser().getUid();

        //recycler view
        recyclerView = view.findViewById(R.id.recycler_lessons);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new LessonsAdapter(lessons);

        bPrevious = view.findViewById(R.id.previous_page);

        //current Chapter
        //get chapter title passing in parameter
        Bundle bundle = getArguments();
        String chapterString = null;
        if(bundle != null){
            chapterString = bundle.getString("chapter");
        }
        final CollectionReference coursesCollections = fStore.collection("courses");
        coursesCollections.whereEqualTo("title", chapterString).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    chapter = documentSnapshot.toObject(Chapter.class);
                    chapter.setDocumentId(documentSnapshot.getId());
                }

                chapter.getSubjectRef().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        final Subject subject = documentSnapshot.toObject(Subject.class);

                        bPrevious.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CourseContentFragment courseContentFragment = new CourseContentFragment();
                                //set chapter clicked in parameter
                                Bundle bundle = new Bundle();
                                bundle.putString("course", subject.getTitle());
                                courseContentFragment.setArguments(bundle);
                                //change fragment on click
                                FragmentManager fragmentManager = getFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.fragment_container, courseContentFragment);
                                fragmentTransaction.commit();
                            }
                        });
                    }
                });

                //chapters list
                final CollectionReference lessonsCollection = fStore.collection("courses_sheet");
                lessonsCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            final Lesson lesson = documentSnapshot.toObject(Lesson.class);
                            lesson.setDocumentId(documentSnapshot.getId());

                            lesson.getCourseRef().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Chapter chapter_tempo = documentSnapshot.toObject(Chapter.class);
                                    chapter_tempo.setDocumentId(documentSnapshot.getId());


                                    if(chapter_tempo.getDocumentId().equals(chapter.getDocumentId())){
                                        lessons.add(lesson);
                                    }

                                    recyclerView.setHasFixedSize(true);
                                    recyclerView.setLayoutManager(layoutManager);
                                    recyclerView.setAdapter(adapter);
                                }
                            });
                        }
                    }
                });
            }
        });

        //initialize current user
        final DocumentReference usersReference = fStore.collection("users").document(userID);

    }

    public static LessonsListFragment newInstance() {
        return new LessonsListFragment();
    }

    private class LessonsViewHolder extends RecyclerView.ViewHolder{
        private CardView cardView;

        private TextView tvLesson;
        private TextView tvTitle;

        public LessonsViewHolder(LayoutInflater inflater, ViewGroup container, final OnItemClickListener listener){
            super(inflater.inflate(R.layout.cardview_lesson_layout, container, false));

            cardView = itemView.findViewById(R.id.cardview_lesson);

            tvTitle = itemView.findViewById(R.id.title);
            tvLesson = itemView.findViewById(R.id.lesson);

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {


                }

            });
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public class LessonsAdapter extends RecyclerView.Adapter<LessonsViewHolder> {
        private ArrayList<Lesson> mLessonsList;
        private OnItemClickListener mListener;

        public void setOnItemClickListener(OnItemClickListener listener){
            mListener = listener;
        }

        public LessonsAdapter(ArrayList<Lesson> lessonsList){
            mLessonsList = lessonsList;
        }

        @NonNull
        @Override
        public LessonsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new LessonsViewHolder(inflater, parent, mListener);
        }

        @Override
        public void onBindViewHolder(@NonNull final LessonsListFragment.LessonsViewHolder holder, final int position) {
            final Lesson currentItem = mLessonsList.get(position);
            holder.tvTitle.setText(currentItem.getTitle());
            holder.tvLesson.setText(Integer.toString(currentItem.getLesson()));

            //check if lesson is done
            DocumentReference studentRef = fStore.collection("students").document(userID);
            studentRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Student student = documentSnapshot.toObject(Student.class);

                    //run through quizDone list by student to check if lesson is done
                    List<Quiz> quizDone = student.getQuizDone();
                    if(quizDone != null){
                        for(Quiz quiz : quizDone){
                            quiz.getSheetRef().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Lesson lesson_of_quiz = documentSnapshot.toObject(Lesson.class);
                                    lesson_of_quiz.setDocumentId(documentSnapshot.getId());

                                    //if quiz of current lesson is done, we checked
                                    if(lesson_of_quiz.getDocumentId().equals(currentItem.getDocumentId())){
                                        holder.cardView.setCardBackgroundColor(Color.GREEN);
                                    }
                                }
                            });
                        }
                    }

                }
            });

            //change fragment course content on click
            holder.cardView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    AppCompatActivity activity = (AppCompatActivity) v.getContext();
                    LessonFragment lessonFragment = new LessonFragment();
                    //set chapter clicked in parameter
                    Bundle bundle = new Bundle();
                    bundle.putString("lessonId", mLessonsList.get(position).getDocumentId());
                    lessonFragment.setArguments(bundle);
                    //change fragment on click
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, lessonFragment);
                    fragmentTransaction.commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mLessonsList.size();
        }
    }

    private void verifyUserLogged(){
        if(FirebaseAuth.getInstance().getCurrentUser() == null || !FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }
}
