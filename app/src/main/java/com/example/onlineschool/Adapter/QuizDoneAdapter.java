package com.example.onlineschool.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineschool.Models.Chapter;
import com.example.onlineschool.Models.Course;
import com.example.onlineschool.Models.Lesson;
import com.example.onlineschool.Models.Quiz;
import com.example.onlineschool.Models.Subject;
import com.example.onlineschool.Models.User;
import com.example.onlineschool.ProfFragment.ProfChatFragment;
import com.example.onlineschool.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class QuizDoneAdapter extends RecyclerView.Adapter<QuizDoneAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Quiz> mQuizList;

    public QuizDoneAdapter(Context mContext, ArrayList<Quiz> quizList){
        this.mQuizList = quizList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public QuizDoneAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.quiz_item, parent, false);
        return new QuizDoneAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final QuizDoneAdapter.ViewHolder holder, int position) {

        Quiz quiz = mQuizList.get(position);
        //set lesson title
        quiz.getSheetRef().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Lesson lesson = documentSnapshot.toObject(Lesson.class);
                holder.lesson_title.setText("Leçon " + String.valueOf(lesson.getLesson()) + " : " + lesson.getTitle());

                //set chapter
                lesson.getCourseRef().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Chapter chapter = documentSnapshot.toObject(Chapter.class);
                        holder.chapter_title.setText("Chapter " + chapter.getChapter() + " : " + chapter.getTitle());

                        //set title subject
                        chapter.getSubjectRef().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Subject subject = documentSnapshot.toObject(Subject.class);
                                holder.subject_title.setText(subject.getTitle());
                            }
                        });
                    }
                });
            }
        });


        holder.time_done.setText(quiz.getTimeQuizDone().toDate().toString());
    }

    @Override
    public int getItemCount() {
        return mQuizList.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{
        private TextView chapter_title, lesson_title, subject_title, time_done;

        public ViewHolder(View itemView) {
            super(itemView);
            chapter_title = itemView.findViewById(R.id.chapter_title);
            lesson_title = itemView.findViewById(R.id.lesson_title);
            subject_title = itemView.findViewById(R.id.subject_title);
            time_done = itemView.findViewById(R.id.time_done);
        }

    }
}
