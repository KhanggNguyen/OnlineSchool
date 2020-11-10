package com.example.onlineschool.StudentFragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import android.widget.TextView;

import com.example.onlineschool.LoginActivity;
import com.example.onlineschool.Models.Chapter;
import com.example.onlineschool.Models.Grade;
import com.example.onlineschool.Models.Lesson;
import com.example.onlineschool.Models.Subject;
import com.example.onlineschool.Models.User;
import com.example.onlineschool.R;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CourseContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CourseContentFragment extends Fragment {
    //declare db variables
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private String userID;
    private User user;
    private Grade userGrade;

    private Button bPrevious;

    //list layout variable
    private RecyclerView recyclerView;
    private CourseContentFragment.ChaptersAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    //current Subject
    Subject subject;

    //chapters list
    ArrayList<Chapter> chapters = new ArrayList<Chapter>();

    //lessons list
    List<Lesson> lessons = new ArrayList<Lesson>();

    //gradeList
    List<Grade> grades = new ArrayList<Grade>();

    public CourseContentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CourseContentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CourseContentFragment newInstance(String param1, String param2) {
        CourseContentFragment fragment = new CourseContentFragment();
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
        View view = inflater.inflate(R.layout.fragment_course_content, container, false);
        verifyUserLogged();
        init(view);
        return view;
    }

    private void init(View view){
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fUser = fAuth.getCurrentUser();
        userID = fAuth.getCurrentUser().getUid();

        //recycler view
        recyclerView = view.findViewById(R.id.recycler_chapter);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new ChaptersAdapter(chapters);

        bPrevious = view.findViewById(R.id.previous_page);

        bPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CoursesFragment coursesFragment = new CoursesFragment();
                //change fragment on click
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, coursesFragment);
                fragmentTransaction.commit();
            }
        });

        //initialize current user
        final DocumentReference usersReference = fStore.collection("users").document(userID);
        usersReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null ) {
                    System.err.println("Erreur de listener : " + e);
                    return;
                }

                if(documentSnapshot != null ) {
                    User user = documentSnapshot.toObject(User.class);
                    grades = user.getGrades();
                    userGrade = grades.get(0);

                    //get course pass in parameter
                    Bundle bundle = getArguments();
                    String subjectString = null;
                    if(bundle != null){
                        subjectString = bundle.getString("course");
                    }

                    //---------add chapter to list
                    //current subject
                    final CollectionReference subjectsCollections = fStore.collection("subjects");
                    subjectsCollections.whereEqualTo("title", subjectString).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(@Nullable QuerySnapshot queryDocumentSnapshots) {
                            for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                                subject = documentSnapshot.toObject(Subject.class);
                                subject.setDocumentId(documentSnapshot.getId());
                            }

                            //chapters list
                            final CollectionReference chaptersCollection = fStore.collection("courses");
                            chaptersCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                                        final Chapter chapter = documentSnapshot.toObject(Chapter.class);
                                        chapter.setDocumentId(documentSnapshot.getId());

                                        chapter.getSubjectRef().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if(documentSnapshot != null){
                                                    Subject subject_tempo = documentSnapshot.toObject(Subject.class);
                                                    if(subject_tempo != null){
                                                        Log.d("TAG", subject_tempo.getTitle());
                                                        //subject_tempo.setDocumentId(documentSnapshot.getId());
                                                        if(subject_tempo.getTitle().equals(subject.getTitle())){
                                                            chapters.add(chapter);
                                                        }
                                                    }
                                                }


                                                //add to list
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

    public static CourseContentFragment newInstance() {
        return new CourseContentFragment();
    }

    private class ChapterViewHolder extends RecyclerView.ViewHolder{
        private CardView cardView;

        private TextView tvChapter;
        private TextView tvTitle;

        public ChapterViewHolder(LayoutInflater inflater, ViewGroup container, final OnItemClickListener listener){
            super(inflater.inflate(R.layout.cardview_chapter_layout, container, false));

            cardView = itemView.findViewById(R.id.cardview_chapter);

            tvTitle = itemView.findViewById(R.id.title);
            tvChapter = itemView.findViewById(R.id.chapter);

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

    public class ChaptersAdapter extends RecyclerView.Adapter<ChapterViewHolder> {
        private ArrayList<Chapter> mChapterList;
        private OnItemClickListener mListener;

        public void setOnItemClickListener(OnItemClickListener listener){
            mListener = listener;
        }

        public ChaptersAdapter(ArrayList<Chapter> chaptersList){
            mChapterList = chaptersList;
        }

        @NonNull
        @Override
        public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new ChapterViewHolder(inflater, parent, mListener);
        }

        @Override
        public void onBindViewHolder(@NonNull CourseContentFragment.ChapterViewHolder holder, final int position) {
            final Chapter currentItem = mChapterList.get(position);
            holder.tvTitle.setText(currentItem.getTitle());
            holder.tvChapter.setText(Integer.toString(currentItem.getChapter()));
            //change fragment course content on click
            holder.cardView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    AppCompatActivity activity = (AppCompatActivity) v.getContext();
                    LessonsListFragment lessonsList = new LessonsListFragment();
                    //set chapter clicked in parameter
                    Bundle bundle = new Bundle();
                    bundle.putString("chapter", mChapterList.get(position).getTitle());
                    lessonsList.setArguments(bundle);
                    //change fragment on click
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, lessonsList);
                    fragmentTransaction.commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mChapterList.size();
        }
    }
}
