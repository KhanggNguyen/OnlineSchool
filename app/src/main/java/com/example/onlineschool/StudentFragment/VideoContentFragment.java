package com.example.onlineschool.StudentFragment;

import android.content.Intent;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.VideoView;

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

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoContentFragment extends Fragment {

    //declare db variables
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private String userID;
    private User user;

    private TextView tvTitle, tvMessage;

    //private Button bPlay, bPause;
    private Button bPrevious;
    //private VideoView videoView;

    //curent lesson
    private Lesson lesson;

    private ArrayList<String> videosList = new ArrayList<String>();

    //list layout variable
    private RecyclerView recyclerView;
    private VideoContentFragment.VideosAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    public VideoContentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VideoContentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoContentFragment newInstance() {
        VideoContentFragment fragment = new VideoContentFragment();
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
        View view = inflater.inflate(R.layout.fragment_video_content, container, false);
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
        tvMessage = view.findViewById(R.id.message);
        //videoView = view.findViewById(R.id.video);
        bPrevious = view.findViewById(R.id.previous_page);
        //bPause = view.findViewById(R.id.pause);
        //bPlay = view.findViewById(R.id.play);

        //recycler view
        recyclerView = view.findViewById(R.id.recycler_video);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new VideosAdapter(videosList);

        //get course pass in parameter
        Bundle bundle = getArguments();
        String lessonIdString = null;
        if(bundle != null) {
            lessonIdString = bundle.getString("lessonId");
        }

        DocumentReference lessonRef = fStore.collection("courses_sheet").document(lessonIdString);

        lessonRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    System.err.println("Erreur de listener : " + e );
                    return;
                }

                if(documentSnapshot != null){
                    lesson = documentSnapshot.toObject(Lesson.class);
                    lesson.setDocumentId(documentSnapshot.getId());

                    tvTitle.setText(lesson.getTitle());

                    //on click back
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

                    //set up video
                    if(lesson.getVideos().size() > 0){

                        for(String urlVideo : lesson.getVideos()){
                            videosList.add(urlVideo);
                        }

                        //add to list
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(adapter);
                    }else{
                        tvMessage.setText("Pas de vidéo disponible!");
                    }
                }
            }
        });

    }

    private class VideoViewHolder extends RecyclerView.ViewHolder{
        private CardView cardView;

        private VideoView videoView;
        private Button play,pause;

        public VideoViewHolder(LayoutInflater inflater, ViewGroup container, final OnItemClickListener listener){
            super(inflater.inflate(R.layout.cardview_video, container, false));

            cardView = itemView.findViewById(R.id.cardview_video);

            videoView = itemView.findViewById(R.id.video);

            play = itemView.findViewById(R.id.play);
            pause = itemView.findViewById(R.id.pause);

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

    public class VideosAdapter extends RecyclerView.Adapter<VideoViewHolder> {
        private ArrayList<String> mVideosUriList;
        private OnItemClickListener mListener;

        public void setOnItemClickListener(OnItemClickListener listener){
            mListener = listener;
        }

        public VideosAdapter(ArrayList<String> videosUriList){
            mVideosUriList = videosUriList;
        }

        @NonNull
        @Override
        public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new VideoViewHolder(inflater, parent, mListener);
        }

        @Override
        public void onBindViewHolder(@NonNull final VideoViewHolder holder, final int position) {
            final String currentItem = mVideosUriList.get(position);

            Uri uri = Uri.parse(currentItem);

            holder.videoView.setVideoURI(uri);
            holder.videoView.requestFocus();

            holder.play.setClickable(true);
            holder.pause.setClickable(true);
            holder.play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.videoView.start();
                }
            });

            holder.pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.videoView.pause();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mVideosUriList.size();
        }
    }

    private void verifyUserLogged(){
        if(FirebaseAuth.getInstance().getCurrentUser() == null || !FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }


}
