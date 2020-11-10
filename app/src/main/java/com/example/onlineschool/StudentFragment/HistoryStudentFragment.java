package com.example.onlineschool.StudentFragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
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

import com.example.onlineschool.Models.ActivityHistory;
import com.example.onlineschool.Models.Lesson;
import com.example.onlineschool.Models.User;
import com.example.onlineschool.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryStudentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryStudentFragment extends Fragment {
    public static final String TAG = "Recent Activity ** : ";

    //layout
    private Button bPrevious;

    //Currently user
    private String userID;
    private User currentUser;

    //Database variable;
    private FirebaseAuth fAuth;
    private FirebaseAuth fAuthStudent;
    private FirebaseFirestore fStore;

    //list layout variable
    private RecyclerView recyclerView;
    private HistoryStudentFragment.HistoriesAdapter historiesAdapter;
    private RecyclerView.LayoutManager layoutManager;

    ArrayList<ActivityHistory> historiesList = new ArrayList<ActivityHistory>();

    public HistoryStudentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryStudentFragment newInstance() {
        HistoryStudentFragment fragment = new HistoryStudentFragment();
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
        View view = inflater.inflate(R.layout.fragment_history_student, container, false);

        init(view);

        return view;
    }

    private void init(View view) {
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        bPrevious = view.findViewById(R.id.previous_page);

        bPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeStudentFragment homeStudentFragment = new HomeStudentFragment();

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, homeStudentFragment);
                fragmentTransaction.commit();
            }
        });

        //recycler view
        recyclerView = view.findViewById(R.id.recycler_histories);
        layoutManager = new LinearLayoutManager(getActivity());
        historiesAdapter = new HistoryStudentFragment.HistoriesAdapter(historiesList);

        CollectionReference historiesColRef = fStore.collection("activity_history");
        historiesColRef.orderBy("activityTime", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots){
                    ActivityHistory activityHistory = queryDocumentSnapshot.toObject(ActivityHistory.class);
                    activityHistory.setDocumentId(queryDocumentSnapshot.getId());
                    if(activityHistory.getStudentId().equals(userID)){
                        historiesList.add(activityHistory);
                    }

                    //add to list
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(historiesAdapter);
                }
            }
        });

    }


    private class HistoryViewHolder extends RecyclerView.ViewHolder{
        private CardView cardView;


        private TextView tvLessonTitle, tvActivityType, tvConnectionTime;

        public HistoryViewHolder(View itemView){
            super(itemView);
            tvActivityType = itemView.findViewById(R.id.activity_type);
            tvLessonTitle = itemView.findViewById(R.id.lesson_title);
            tvConnectionTime = itemView.findViewById(R.id.connection_time);
        }

        public HistoryViewHolder(LayoutInflater inflater, ViewGroup container, OnItemClickListener listener){
            super(inflater.inflate(R.layout.cardview_history, container, false));

            cardView = itemView.findViewById(R.id.cardview_history);

            tvActivityType = itemView.findViewById(R.id.activity_type);
            tvLessonTitle = itemView.findViewById(R.id.lesson_title);
            tvConnectionTime = itemView.findViewById(R.id.connection_time);

        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public class HistoriesAdapter extends RecyclerView.Adapter<HistoryViewHolder> {
        private ArrayList<ActivityHistory> mHistoriesLists;
        private OnItemClickListener mListener;

        public void setOnItemClickListener(OnItemClickListener listener){
            mListener = listener;
        }

        public HistoriesAdapter(ArrayList<ActivityHistory> historiesList){
            mHistoriesLists = historiesList;
        }

        @NonNull
        @Override
        public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new HistoryViewHolder(inflater, parent, mListener);
        }

        @Override
        public void onBindViewHolder(@NonNull final HistoryViewHolder holder, final int position) {
            final ActivityHistory currentItem = mHistoriesLists.get(position);

            //set time
            holder.tvConnectionTime.setText(currentItem.getActivityTime().toDate().toString());

            //find lesson title
            DocumentReference lessonDocRef = fStore.collection("courses_sheet").document(currentItem.getLessonId());
            lessonDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Lesson lesson = documentSnapshot.toObject(Lesson.class);

                    holder.tvLessonTitle.setText(String.valueOf(lesson.getLesson()) + " : " + lesson.getTitle());

                    if(currentItem.isLessonSheet()){
                        holder.tvActivityType.setText(R.string.cardview_history_title);
                    }

                    if(currentItem.isExercice()){
                        holder.tvActivityType.setText("Exercices");
                    }

                    if(currentItem.isQuiz()){
                        holder.tvActivityType.setText("Quiz");
                    }

                    if(currentItem.isVideo()){
                        holder.tvActivityType.setText("Vidéo");
                    }
                }
            });


        }

        @Override
        public int getItemCount() {
            return mHistoriesLists.size();
        }
    }

}
