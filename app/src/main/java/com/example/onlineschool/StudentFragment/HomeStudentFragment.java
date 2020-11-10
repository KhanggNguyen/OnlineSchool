package com.example.onlineschool.StudentFragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.onlineschool.LoginActivity;
import com.example.onlineschool.R;
import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeStudentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeStudentFragment extends Fragment{

    //Declare cardview variable
    private CardView cvCourses, cvHistory, cvLiveLesson, cvChat;

    public HomeStudentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment HomeStudentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeStudentFragment newInstance() {
        HomeStudentFragment fragment = new HomeStudentFragment();
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
        View view = inflater.inflate(R.layout.fragment_home_student, container, false);
        verifyUserLogged();
        init(view);
        OnClickCourses(view);
        OnClickHistory(view);
        OnClickChat(view);
        OnClickLive(view);
        return view;
    }

    private void init(View view){
        cvCourses = view.findViewById(R.id.courses);
        cvHistory = view.findViewById(R.id.history);
        cvLiveLesson = view.findViewById(R.id.live);
        cvChat = view.findViewById(R.id.chat);

    }

    private void OnClickCourses(View view){
        ((CardView) view.findViewById(R.id.courses)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CoursesFragment selectedFragment = new CoursesFragment().newInstance();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
                fragmentTransaction.commit();

            }
        });


    }


    private void OnClickHistory(View view){
        ((CardView) view.findViewById(R.id.history)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                HistoryStudentFragment selectedFragment = new HistoryStudentFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
                fragmentTransaction.commit();
            }
        });


    }

    private void OnClickChat(View view){
        ((CardView) view.findViewById(R.id.chat)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                StudentChatFragment studentChatFragment = new StudentChatFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, studentChatFragment);
                fragmentTransaction.commit();
            }
        });


    }

    private void OnClickLive(View view){
        ((CardView) view.findViewById(R.id.live)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                CoursesFragment selectedFragment = new CoursesFragment();
                FragmentManager fragmentManager = getFragmentManager();
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
