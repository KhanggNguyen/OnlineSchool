package com.example.onlineschool.ParentFragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.onlineschool.LoginActivity;
import com.example.onlineschool.Models.Parent;
import com.example.onlineschool.Models.Student;
import com.example.onlineschool.Models.User;
import com.example.onlineschool.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StudentLastConnectionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StudentLastConnectionsFragment extends Fragment {
    public static final String TAG = "Last connections ** : ";

    //Currently user
    private String userID;
    private User currentUser;

    //Database variable;
    private FirebaseAuth fAuth;
    private FirebaseAuth fAuthStudent;
    private FirebaseFirestore fStore;

    //list layout variable
    private RecyclerView recyclerView;
    private RecyclerView recyclerViewConnection;
    private StudentLastConnectionsFragment.StudentAdapter studentAdapter;
    private StudentLastConnectionsFragment.ConnectionAdapter connectionAdapter;
    private RecyclerView.LayoutManager layoutManager;

    ArrayList<Student> studentList = new ArrayList<Student>();
    ArrayList<Timestamp> timestampsList;

    public StudentLastConnectionsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.

     * @return A new instance of fragment StudentLastConnectionsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StudentLastConnectionsFragment newInstance() {
        StudentLastConnectionsFragment fragment = new StudentLastConnectionsFragment();
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
        View view = inflater.inflate(R.layout.fragment_student_last_connections, container, false);
        verifyUserLogged();
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

    }

    private void printAdapter(final View view){
        recyclerView = view.findViewById(R.id.recycler_last_connection);
        layoutManager = new LinearLayoutManager(getActivity());
        studentAdapter = new StudentLastConnectionsFragment.StudentAdapter(studentList);

        DocumentReference userRef = fStore.collection("parents").document(userID);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                final Parent parent = documentSnapshot.toObject(Parent.class);
                final CollectionReference studentCollectionRef = fStore.collection("students");
                studentCollectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
            recyclerViewConnection = itemView.findViewById(R.id.recycler_last_connection_cardview);
        }

        public StudentViewHolder(LayoutInflater inflater, ViewGroup container, final OnItemClickListener listener){
            super(inflater.inflate(R.layout.cardview_students_list, container, false));

            cardView = itemView.findViewById(R.id.cardview_students_list);

            tvStudentName = itemView.findViewById(R.id.student_name);
            recyclerViewConnection = itemView.findViewById(R.id.recycler_last_connection_cardview);

        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public class StudentAdapter extends RecyclerView.Adapter<StudentLastConnectionsFragment.StudentViewHolder> {
        private ArrayList<Student> mStudentsList;
        private OnItemClickListener mListener;

        public void setOnItemClickListener(OnItemClickListener listener){
            mListener = listener;
        }

        public StudentAdapter(ArrayList<Student> studentsList){
            mStudentsList = studentsList;
        }

        @NonNull
        @Override
        public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new StudentViewHolder(inflater, parent, mListener);
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
            timestampsList = new ArrayList<Timestamp>();
            //traiter students list connection with another cardView -> connection cardview  + adapter
            layoutManager = new LinearLayoutManager(getActivity());
            connectionAdapter = new StudentLastConnectionsFragment.ConnectionAdapter(timestampsList);
            studentRef = fStore.collection("students").document(currentItem.getDocumentId());
            studentRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Student student = documentSnapshot.toObject(Student.class);
                    student.setDocumentId(documentSnapshot.getId());

                    for(Timestamp timestamp : student.getStudentConnection()){
                        timestampsList.add(timestamp);
                    }

                    Collections.sort(timestampsList, Collections.<Timestamp>reverseOrder());

                    recyclerViewConnection.setHasFixedSize(true);
                    recyclerViewConnection.setLayoutManager(layoutManager);
                    recyclerViewConnection.setAdapter(connectionAdapter);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mStudentsList.size();
        }
    }

    private class ConnectionViewHolder extends RecyclerView.ViewHolder{
        private CardView cardView;

        private TextView tvConnectionTime;

        public ConnectionViewHolder(View itemView){
            super(itemView);
            tvConnectionTime = itemView.findViewById(R.id.connection_time);
            cardView = itemView.findViewById(R.id.cardview_students_connection);
        }

        public ConnectionViewHolder(LayoutInflater inflater, ViewGroup container, final OnItemClickListener listener){
            super(inflater.inflate(R.layout.cardview_students_connection, container, false));

            cardView = itemView.findViewById(R.id.cardview_students_connection);

            tvConnectionTime = itemView.findViewById(R.id.connection_time);

        }
    }

    public class ConnectionAdapter extends RecyclerView.Adapter<ConnectionViewHolder> {
        private ArrayList<Timestamp> mTimestampsList;
        private OnItemClickListener mListener;

        public void setOnItemClickListener(OnItemClickListener listener){
            mListener = listener;
        }

        public ConnectionAdapter(ArrayList<Timestamp> TimestampsList){
            mTimestampsList = TimestampsList;
        }

        @NonNull
        @Override
        public ConnectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new ConnectionViewHolder(inflater, parent, mListener);
        }

        @Override
        public void onBindViewHolder(@NonNull ConnectionViewHolder holder, final int position) {
            final Timestamp currentItem = mTimestampsList.get(position);

            holder.tvConnectionTime.setText(currentItem.toDate().toString());
        }

        @Override
        public int getItemCount() {
            return mTimestampsList.size();
        }

    }

    private void verifyUserLogged(){
        if(FirebaseAuth.getInstance().getCurrentUser() == null || !FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

}
