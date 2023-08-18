package com.example.assignment3try;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.assignment3try.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    FusedLocationProviderClient fusedLocationClient;

    //TODO
    //fix location bug
    //add search button

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    double lat;
    double lon;
    private ArrayList<Cam> camList;
    private RequestQueue mQueue;
    //Auto fill
    private static final String TAG = "Info :";

    /**
     * On create function for the activity, setting up the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        //call function
        if (hasPermissions(this)) {
            //Permissions have not yet been granted
            askForPermission();
        } else {
            //Permissions have already been granted
            getLastKnownLocation();
        }
        //Initialise the SDK
        String apiKey = " AIzaSyA5pUxD_2Xi1s-bga4itPVaq-VblEHmxg8";
        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(), apiKey);
        }
        PlacesClient placesClient = Places.createClient(this);
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(-37.789474095, 175.281099311),
                new LatLng(37.789474095, 175.281099311)
        ));
        //select a country
        autocompleteFragment.setCountries("NZ");
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                List<Address> addressList = null;
                //clear map
                mMap.clear();
                Geocoder geocoder = new Geocoder(getApplicationContext());
                Address address;
                try {
                    addressList = geocoder.getFromLocationName(place.getName(), 1);
                    address = addressList.get(0);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                //LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                lat = address.getLatitude();
                lon = address.getLongitude();
                LatLng NZ = new LatLng(lat,lon);
                mMap.addMarker(new MarkerOptions().position(NZ).title(place.getName()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(NZ));
                getWebCams();
                Weather();
            }

            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     * @param googleMap The map to display onto
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //for camera onclick
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener(){
            @Override
            public void onInfoWindowClick(Marker marker){
                if(marker.getTitle().equals("camera")){
                    //gets the cam id from the Tag
                    String camId = String.valueOf(marker.getTag());
                    Intent intent = new Intent(MapsActivity.this, DetailsActivity.class);
                    intent.putExtra("id", camId);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * Gets the bitmap image associated with a resource id in a given context
     * @param context The context to get the image from
     * @param vectorResId The id of the bitmap to get
     * @return The bitmap image associated with a resource id in a given context
     */
    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Checks if the app has any form of location permission
     * @return true if the app has permission, false otherwise
     */
    public static boolean hasPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Ask the user for fine or coarse location permission
     */
    public void askForPermission(){
        ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
            Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION,false);
            //check if permission granted
            if (fineLocationGranted != null && fineLocationGranted) {
                // Precise location access granted.
                //fusedLocationClient.getLastLocation();
                ImageView inputLocation = (ImageView) findViewById(R.id.locationIcon);
                inputLocation.setVisibility(View.INVISIBLE);
                getLastKnownLocation();
            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                // Only approximate location access granted.
                ImageView inputLocation = (ImageView) findViewById(R.id.locationIcon);
                inputLocation.setVisibility(View.VISIBLE);
            } else {
                // No location access granted.
                ImageView inputLocation = (ImageView) findViewById(R.id.locationIcon);
                inputLocation.setVisibility(View.VISIBLE);
            }
        });
        //Launch a location permission request
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    /**
     * Sets the marker to the last known location, and moves the camera there
     */
    public void getLastKnownLocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. This can sometimes be null.
                if (location != null) {
                    // Logic to handle location object
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    LatLng NZ = new LatLng(lat,lon);
                    //set the marker for last known location
                    mMap.addMarker(new MarkerOptions().position(NZ).title("Marker in NZ"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(NZ));
                    //zoom
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12.0f));
                    getWebCams();
                    Weather();
                }
                else {
                    // Handle a location not being found
                }
            }
        });
    }

    /**
     * Gets the locations with webcam images, then displaying icons for each designating their locations
     */
    public void getWebCams(){
        mQueue = Volley.newRequestQueue(this);
        //location withing 50km url of api // need to change to max to 5
        String url = getWebCamUrl(lat, lon);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {//reading in json
                try {
                    //get result
                    JSONObject jsonObjectResult = response.getJSONObject("result");
                    //get webcams array
                    JSONArray jsonArray = jsonObjectResult.getJSONArray("webcams");
                    camList = new ArrayList<Cam>();
                    for(int i = 0; i<jsonArray.length(); i++){
                        camList.add(jsonToCam(jsonArray.getJSONObject(i)));
                    }
                    displayWebCams(camList, mMap);
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

    public static Cam jsonToCam(JSONObject jsonCam) throws JSONException {
        try {
            String id = jsonCam.getString("id");
            JSONObject location = jsonCam.getJSONObject("location");
            double latitude = location.getDouble("latitude");
            double longitude = location.getDouble("longitude");
            return new Cam(id,latitude,longitude);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Displays a series of icons on a google map, representing the items of an array of Cam objects
     * @param camList The array of cam objects to be displayed
     * @param mMap The google map to be displayed upon
     */
    public void displayWebCams(ArrayList<Cam> camList, GoogleMap mMap){
        //loop through arrays and add cams
        for(int i =0; i < camList.size(); i++){
            double latitude = camList.get(i).getlat();
            double longitude = camList.get(i).getlon();
            LatLng NZ = new LatLng(latitude,longitude);
            Marker marker = mMap.addMarker(new MarkerOptions().position(NZ).title("camera").icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.camera)));
            marker.setTag(camList.get(i).getid());
        }
    }

    /**
     * Displays the weather in a location
     */
    public void Weather(){
        //https://api.openweathermap.org/data/2.5/weather?lat=&lon=10.99&appid=f312c527574103fdab33428dc72292f5
        mQueue = Volley.newRequestQueue(this);
        String url = getWeatherUrl(lat, lon);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {//reading in the json
                weatherResponce(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(request);
    }

    private void weatherResponce(JSONObject response) {
        try {
            JSONArray weather = response.getJSONArray("weather");
            JSONObject WeatherCondition = weather.getJSONObject(0);
            String WeatherCD = WeatherCondition.getString("main");
            mMap.addMarker(getWeatherMarker(WeatherCD, lat, lon, this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Given a weather condition and coordinates, returns an appropriate marker.
     * @param condition The weather condition, returns null if there is no associated graphic for said weather condition. Not case sensitive.
     * @param latitude The marker's latitude to appear at.
     * @param longitude The marker's longitude to appear at.
     * @return An appropriate marker, or null if no such marker exists.
     */
    public static MarkerOptions getWeatherMarker(String condition, double latitude, double longitude, Context context) {
        //Get the markers position and graphics id
        LatLng position = new LatLng(latitude,longitude);
        int icon = context.getResources().getIdentifier(condition.toLowerCase(), "drawable", context.getPackageName());
        //return null if the id is 0, a.k.a there is no associated graphics. Otherwise return the marker.
        if (icon == 0) {
            return null;
        }
        return new MarkerOptions().position(position).icon(bitmapDescriptorFromVector(context.getApplicationContext(), icon));
    }

    public static String getWebCamUrl(double latitude, double longitude) {
        String url = "https://api.windy.com/api/webcams/v2/list/limit=5,0/nearby="
                + latitude + "," + longitude +
                ",50?show=webcams:location;?&key=a8QislEEDfgpF3c48QIIUeJWzqXf8K6X";
        return url;
    }

    public static String getWeatherUrl(double latitude, double longitude) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat="
                + latitude + "&lon=" + longitude + "&appid=f312c527574103fdab33428dc72292f5";
        return url;
    }

    /**
     * Informs the user that they need to give the app permission to check for last known location
     * @param view The view
     */
    public static void manualLocation(View view, Context context){
        //ImageView inputLocation = (ImageView) findViewById(R.id.locationIcon);
        //inputLocation.setVisibility(View.INVISIBLE);
        Toast.makeText(context, "give this app permission to check for last known location",
                Toast.LENGTH_LONG).show();
    }
}
