package com.example.onlineschool.StudentFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.onlineschool.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChangeSubscriptionNeededFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChangeSubscriptionNeededFragment extends Fragment {


    public ChangeSubscriptionNeededFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ChangeSubscriptionNeededFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChangeSubscriptionNeededFragment newInstance() {
        ChangeSubscriptionNeededFragment fragment = new ChangeSubscriptionNeededFragment();
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
        View view = inflater.inflate(R.layout.fragment_change_subscription_needed, container, false);

        return view;
    }

    private void init(View view){

    }
}
