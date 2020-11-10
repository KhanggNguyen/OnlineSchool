package com.example.onlineschool.ProfFragment;

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
import com.example.onlineschool.StudentFragment.CoursesFragment;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeProfFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeProfFragment extends Fragment {
    private CardView cvMessages, cvStudentList;


    public HomeProfFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeProfFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeProfFragment newInstance() {
        HomeProfFragment fragment = new HomeProfFragment();
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
        View view = inflater.inflate(R.layout.fragment_home_prof, container, false);
        verifyUserLogged();
        init(view);

        return view;
    }

    private void init(View view){
        OnClickStudentList(view);
        OnClickMessages(view);
    }

    private void verifyUserLogged(){
        if(FirebaseAuth.getInstance().getCurrentUser() == null || !FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

    private void OnClickStudentList(View view){
        ((CardView) view.findViewById(R.id.students_list)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersFragment selectedFragment = new UsersFragment().newInstance();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
                fragmentTransaction.commit();
            }
        });
    }

    private void OnClickMessages(View view){
        ((CardView) view.findViewById(R.id.Messages)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfChatListFragment selectedFragment = new ProfChatListFragment().newInstance();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
                fragmentTransaction.commit();
            }
        });
    }
}
