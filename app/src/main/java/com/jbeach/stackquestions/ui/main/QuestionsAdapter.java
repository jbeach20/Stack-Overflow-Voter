package com.jbeach.stackquestions.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jbeach.stackquestions.data.Question;
import com.jbeach.stackquestions.R;

import java.util.List;

public class QuestionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface QuestionSelected{
        void selected(Question question);
    }

    private Context context;
    private List<Question> questions;
    private QuestionSelected questionSelected;


    QuestionsAdapter(Context context, List<Question> questions) {
        this.context = context;
        this.questions = questions;
    }

    void setQuestionSelected(QuestionSelected selected){
        this.questionSelected = selected;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

        final Question q = questions.get(position);

        ((ViewHolder)holder).questionTitle.setText(q.getTitle());
        ((ViewHolder)holder).askedAt.setText(q.getAskedAt());
        ((ViewHolder)holder).createdBy.setText(q.getAskedBy());
        ((ViewHolder)holder).numAnswers.setText(Integer.toString(q.getAnswerCount()));

        ((ViewHolder)holder).itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                questionSelected.selected(q);
            }
        });

        //checks if we're in history or questions tab
        if(q.getVotedAnswerId() != null){
            ((ViewHolder)holder).votedRight.setVisibility(View.VISIBLE);
            if(q.isUserWasRight()){
                ((ViewHolder)holder).votedRight.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_box_green_24dp));
            }else{
                ((ViewHolder)holder).votedRight.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_clear_red_24dp));
            }
        }else{
            ((ViewHolder)holder).votedRight.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView ;

        // Inflate the custom layout
        contactView  = inflater.inflate(R.layout.list_question, parent, false);
        return new ViewHolder(contactView);

    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView questionTitle;
        TextView createdBy;
        TextView askedAt;
        TextView numAnswers;
        ImageView votedRight;

        ViewHolder(View itemView) {
            super(itemView);
            questionTitle = itemView.findViewById(R.id.question_title);
            createdBy = itemView.findViewById(R.id.asked_by);
            askedAt = itemView.findViewById(R.id.asked_at);
            numAnswers = itemView.findViewById(R.id.num_answers);
            votedRight = itemView.findViewById(R.id.user_voted_right);
        }
    }


}
