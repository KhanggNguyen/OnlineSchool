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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.onlineschool.LoginActivity;
import com.example.onlineschool.Models.Course;
import com.example.onlineschool.Models.Grade;
import com.example.onlineschool.Models.Student;
import com.example.onlineschool.Models.Subject;
import com.example.onlineschool.Models.Subscription;
import com.example.onlineschool.Models.User;
import com.example.onlineschool.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CoursesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CoursesFragment extends Fragment {
    //declare db variables
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private String userID;
    private User user;

    //list layout variable
    private RecyclerView recyclerView;
    private CoursesAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    public CoursesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Courses.
     */
    // TODO: Rename and change types and number of parameters
    public static CoursesFragment newInstance(String param1, String param2) {
        CoursesFragment fragment = new CoursesFragment();
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
        View view = inflater.inflate(R.layout.fragment_courses, container, false);
        verifyIfUserSubscription(view);
        verifyUserLogged();
        init(view);
        return view;
    }

    private void init(View view){
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fUser = fAuth.getCurrentUser();
        userID = fUser.getUid();

        //initialize current user
        DocumentReference userRef = fStore.collection("users").document(userID);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(User.class);
            }
        });

        //course list initialize
        final ArrayList<Course> coursesList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recycler_courses);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new CoursesAdapter(coursesList);

        //get subject list from db
        CollectionReference subjectsCollections = fStore.collection("subjects");
        subjectsCollections.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            Subject subject = documentSnapshot.toObject(Subject.class);
                            Course course = new Course();
                            course.setCourseName(subject.getTitle());
                            course.setCourseDescription(subject.getDescription());

                            switch (subject.getTitle()){
                                case "Mathématiques":
                                    course.setImageResource(R.drawable.math);
                                    break;

                                case "Histoire":
                                    course.setImageResource(R.drawable.history);
                                    break;

                                case "Géographie":
                                    course.setImageResource(R.drawable.geography);
                                    break;

                                case "Physique-chimie":
                                    course.setImageResource(R.drawable.physicchimie);
                                    break;

                                case "Philosophie":
                                    course.setImageResource(R.drawable.philosophy);
                                    break;

                                case "SVT":
                                    course.setImageResource(R.drawable.biology);
                                    break;

                                case "Anglais":
                                    course.setImageResource(R.drawable.english);
                                    break;

                                case "Espagnol":
                                    course.setImageResource(R.drawable.spainish);
                                    break;

                                case "Allemand":
                                    course.setImageResource(R.drawable.germany);
                                    break;

                                case "Français":
                                    course.setImageResource(R.drawable.french);
                                    break;
                            }

                            //verify if user subscribe this course
                            for (Grade gradeSubject : subject.getGradesRef()){
                                if(gradeSubject.getId() == user.getGrades().get(0).getId()){
                                    coursesList.add(course);
                                }
                            }

                            recyclerView.setHasFixedSize(true);
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setAdapter(adapter);

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

    public static CoursesFragment newInstance() {
        return new CoursesFragment();
    }

    private class CourseViewHolder extends RecyclerView.ViewHolder{
        private CardView cardView;

        private ImageView imageView;
        private TextView tvTitle;
        //private TextView tvDescription;

        public CourseViewHolder(View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.course_photo);
            tvTitle = itemView.findViewById(R.id.course_name);


        }

        public CourseViewHolder(LayoutInflater inflater, ViewGroup container, final OnItemClickListener listener){
            super(inflater.inflate(R.layout.cardview_layout, container, false));

            cardView = itemView.findViewById(R.id.courses);

            imageView = itemView.findViewById(R.id.course_photo);
            tvTitle = itemView.findViewById(R.id.course_name);

        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public class CoursesAdapter extends RecyclerView.Adapter<CourseViewHolder> {
        private ArrayList<Course> mCoursesList;
        private OnItemClickListener mListener;

        public void setOnItemClickListener(OnItemClickListener listener){
            mListener = listener;
        }

        public CoursesAdapter(ArrayList<Course> coursesList){
            mCoursesList = coursesList;
        }

        @NonNull
        @Override
        public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new CourseViewHolder(inflater, parent, mListener);
        }

        @Override
        public void onBindViewHolder(@NonNull CourseViewHolder holder, final int position) {
            final Course currentItem = mCoursesList.get(position);
            holder.imageView.setImageResource(currentItem.getImageResource());
            //holder.tvDescription.setText(currentItem.getCourseDescription());
            holder.tvTitle.setText(currentItem.getCourseName());
            //change fragment course content on click
            holder.cardView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    Log.d("Courses Fragment : ", mCoursesList.get(position).getCourseName());
                    AppCompatActivity activity = (AppCompatActivity) v.getContext();
                    CourseContentFragment courseContent = new CourseContentFragment();
                    //set course clicked in parameter
                    Bundle bundle = new Bundle();
                    bundle.putString("course", mCoursesList.get(position).getCourseName());
                    courseContent.setArguments(bundle);
                    //change fragment on click
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, courseContent);
                    fragmentTransaction.commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mCoursesList.size();
        }
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
                    if(subscription.getDate_fin().toDate().before(new Date())){
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
