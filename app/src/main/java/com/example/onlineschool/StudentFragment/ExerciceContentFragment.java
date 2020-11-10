package com.example.onlineschool.StudentFragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.onlineschool.LoginActivity;
import com.example.onlineschool.Models.Exercice;
import com.example.onlineschool.Models.Lesson;
import com.example.onlineschool.Models.QuestionResponse;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExerciceContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExerciceContentFragment extends Fragment {
    //declare db variables
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private String userID;
    private User user;

    private TextView tvTitle;

    private Button bPrevious;

    //Question-response list
    ArrayList<QuestionResponse> questionResponsesList = new ArrayList<QuestionResponse>();

    //curent lesson
    private Lesson lesson;
    private String lessonIdString;

    //list layout variable
    private RecyclerView recyclerView;
    private ExerciceContentFragment.ExercicesAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    public ExerciceContentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ExerciceContentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExerciceContentFragment newInstance() {
        ExerciceContentFragment fragment = new ExerciceContentFragment();
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
        View view = inflater.inflate(R.layout.fragment_exercice_content, container, false);
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

        bPrevious = view.findViewById(R.id.previous_page);

        //recycler view
        recyclerView = view.findViewById(R.id.recycler_exercice);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new ExerciceContentFragment.ExercicesAdapter(questionResponsesList);

        //get course pass in parameter
        Bundle bundle = getArguments();
        if(bundle != null){
            lessonIdString = bundle.getString("lessonId");
        }

        DocumentReference lessonRef = fStore.collection("courses_sheet").document(lessonIdString);

        lessonRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    System.err.println("Erreur de listener : " + e);
                    return;
                }
                if(documentSnapshot != null){
                    lesson = documentSnapshot.toObject(Lesson.class);
                    lesson.setDocumentId(lessonIdString);


                    tvTitle.setText(lesson.getTitle());

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

                    //exercices list
                    final CollectionReference exercicesRef = fStore.collection("exercices");
                    exercicesRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                                final Exercice ex = documentSnapshot.toObject(Exercice.class);
                                ex.setDocumentId(documentSnapshot.getId());

                                ex.getSheetRef().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Lesson lesson_temp = documentSnapshot.toObject(Lesson.class);
                                        if(lesson_temp != null ){
                                            lesson_temp.setDocumentId(documentSnapshot.getId());

                                            if(lesson.getDocumentId().equals(lesson_temp.getDocumentId())){
                                                for(int i=0; i<ex.getQuestion().size(); i++){
                                                    QuestionResponse qr = new QuestionResponse();
                                                    qr.setQuestion(ex.getQuestion().get(i));
                                                    qr.setResponse(ex.getResponse().get(i));
                                                    if(!ex.getEnonce().get(i).isEmpty()){
                                                        qr.setEnonce(ex.getEnonce().get(i));
                                                    }

                                                    if(!ex.getImageName().get(i).isEmpty()){
                                                        qr.setImage(ex.getImageName().get(i));
                                                    }

                                                    questionResponsesList.add(qr);

                                                    //add to list
                                                    recyclerView.setHasFixedSize(true);
                                                    recyclerView.setLayoutManager(layoutManager);
                                                    recyclerView.setAdapter(adapter);
                                                }
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

    private void verifyUserLogged(){
        if(FirebaseAuth.getInstance().getCurrentUser() == null || !FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }


    private class ExercicesViewHolder extends RecyclerView.ViewHolder{
        private CardView cardView;

        private TextView enonce, question, response;
        private Button show_answer;
        private ImageView image;

        public ExercicesViewHolder(LayoutInflater inflater, ViewGroup container, final OnItemClickListener listener){
            super(inflater.inflate(R.layout.cardview_question_reponse_layout, container, false));

            cardView = itemView.findViewById(R.id.question_response);

            enonce = itemView.findViewById(R.id.enonce);
            question = itemView.findViewById(R.id.question);
            response = itemView.findViewById(R.id.response);
            image = itemView.findViewById(R.id.image);
            show_answer = itemView.findViewById(R.id.show_answer);

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

    public class ExercicesAdapter extends RecyclerView.Adapter<ExercicesViewHolder> {
        private ArrayList<QuestionResponse> mExercicesList;
        private OnItemClickListener mListener;

        public void setOnItemClickListener(OnItemClickListener listener){
            mListener = listener;
        }

        public ExercicesAdapter(ArrayList<QuestionResponse> exercicesList){
            mExercicesList = exercicesList;
        }

        @NonNull
        @Override
        public ExercicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new ExercicesViewHolder(inflater, parent, mListener);
        }

        @Override
        public void onBindViewHolder(@NonNull final ExercicesViewHolder holder, final int position) {
            final QuestionResponse currentItem = mExercicesList.get(position);
            holder.enonce.setText(currentItem.getEnonce());
            holder.question.setText(currentItem.getQuestion());
            holder.response.setText(currentItem.getResponse());

            //set visibility of tv to 0
            holder.response.setVisibility(View.GONE);

            holder.show_answer.setClickable(true);
            holder.show_answer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.response.setVisibility((holder.response.getVisibility() == View.VISIBLE)
                            ? View.GONE : View.VISIBLE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mExercicesList.size();
        }
    }
}
