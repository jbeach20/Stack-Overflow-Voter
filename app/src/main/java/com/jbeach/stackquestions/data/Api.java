package com.jbeach.stackquestions.data;


import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Api {

    public interface Listener{
        void onSuccess(JSONObject object);
        void onFailure(ApiError error);
    }

    public static class ApiHandler implements Listener{
        @Override
        public void onSuccess(JSONObject object) {
            Log.i("Success", object.toString());
        }

        @Override
        public void onFailure(ApiError error) {
            Log.e("Error", error.devMessage);
        }
    }

    private Api(){

    }

    private static final String BASE_URL = "https://api.stackexchange.com/2.2";
    private static final String KEY = "?site=stackoverflow&order=desc&sort=activity&key=S)P5MqMWhfZEnKI4*O8VTg((";

    /**
     * Returns a list of questions form stack overflow
     * @param listener -
     */
    public static void getQuestionList(Context context, String query, Listener listener){
        get(context, "/search/advanced?order=desc&sort=activity&q="+query+"&accepted=True&answers=2&site=stackoverflow&key=S)P5MqMWhfZEnKI4*O8VTg((", listener, false);//+"?sort=insert_sequence,DESC&fields=*"
    }

    /**
     * Returns a detailed question
     * @param listener -
     */
    public static void getQuestion(Context context, String qId, Listener listener){
        get(context, "/questions/"+qId+KEY, listener, true);//+"?sort=insert_sequence,DESC&fields=*"
    }

    /**
     * Returns a detailed question
     * @param listener -
     */
    public static void getPost(Context context, String pId, Listener listener){
        get(context, "/posts/"+pId+KEY, listener, true);//+"?sort=insert_sequence,DESC&fields=*"
    }

    /**
     * Returns a the list of answers
     * @param listener -
     */
    public static void getAnswers(Context context, String qId, Listener listener){
        get(context, "/questions/"+qId+"/answers"+KEY, listener, true);//+"?sort=insert_sequence,DESC&fields=*"
    }

    private static void get(Context context, String url, final Listener listener, boolean withBody){

        if(isNetworkConnected(context)) {

            String builtUrl = BASE_URL + url;

            if (withBody) {
                builtUrl += "&filter=withbody";
            }

            Request request = new Request.Builder().url(builtUrl).build();

            OkHttpClient client = new OkHttpClient();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e("clientonFailure", e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    if (!response.isSuccessful()) {
                        listener.onFailure(new ApiError(""));
                        throw new IOException("Unexpected code " + response);
                    }

                    final String responseData = response.body().string();

                    if (listener != null) {
                        if (response.body() != null) {
                            try {
                                listener.onSuccess(new JSONObject(responseData));
                                return;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            listener.onFailure(new ApiError("Error", "Error"));
                            return;
                        }
                        listener.onFailure(new ApiError("Error", "Error"));
                    }

                }
            });
        }else{
            Toast.makeText(context, "Cannot connect to the internet. Please check your connection", Toast.LENGTH_LONG).show();
        }
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm != null)
            return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
        else{
            return false;
        }
    }

    public static class ApiError{

        String userMessage;
        String devMessage;

        public ApiError(String userMessage, String devMessage){
            this.userMessage = userMessage;
            this.devMessage = devMessage;
        }
        public ApiError(String devMessage){
            this.userMessage = "Sorry, there was an error";
            this.devMessage = devMessage;
        }
    }

}

