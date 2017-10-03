package spd.com.shanbaytest.models;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONObject;

import spd.com.shanbaytest.models.Bean.WordDetails;

/**
 * Created by linus on 17-9-30.
 */

public class WordDetailsModel {

    private static WordDetailsModel instance;

    public interface GetDetailsListener{

        void success(WordDetails wordDetails);
        void fail(Object fail);
    }

    private WordDetailsModel(){

    }

    public static WordDetailsModel getInstance() {
        if (instance == null){
            instance = new WordDetailsModel();
        }

        return instance;
    }

    /*加载word details并通过gson进行解析
     */
    public void getWordDetails(Context context, String word, final GetDetailsListener listener){

        String url = "https://api.shanbay.com/bdc/search/?word=" + word;

        final Gson gson = new Gson();
        final RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        WordDetails wordDetails = gson.fromJson(response.toString(), WordDetails.class);
                        listener.success(wordDetails);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.fail(error);
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

}
