package com.jbeach.stackquestions.ui.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.jbeach.stackquestions.data.Question;
import com.jbeach.stackquestions.ui.questions.QuestionActivity;
import com.jbeach.stackquestions.R;
import com.jbeach.stackquestions.data.VotingDatabase;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Question> questions = new ArrayList<>();
    private QuestionsAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TextView numRight;
    private TextView numWrong;
    private TextView percent;

    private VotingDatabase db;

    static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getContext() != null) {
            db = Room.databaseBuilder(getContext(), VotingDatabase.class, "voting")
                    .addMigrations(VotingDatabase.MIGRATION_1_2)
                    .build();
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = root.findViewById(R.id.recycler_view_questions);
        numRight = root.findViewById(R.id.num_right);
        numWrong = root.findViewById(R.id.num_wrong);
        percent = root.findViewById(R.id.percent);
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh);

        LinearLayoutManager layoutManager  = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());

        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadAllInfo();
            }
        });

        root.findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure you want to delete your history? This will also clear your score.");
                builder.setPositiveButton("Clear History", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        Toast.makeText(getContext(), "Clearing history...", Toast.LENGTH_SHORT).show();
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                db.clearAllTables();
                                if(getActivity() != null)
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                            }
                        });
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        loadAllInfo();

        return root;
    }

    //Loads questions and stats from ROOM db
    private void loadAllInfo(){

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //convert to doubles for division math
                final double right = Double.valueOf(db.questionsDao().getSumCorrect());
                final double wrong = Double.valueOf(db.questionsDao().getSumIncorrect());

                if(getActivity() != null)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int r = (int)right;
                            int w = (int)wrong;
                            numRight.setText(Integer.toString(r));
                            numWrong.setText(Integer.toString(w));
                            percent.setText(Math.round(right/(right+wrong) * 100)+"%");

                        }
                    });
            }
        });

        new GetHistoryTask().execute();

    }

    class GetHistoryTask extends AsyncTask<Void, Void, List<Question>>{

        @Override
        protected List<Question> doInBackground(Void... voids) {
            return db.questionsDao().getAll();
        }

        @Override
        protected void onPostExecute(List<Question> questions) {
            HistoryFragment.this.questions = questions;
            Log.i("Questions", Integer.toString(questions.size()));
            adapter = new QuestionsAdapter(getContext(), HistoryFragment.this.questions);
            adapter.setQuestionSelected(new QuestionsAdapter.QuestionSelected() {
                @Override
                public void selected(Question question) {
                    Intent intent = new Intent(getActivity(), QuestionActivity.class);
                    intent.putExtra("question_id", Integer.toString(question.getId()));
                    intent.putExtra("can_vote", false);
                    intent.putExtra("did_vote_correctly", question.isUserWasRight());
                    intent.putExtra("user_answer_id", question.getVotedAnswerId());

                    startActivity(intent);
                }
            });
            swipeRefreshLayout.setRefreshing(false);
            recyclerView.setAdapter(adapter);
            super.onPostExecute(questions);
        }
    }



}
