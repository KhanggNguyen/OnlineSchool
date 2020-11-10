package com.example.onlineschool.ParentFragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onlineschool.LoginActivity;
import com.example.onlineschool.Models.Formula;
import com.example.onlineschool.Models.Student;
import com.example.onlineschool.Models.Subscription;
import com.example.onlineschool.Models.User;
import com.example.onlineschool.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ParentStudentActionListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ParentStudentActionListFragment extends Fragment {
    public static final String TAG = "Parent Action On Student List ** : ";

    //Currently user
    private String userID;
    private User currentUser;

    //Database variable;
    private FirebaseAuth fAuth;
    private FirebaseAuth fAuthStudent;
    private FirebaseFirestore fStore;

    private String studentID;

    //for picking formula
    private CharSequence[] listItemsFormula;
    private List<Formula> formulasList = new ArrayList<>();
    private int checkedItemFormula;
    private List<Formula> mUserItemsFormula = new ArrayList<>();

    public ParentStudentActionListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ParentStudentActionListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ParentStudentActionListFragment newInstance() {
        ParentStudentActionListFragment fragment = new ParentStudentActionListFragment();
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
        View view = inflater.inflate(R.layout.fragment_parent_student_action_list, container, false);
        init(view);
        verifyUserLogged(view);
        onClickConsultHistory(view);
        onClickConsultQuizDone(view);
        onExtendSubscription(view);
        onChangeSubscription(view);
        return view;
    }

    private void init(View view) {
        //initialize db
        this.fAuth = FirebaseAuth.getInstance();
        this.fAuthStudent = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();

        //Currently user
        userID = fAuth.getCurrentUser().getUid();

        Bundle bundle = getArguments();
        if(bundle != null){
            studentID = bundle.getString("studentID");
        }

        //initialize picking formulas elements
        CollectionReference formulasCollectionref = fStore.collection("formula");
        formulasCollectionref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int i=0;
                for(QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots){
                    Formula formula = queryDocumentSnapshot.toObject(Formula.class);
                    formula.setDocumentID(queryDocumentSnapshot.getId());
                    formulasList.add(formula);
                    listItemsFormula[i] = "Formule " + formula.getFormula_name() + " - Prix : " + formula.getPrice();
                    i++;

                }
            }
        });
        listItemsFormula = getResources().getStringArray(R.array.formula);


        ((Button) view.findViewById(R.id.previous_page)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StudentsListFragment studentsListFragment = new StudentsListFragment();
                //change fragment on click
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, studentsListFragment);
                fragmentTransaction.commit();
            }
        });


    }

    private void verifyUserLogged(View view){
        if(FirebaseAuth.getInstance().getCurrentUser() == null || !FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

    private void onClickConsultHistory(View view){
        ((Button) view.findViewById(R.id.consult_history)).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                HistoryParentFragment historyParentFragment = new HistoryParentFragment();
                //get student id from bundle

                //set chapter clicked in parameter
                Bundle bundle2 = new Bundle();
                bundle2.putString("studentID", studentID);
                historyParentFragment.setArguments(bundle2);
                //change fragment on click
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, historyParentFragment);
                fragmentTransaction.commit();

            }
        });
    }

    private void onClickConsultQuizDone(View view){
        ((Button) view.findViewById(R.id.consult_quiz_done)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConsultQuizDoneFragment consultQuizDoneFragment = new ConsultQuizDoneFragment();
                //get student id from bundle
                /*
                Bundle bundle = getArguments();
                String studentID = null;
                if(bundle != null){
                    studentID = bundle.getString("studentID");
                }
                */
                //set chapter clicked in parameter
                Bundle bundle2 = new Bundle();
                bundle2.putString("studentID", studentID);
                consultQuizDoneFragment.setArguments(bundle2);
                //change fragment on click
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, consultQuizDoneFragment);
                fragmentTransaction.commit();
            }
        });
    }

    private void onChangeSubscription(View view) {
        ((Button) view.findViewById(R.id.modify_subscription)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                mBuilder.setTitle(getResources().getString(R.string.bFormulas_string));
                final int[] index = new int[1];
                mBuilder.setSingleChoiceItems(listItemsFormula, checkedItemFormula, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.OptionMessageRegisterStudentFragment).toString() + listItemsFormula[which].toString(), Toast.LENGTH_SHORT).show();
                        index[0] = which;
                        checkedItemFormula = which + 1;

                    }
                });

                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String item = listItemsFormula[index[0]].toString();
                        mUserItemsFormula.add(formulasList.get(index[0]));

                        //extend
                        final DocumentReference documentReferenceStudent = fStore.collection("students").document(studentID);
                        documentReferenceStudent.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Student student = documentSnapshot.toObject(Student.class);
                                final Subscription subscription = new Subscription();
                                //initalize formula
                                subscription.setFormula_choisi(mUserItemsFormula.get(0));
                                //expired
                                if(student.getSubscription().getDate_fin().toDate().before(new Date())){
                                    Date dt = new Date();
                                    Calendar c = Calendar.getInstance();
                                    c.setTime(dt);
                                    c.add(Calendar.MONTH, 1);
                                    dt = c.getTime();
                                    subscription.setDate_fin(new Timestamp(dt));

                                }else{
                                    //inscrease incremente current month
                                    if(student.getSubscription().getFormula_choisi().equals(mUserItemsFormula)){
                                        Date dt = student.getSubscription().getDate_fin().toDate();
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(dt);
                                        c.add(Calendar.MONTH, 1);
                                        dt = c.getTime();
                                        subscription.setDate_fin(new Timestamp(dt));
                                    }else{
                                        Date dt = new Date();
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(dt);
                                        c.add(Calendar.MONTH, 1);
                                        dt = c.getTime();
                                        subscription.setDate_fin(new Timestamp(dt));
                                    }
                                }
                                student.setSubscription(subscription);
                                documentReferenceStudent.set(student).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @SuppressLint("LongLogTag")
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getActivity(), "Le changement d'abonnement a été pris en compte. Votre nouveau abonnement est " + subscription.getFormula_choisi().getFormula_name() + " jusqu'à " + subscription.getDate_fin().toDate().toString(), Toast.LENGTH_LONG).show();
                                        Log.d(TAG, "onSuccess : student Profile is created for " + studentID);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @SuppressLint("LongLogTag")
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure : " + e.toString());
                                    }
                                });
                            }
                        });
                    }
                });
                AlertDialog mDialog = mBuilder.create();
                mDialog.show();

            }
        });
    }

    private void onExtendSubscription(View view){
        //get student id from bundle
        ((Button) view.findViewById(R.id.extend_subscription)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DocumentReference documentReferenceStudent = fStore.collection("students").document(studentID);
                documentReferenceStudent.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot != null){
                            Student student = documentSnapshot.toObject(Student.class);
                            Subscription subscription = student.getSubscription();
                            //initalize formula
                            final Map<String, Object> formulaObject = new HashMap<>();
                            //expired
                            if(student.getSubscription().getDate_fin().toDate().before(new Date())){
                                Date dt = new Date();
                                Calendar c = Calendar.getInstance();
                                c.setTime(dt);
                                c.add(Calendar.MONTH, 1);
                                dt = c.getTime();
                                subscription.setDate_fin(new Timestamp(dt));
                            }else{
                                Date dt = student.getSubscription().getDate_fin().toDate();
                                Calendar c = Calendar.getInstance();
                                c.setTime(dt);
                                c.add(Calendar.MONTH, 1);
                                dt = c.getTime();
                                subscription.setDate_fin(new Timestamp(dt));
                            }
                            Toast.makeText(getActivity(), "La prolongation a été bien pris en compte. Votre abonnement se prolonge jusqu'à : " + subscription.getDate_fin().toDate().toString(), Toast.LENGTH_LONG).show();
                            student.setSubscription(subscription);
                            documentReferenceStudent.set(student).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @SuppressLint("LongLogTag")
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess : student Profile is created for " + studentID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @SuppressLint("LongLogTag")
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure : " + e.toString());
                                }
                            });
                        }
                    }
                });
            }
        });
    }


}
