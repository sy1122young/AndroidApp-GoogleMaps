package com.example.assignment3try;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailsActivity extends AppCompatActivity {
    private RequestQueue mQueue;
    String preview;

    /**
     * On create function for the activity, setting up the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        //hide action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        //get the id of the camera for the GET request
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            displayCam(extras.getString("id"));
        }
    }

    /**
     * Displays a camera given an id in string form
     * @param camId The id of the requested camera
     */
    public void displayCam(String camId){
        mQueue = Volley.newRequestQueue(this);
        Log.d("hello", "preview1");
        //location withing 50km url of api // need to change to max to 5
        String url = getCamIdUrl(camId);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {//reading in json
                try {
                    //get resuilt
                    JSONObject jsonObjectResult = response.getJSONObject("result");
                    //get webcams array
                    JSONArray jsonArray = jsonObjectResult.getJSONArray("webcams");
                    JSONObject webcams = jsonArray.getJSONObject(0);
                    //Gets the title of camera in Json Object
                    String webcamInfo = webcams.getString("title");
                    //puts the information in the text view
                    TextView textView = findViewById(R.id.webcamInfo);
                    textView.setText(webcamInfo);
                    //gets the image
                    JSONObject image = webcams.getJSONObject("image");
                    JSONObject current = image.getJSONObject("current");
                    preview = current.getString("preview");
                    Log.d("hello", "preview"+preview);
                    requestImage(preview);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(request);
    }

    public static String getCamIdUrl(String camId) {
        String url = "https://api.windy.com/api/webcams/v2/list/webcam=" + camId +
                "?show=webcams:location,image;&key=a8QislEEDfgpF3c48QIIUeJWzqXf8K6X";
        return url;
    }

    /**
     * Requests the specified image, adding it to the request queue if able
     * @param image the image to request
     */
    public void requestImage(String image){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        ImageRequest imageRequest= new ImageRequest(image,new Response.Listener<Bitmap>(){
            @Override
            public void onResponse(Bitmap response) {
                ImageView camImage = (ImageView) findViewById(R.id.webcamView);
                camImage.setImageBitmap(response);
            }
        },0,0, ImageView.ScaleType.CENTER_CROP, null, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
            }
        });
        requestQueue.add(imageRequest);
    }
}