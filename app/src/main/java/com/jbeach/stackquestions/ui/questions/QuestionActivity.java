package com.jbeach.stackquestions.ui.questions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jinatonic.confetti.CommonConfetti;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jbeach.stackquestions.R;
import com.jbeach.stackquestions.data.Answer;
import com.jbeach.stackquestions.data.Api;
import com.jbeach.stackquestions.data.Question;
import com.jbeach.stackquestions.data.VotingDatabase;
import com.jbeach.stackquestions.ui.main.QuestionsFragment;
import com.sothree.slidinguppanel.ScrollableViewHelper;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class QuestionActivity extends AppCompatActivity {

    int previousVote = -1;
    int userAnswerPos = -1;
    int acceptedAnswerPos;

    Answer currentVote;
    String questionId;
    String acceptedAnswerId;
    TextView title;
    TextView qAskedBy;
    TextView qViewCount;
    TextView numAnswers;
    TextView needHelpText;
    WebView body;
    SlidingUpPanelLayout slidingUpPanelLayout;
    ProgressBar progressBarAnswers;
    ProgressBar progressBarQuestion;

    RecyclerView recyclerView;
    ArrayList<Answer> answers = new ArrayList<>();
    AnswersAdapter adapter;
    Context context = this;
    FloatingActionButton voteFab;

    VotingDatabase db;
    Question question;

    boolean needsHelp = false;
    boolean canVote = true;
    int userDidVoteAnswerId = -1;
    /** Used to show results ON VIEWING HISTORY*/
    boolean userDidVoteCorrectly = false;
    /** Used to show results RIGHT AFTER voting*/
    boolean showAnswers = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        setTitle("Select Answer");
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.ic_close_white_24dp));
        }

        title = findViewById(R.id.title1);
        qAskedBy = findViewById(R.id.asked);
        qViewCount = findViewById(R.id.view_count);
        body = findViewById(R.id.body);
        numAnswers = findViewById(R.id.num_answers);
        voteFab = findViewById(R.id.confirm_vote);
        needHelpText = findViewById(R.id.need_help);
        slidingUpPanelLayout = findViewById(R.id.sliding_panel);
        slidingUpPanelLayout.setAnchorPoint(.9f);
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
        slidingUpPanelLayout.setScrollableViewHelper(new WebViewScrollableViewHelper());
        recyclerView = findViewById(R.id.recycler_view_answers);
        progressBarAnswers = findViewById(R.id.loading_indicator_answers);
        progressBarQuestion = findViewById(R.id.loading_indicator_question);

        adapter = new AnswersAdapter(context);
        LinearLayoutManager layoutManager  = new LinearLayoutManager(context);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());

        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);

        db = Room.databaseBuilder(context, VotingDatabase.class, "voting")
                .addMigrations(VotingDatabase.MIGRATION_1_2)
                .build();

        if(getIntent().getExtras() != null){

            questionId = getIntent().getStringExtra("question_id"); //NON-NLS
            canVote = getIntent().getBooleanExtra("can_vote", true); //NON-NLS

            if(!canVote){
                userDidVoteCorrectly = getIntent().getBooleanExtra("did_vote_correctly", false);
                userDidVoteAnswerId = Integer.parseInt(getIntent().getStringExtra("user_answer_id"));
                showAnswers = true;
                setTitle(userDidVoteCorrectly ? "You got this one right!" : "You got this one wrong");
            }

            Api.getQuestion(context, questionId, new Api.ApiHandler(){

                @Override
                public void onSuccess(JSONObject object) {
                    super.onSuccess(object);

                    try {

                        JSONObject item = object.getJSONArray("items").getJSONObject(0);

                        final String q = item.getString("title");
                        final String b = item.getString("body");
                        final int count = item.getInt("answer_count");
                        final int viewCount = item.getInt("view_count");
                        final String name = item.getJSONObject("owner").getString("display_name");
                        final String answeredAt = QuestionsFragment.dateToString(new Date(item.getLong("creation_date")));

                        question = new Question(Integer.parseInt(questionId), q,name, answeredAt, count);

                        QuestionActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //title.setText(q);
                                title.setText(q);
                                qAskedBy.setText(name);
                                qViewCount.setText(Integer.toString(viewCount));

                                numAnswers.setText(Integer.toString(count));
                                body.loadData(b,"text/html", "utf-8");
                                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                progressBarQuestion.setVisibility(View.GONE);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            Api.getAnswers(context, questionId, new Api.ApiHandler(){

                @Override
                public void onSuccess(JSONObject object) {
                    super.onSuccess(object);

                    try {

                        JSONArray items = object.getJSONArray("items");

                        for(int i = 0; i < items.length(); i++){
                            JSONObject a = items.getJSONObject(i);

                            final String id = a.getString("answer_id");
                            final String body = a.getString("body");
                            final int score = a.getInt("score");
                            final boolean isAccepted = a.getBoolean("is_accepted");
                            final String answeredBy = a.getJSONObject("owner").getString("display_name");
                            final String answeredAt = QuestionsFragment.dateToString(new Date(a.getLong("creation_date")));
                            if(isAccepted){
                                acceptedAnswerId = id;
                                acceptedAnswerPos = i;
                            }
                            if(userDidVoteAnswerId > -1){
                                if(Integer.parseInt(id) == userDidVoteAnswerId){
                                    userAnswerPos = i;
                                }
                            }
                            answers.add(new Answer(id, body, answeredBy, answeredAt, isAccepted, score));

                        }

                        QuestionActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //title.setText(q);
                                adapter.notifyDataSetChanged();
                                progressBarAnswers.setVisibility(View.GONE);
                            }
                        });


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }

        if(canVote)
            voteFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //makes sure an answer is selected
                    if(previousVote >= 0) {

                        canVote = false;
                        voteFab.hide();
                        voteFab.setOnClickListener(null);

                        question.setUserWasRight(currentVote.isAccepted());
                        question.setVotedAnswerId(currentVote.getId());
                        question.setCorrectAnswerId(acceptedAnswerId);

                        if(currentVote.isAccepted()){
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage("You voted for the right answer! Great job!\n\nYou can view your results from the home page.");
                            builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();

                            CommonConfetti.rainingConfetti(((RelativeLayout)findViewById(R.id.main_content)), new int[] {
                                    ContextCompat.getColor(context, R.color.stack_green),
                                    ContextCompat.getColor(context, R.color.stack_orange),
                                    ContextCompat.getColor(context, R.color.stack_blue),
                                    ContextCompat.getColor(context, R.color.stack_orange_dark)})
                                    .infinite();
                        }else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage("Sorry! You guessed wrong. Take a look at the correct answer (highlighted in green)\n\nYou can also view your results from the home page.");
                            builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    showAnswers = true;
                                    userAnswerPos = previousVote;
                                    adapter.notifyDataSetChanged();
                                    recyclerView.scrollToPosition(acceptedAnswerPos);
                                }
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }

                        try {
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    db.questionsDao().insert(question);
                                }
                            });

                        } catch (Exception e) {
                            Toast.makeText(context, "There was an error saving your vote! Please try again", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }

                    }
                }
            });

        needHelpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                needsHelp = true;
                adapter.notifyDataSetChanged();
                needHelpText.setVisibility(View.GONE);
            }
        });

    }

    private void setVote(String aId, int position){
        this.currentVote = answers.get(position);
        this.currentVote.setUserDidVote(true);

        if(previousVote >= 0){
            answers.get(previousVote).setUserDidVote(false);
        }else{
            voteFab.show();
        }

        adapter.notifyDataSetChanged();

        previousVote = position;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        WebView body;
        TextView createdBy;
        TextView askedAt;
        TextView position;
        TextView score;
        ImageView selectImage;

        ViewHolder(View itemView) {
            super(itemView);
            body = itemView.findViewById(R.id.body);
            createdBy = itemView.findViewById(R.id.asked_by);
            askedAt = itemView.findViewById(R.id.asked_at);
            selectImage = itemView.findViewById(R.id.select_image);
            position = itemView.findViewById(R.id.position);
            score = itemView.findViewById(R.id.score);
        }
    }


    public class AnswersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        Context context;

        public AnswersAdapter(Context context) {
            this.context = context;
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

            final Answer a = answers.get(position);

            ((ViewHolder)holder).body.loadData(a.getBody(),"text/html", "utf-8");
            ((ViewHolder)holder).askedAt.setText(a.getAnsweredAt());
            ((ViewHolder)holder).createdBy.setText(a.getAnsweredBy());
            ((ViewHolder)holder).position.setText(Integer.toString(position+1)+":");

            if(needsHelp) {
                ((ViewHolder) holder).score.setText(Integer.toString(a.getScore()));
            }else{
                ((ViewHolder) holder).score.setText("?");
            }

            if(canVote) {
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Go to question & answer activity
                        setVote(a.getId(), position);
                    }
                };
                ((ViewHolder) holder).itemView.setOnClickListener(onClickListener);
                ((ViewHolder) holder).body.setOnClickListener(onClickListener);

                if(a.isUserDidVote()){
                    ((ViewHolder)holder).itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.stack_green));
                    ((ViewHolder)holder).selectImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_box_white_24dp));

                }else{
                    ((ViewHolder)holder).itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.stack_background));
                    ((ViewHolder)holder).selectImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_box_outline_blank_black_24dp));
                }
            }else{

                ((ViewHolder) holder).itemView.setOnClickListener(null);
                ((ViewHolder) holder).body.setOnClickListener(null);

                if(showAnswers){
                    if(userDidVoteCorrectly){
                        if(position == userAnswerPos){
                            ((ViewHolder)holder).itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.stack_green));
                            ((ViewHolder)holder).selectImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_box_white_24dp));
                        }else{
                            ((ViewHolder)holder).itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.stack_background));
                            ((ViewHolder)holder).selectImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_box_outline_blank_black_24dp));
                        }
                    }else{
                        if(position == acceptedAnswerPos){
                            ((ViewHolder)holder).itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.stack_green));
                            ((ViewHolder)holder).selectImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_box_outline_blank_white_24dp));
                        }else if(position == userAnswerPos){
                            ((ViewHolder)holder).itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.stack_red));
                            ((ViewHolder)holder).selectImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_box_white_24dp));
                        }else{
                            ((ViewHolder)holder).itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.stack_background));
                            ((ViewHolder)holder).selectImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_box_outline_blank_black_24dp));
                        }
                    }
                }
            }


        }

        @Override
        public int getItemCount() {
            return answers.size()  ;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View contactView ;

            // Inflate the custom layout

            contactView  = inflater.inflate(R.layout.list_answer, parent, false);
            return new ViewHolder(contactView);

        }

        @Override
        public int getItemViewType(int position) {
            return 1;
        }


    }

    private class WebViewScrollableViewHelper extends ScrollableViewHelper {
        public int getScrollableViewScrollPosition(View scrollableView, boolean isSlidingUp) {
            if (scrollableView instanceof WebView) {
                if(isSlidingUp){
                    return scrollableView.getScrollY();
                } else {
                    WebView nsv = ((WebView) scrollableView);
                    View child = nsv.getChildAt(0);
                    return (child.getBottom() - (nsv.getHeight() + nsv.getScrollY()));
                }
            } else {
                return 0;
            }
        }
    }




}
