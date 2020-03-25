package com.jbeach.stackquestions.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jbeach.stackquestions.data.Api;
import com.jbeach.stackquestions.data.Question;
import com.jbeach.stackquestions.ui.questions.QuestionActivity;
import com.jbeach.stackquestions.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class QuestionsFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Question> questions = new ArrayList<>();
    private QuestionsAdapter adapter;
    SearchView searchView;
    ProgressBar progressBar;

    private static final String PREFERENCES = "stack_questions";
    private SharedPreferences sharedpreferences;
    public SharedPreferences.Editor editor;

    public static QuestionsFragment newInstance() {
        QuestionsFragment fragment = new QuestionsFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    static SimpleDateFormat readableDateFormatShort = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    public static String dateToString(Date date){
        return readableDateFormatShort.format(date);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        searchView = root.findViewById(R.id.search_view);
        progressBar = root.findViewById(R.id.loading_indicator);
        recyclerView = root.findViewById(R.id.recycler_view_questions);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                editor = sharedpreferences.edit();
                editor.putString("filter_query", query);
                editor.apply();
                getQuestions(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        adapter = new QuestionsAdapter(getContext(), questions);
        adapter.setQuestionSelected(new QuestionsAdapter.QuestionSelected() {
            @Override
            public void selected(Question question) {
                Intent intent = new Intent(getActivity(), QuestionActivity.class);
                intent.putExtra("question_id", Integer.toString(question.getId()));
                startActivity(intent);
            }
        });

        LinearLayoutManager layoutManager  = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());

        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);

        if(getContext() != null) {
            sharedpreferences = getContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
            String query = sharedpreferences.getString("filter_query", "");
            searchView.setQuery(query, false);
            getQuestions(query);
        }

        return root;
    }

    private void getQuestions(String query){

        progressBar.setVisibility(View.VISIBLE);
        questions.clear();

        Api.getQuestionList(getContext(), query, new Api.ApiHandler(){
            @Override
            public void onSuccess(JSONObject object) {
                super.onSuccess(object);

                try {
                    JSONArray items = object.getJSONArray("items");
                    for (int i = 0; i < items.length(); i++){

                        JSONObject item = items.getJSONObject(i);
                        int id = item.getInt("question_id");
                        String title = item.getString("title");
                        int count = item.getInt("answer_count");
                        String name = item.getJSONObject("owner").getString("display_name");
                        Date at = new Date(item.getLong("creation_date"));

                        questions.add(new Question(id, title, name, dateToString(at), count));
                    }
                    if(getActivity() != null)
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                adapter.notifyDataSetChanged();

                            }
                        });
                } catch (JSONException e) {
                    if(getActivity() != null)
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), "Sorry, there was an error handling the data", Toast.LENGTH_LONG).show();
                            }
                        });
                    e.printStackTrace();
                }

            }
        });
    }


}