package spd.com.myapplication;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * Created by linus on 17-9-30.
 */

public class WordDetailsModel {

    private static WordDetailsModel instance;

    private WordDetailsModel(){

    }

    public static WordDetailsModel getInstance() {
        if (instance == null){
            instance = new WordDetailsModel();
        }

        return instance;
    }

    WordDetails getWordDetails(Context context, String word){

        String url = "https://api.shanbay.com/bdc/search/?word=" + word;

        final WordDetails[] wordDetails = new WordDetails[1];
        final Gson gson = new Gson();
        final RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.w("TAG", response.toString());
                        wordDetails[0] = gson.fromJson(response.toString(), WordDetails.class);
                        Log.w("TAG", wordDetails[0].toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        });

        requestQueue.add(jsonObjectRequest);

        return wordDetails[0];
    }

}
