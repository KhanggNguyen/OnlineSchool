package com.example.onlineschool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.onlineschool.ParentFragment.HomeParentFragment;
import com.example.onlineschool.ParentFragment.ParentChatFragment;
import com.example.onlineschool.ParentFragment.ParentProfileFragment;
import com.example.onlineschool.ProfFragment.ProfChatListFragment;
import com.example.onlineschool.ProfFragment.HomeProfFragment;
import com.example.onlineschool.ProfFragment.ProfProfileFragment;
import com.example.onlineschool.StudentFragment.HomeStudentFragment;
import com.example.onlineschool.StudentFragment.NotificationFragment;
import com.example.onlineschool.StudentFragment.StudentChatFragment;
import com.example.onlineschool.StudentFragment.StudentProfileFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    private static int SPLASH_TIME_OUT = 4000;

    //Boolean variable to mark if the transaction is safe
    private boolean isTransactionSafe;

    //Boolean variable to mark if there is any transaction pending
    private boolean isTransactionPending;

    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private FirebaseFirestore fStore;

    //Currently user
    private String userID;
    private boolean isParent = false;
    private boolean isStudent = false;
    private boolean isProf = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        bottomNavListener();

    }

    private void init(){
        //initialize db
        fAuth = FirebaseAuth.getInstance();
        fUser = fAuth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();

        verifyUserLogged(fUser);

        if(fUser == null || !fUser.isEmailVerified()){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        userID = fUser.getUid();
        DocumentReference userRef = fStore.document("parents/"+userID);
        DocumentReference studentRef = fStore.document("students/"+userID);
        DocumentReference profRef = fStore.document("professeurs/"+userID);
        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            isParent = true;
                        }

                        if(isParent) {
                            Log.d(TAG, "Init parent home ");
                            if(isTransactionSafe){
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeParentFragment()).commit();
                                isTransactionPending = false;
                            }else{
                                isTransactionPending = true;
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error ! ", Toast.LENGTH_SHORT).show();
                    }
                })
        ;


        studentRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            isStudent = true;
                        }

                        if(isStudent) {
                            Log.d(TAG, "Init student home ");
                            if(isTransactionSafe){
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeStudentFragment()).commit();
                                isTransactionPending = false;
                            }else{
                                isTransactionPending = true;
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error ! ", Toast.LENGTH_SHORT).show();
                    }
                })
        ;


        profRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            isProf = true;
                        }

                        if(isProf) {
                            Log.d(TAG, "Init professeur home ");
                            if (isTransactionSafe){
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeProfFragment()).commit();
                                isTransactionPending = false;
                            }else{
                                isTransactionPending = true;
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error ! ", Toast.LENGTH_SHORT).show();
                    }
                })
        ;
    }

    private void verifyUserLogged(FirebaseUser user){
        if(user == null ){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

    private void bottomNavListener(){
        ((BottomNavigationView) findViewById(R.id.bottom_navigation)).setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                Fragment selectedFragment = null;
                switch(menuItem.getItemId()){
                    case R.id.nav_home:
                        if(isParent){
                            selectedFragment = new HomeParentFragment();
                        }
                        if(isStudent){
                            selectedFragment = new HomeStudentFragment();
                        }
                        if(isProf){
                            selectedFragment = new HomeProfFragment();
                        }
                        break;
                    case R.id.nav_chat:
                        if(isStudent){
                            selectedFragment = new StudentChatFragment();
                        }
                        if(isParent){
                            selectedFragment = new ParentChatFragment();
                        }
                        if(isProf){
                            selectedFragment = new ProfChatListFragment();
                        }
                        break;

                    case R.id.nav_notification:
                        selectedFragment = new NotificationFragment();
                        break;

                    case R.id.nav_profile:
                        if(isParent)
                            selectedFragment = new ParentProfileFragment();
                        if(isStudent){
                            selectedFragment = new StudentProfileFragment();
                        }
                        if(isProf){
                            selectedFragment = new ProfProfileFragment();
                        }
                        break;
                }
                if(isTransactionSafe){
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                    isTransactionPending=false;
                }else {
                    isTransactionPending=true;
                }


                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if(fragment != null){
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }else{
            super.onBackPressed();
        }
    }

    public void onPostResume(){
        super.onPostResume();
        isTransactionSafe=true;
    }

    public void onPause(){
        super.onPause();
        isTransactionSafe=false;
    }

}
