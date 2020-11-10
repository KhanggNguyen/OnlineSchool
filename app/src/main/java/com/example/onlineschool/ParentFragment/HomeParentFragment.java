package com.example.onlineschool.ParentFragment;

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
 * Use the {@link HomeParentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeParentFragment extends Fragment {

    public HomeParentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeParentFragment newInstance() {
        HomeParentFragment fragment = new HomeParentFragment();
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
        View view = inflater.inflate(R.layout.fragment_home_parent, container, false);
        verifyUserLogged();
        init(view);
        OnClickLastConnection(view);
        OnClickStudentsList(view);
        OnClickAddStudent(view);
        return view;
    }

    private void init(View view) {
    }

    private void verifyUserLogged(){
        if(FirebaseAuth.getInstance().getCurrentUser() == null || !FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

    private void OnClickLastConnection(View view){
        ((CardView) view.findViewById(R.id.students_connection)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StudentLastConnectionsFragment selectedFragment = new StudentLastConnectionsFragment().newInstance();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
                fragmentTransaction.commit();
            }
        });
    }

    private void OnClickStudentsList(View view){
        ((CardView) view.findViewById(R.id.cardview_menu_students_list)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StudentsListFragment selectedFragment = new StudentsListFragment().newInstance();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
                fragmentTransaction.commit();
            }
        });
    }

    private void OnClickAddStudent(View view){
        ((CardView) view.findViewById(R.id.add_student)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragmentRegisterStudent = new RegisterStudentFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragmentRegisterStudent);
                fragmentTransaction.commit();
            }
        });
    }
}
