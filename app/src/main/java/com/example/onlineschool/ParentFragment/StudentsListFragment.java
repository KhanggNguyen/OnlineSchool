package com.example.onlineschool.ParentFragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

import com.example.onlineschool.Models.Parent;
import com.example.onlineschool.Models.Student;
import com.example.onlineschool.Models.User;
import com.example.onlineschool.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StudentsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StudentsListFragment extends Fragment {

    public static final String TAG = "Students List Fragment ** : ";

    //Currently user
    private String userID;
    private User currentUser;

    //Database variable;
    private FirebaseAuth fAuth;
    private FirebaseAuth fAuthStudent;
    private FirebaseFirestore fStore;

    private RecyclerView recyclerView;
    private StudentsListFragment.StudentAdapter studentAdapter;
    private RecyclerView.LayoutManager layoutManager;

    ArrayList<Student> studentList = new ArrayList<Student>();

    public StudentsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StudentsListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StudentsListFragment newInstance() {
        StudentsListFragment fragment = new StudentsListFragment();
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
        View view = inflater.inflate(R.layout.fragment_students_list, container, false);

        init(view);
        printAdapter(view);
        return view;
    }

    private void init(View view) {
        //initialize db
        this.fAuth = FirebaseAuth.getInstance();
        this.fAuthStudent = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();

        //Currently user
        userID = fAuth.getCurrentUser().getUid();

        ((Button) view.findViewById(R.id.previous_page)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeParentFragment homeParentFragment = new HomeParentFragment();

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, homeParentFragment);
                fragmentTransaction.commit();
            }
        });

    }

    private void printAdapter(final View view){
        recyclerView = view.findViewById(R.id.recycler_student_lists);
        layoutManager = new LinearLayoutManager(getActivity());
        studentAdapter = new StudentsListFragment.StudentAdapter(studentList);

        DocumentReference userRef = fStore.collection("parents").document(userID);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                final Parent parent = documentSnapshot.toObject(Parent.class);
                final CollectionReference studentCollectionRef = fStore.collection("students");
                studentCollectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(final QueryDocumentSnapshot document : queryDocumentSnapshots){
                            Student student = document.toObject(Student.class);
                            student.setDocumentId(document.getId());
                            DocumentReference parentRef = fStore.collection("parents").document(userID);
                            if(student.getParentReference() != null){
                                if(student.getParentReference().equals(parentRef)){
                                    studentList.add(student);
                                    Log.d(TAG, document.getId());
                                }
                            }
                        }
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(studentAdapter);


                    }
                });
            }
        });
    }

    private class StudentViewHolder extends RecyclerView.ViewHolder{
        private CardView cardView;


        private TextView tvStudentName;

        public StudentViewHolder(View itemView){
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.student_name);
        }

        public StudentViewHolder(LayoutInflater inflater, ViewGroup container){
            super(inflater.inflate(R.layout.cardview_student, container, false));

            cardView = itemView.findViewById(R.id.cardview_student);

            tvStudentName = itemView.findViewById(R.id.student_name);

        }
    }

    public class StudentAdapter extends RecyclerView.Adapter<StudentViewHolder> {
        private ArrayList<Student> mStudentsList;

        public StudentAdapter(ArrayList<Student> studentsList){
            mStudentsList = studentsList;
        }

        @NonNull
        @Override
        public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new StudentViewHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull final StudentViewHolder holder, final int position) {
            final Student currentItem = mStudentsList.get(position);

            DocumentReference studentRef = fStore.collection("users").document(currentItem.getDocumentId());
            studentRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User user = documentSnapshot.toObject(User.class);
                    holder.tvStudentName.setText(user.getName());
                }
            });

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ParentStudentActionListFragment parentStudentActionListFragment = new ParentStudentActionListFragment();
                    //set chapter clicked in parameter
                    Bundle bundle = new Bundle();
                    bundle.putString("studentID", currentItem.getDocumentId());
                    parentStudentActionListFragment.setArguments(bundle);
                    //change fragment on click
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, parentStudentActionListFragment);
                    fragmentTransaction.commit();
                }
            });

        }

        @Override
        public int getItemCount() {
            return mStudentsList.size();
        }
    }
}
