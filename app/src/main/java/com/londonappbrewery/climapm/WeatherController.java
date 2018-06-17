package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.UrlQuerySanitizer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpRequest;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpClientConnection;


public class WeatherController extends AppCompatActivity {
        // Memeber Varibles Debugging purposes



    // Constants:
    final int REQUEST_CODE = 123;
    final String LOGCAT_TAG = "Clima";


    // App ID to use OpenWeather data
    final String APP_ID = "&appid=c4d51c0453758b3c64515ce293808319";
    // Time between location updates (5000 milliseconds or 5 seconds)

    String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?lat=";
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:
    String Location_Provider = LocationManager.NETWORK_PROVIDER;


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


        // TODO: Add an OnClickListener to the changeCityButton here:

        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (WeatherController.this, ChangeCity.class);
                startActivity(intent);
            }
        });

    }


    // TODO: Add onResume() here:

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Clima", "oneResume() called");
        Log.d("Clima", "Getting weather");
        Intent myIntent = getIntent();
        String city = myIntent.getStringExtra("City"); // retrive the city name

        // todo make API call to open weather map
        if (city != null) {
            getWeatherForNewCity( city);

        } else {


            getWeatherForCurrentLocation();
        }
    }

    // TODO: Add getWeatherForNewCity(String city) here:
    private void getWeatherForNewCity (String city){


        String newUrl = "http://api.openweathermap.org/data/2.5/weather?q="+city+APP_ID;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(newUrl, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Log.d(LOGCAT_TAG, "Success! JSON: " + response.toString());
                WeatherDataModel weatherDataMode = WeatherDataModel.fromJson(response);
                updateUI(weatherDataMode);



            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {

                Log.e(LOGCAT_TAG, "Fail " + e.toString());
                Toast.makeText(WeatherController.this, "Request Failed", Toast.LENGTH_SHORT).show();

                Log.d(LOGCAT_TAG, "Status code " + statusCode);
                Log.d(LOGCAT_TAG, "Here's what we got instead " + response.toString());
            }

        });

    }


    // TODO: Add getWeatherForCurrentLocation() here:
    private void getWeatherForCurrentLocation() {

        Log.d("Clima", "getting weather for Current Location");
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Clima", "onLocationChanged() callback recieved");
                String longtitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());
                Log.d("Clima", "the longitude is"+ longtitude);
                Log.d("Clima","The latitude is "+ latitude);

                RequestParams params = new RequestParams(); // this is from the libray that we used
                params.put("lat",latitude);
                params.put("long",longtitude);
                params.put("APPId",APP_ID);
                //api.openweathermap.org/data/2.5/weather?lat=35&lon=139

                WEATHER_URL += latitude + "&lon="+longtitude+APP_ID;// updating url


                letsDoSomeNetworking ( params);


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Clima", "OnProviderDisabled() callback");
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(this,new String [] {Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_CODE); // request the permision from the user
            return;
        }
        mLocationManager.requestLocationUpdates(Location_Provider, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    //  check the result of the permision

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE){

            if ( grantResults.length >0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){

                Log.d("Clima","Permision is Granted On request Permision Result ()");

                // now the app has the permision

                getWeatherForCurrentLocation();



            }
            else{
                Log.d("Clima","permisison has been denied");
            }

        }
    }


    // TODO: Add letsDoSomeNetworking(RequestParams params) here:

        private void letsDoSomeNetworking (RequestParams params){


            AsyncHttpClient client = new AsyncHttpClient();
            client.get(WEATHER_URL, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    Log.d(LOGCAT_TAG, "Success! JSON: " + response.toString());
                    WeatherDataModel weatherDataMode = WeatherDataModel.fromJson(response);
                    updateUI(weatherDataMode);



                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {

                    Log.e(LOGCAT_TAG, "Fail " + e.toString());
                    Toast.makeText(WeatherController.this, "Request Failed", Toast.LENGTH_SHORT).show();

                    Log.d(LOGCAT_TAG, "Status code " + statusCode);
                    Log.d(LOGCAT_TAG, "Here's what we got instead " + response.toString());
                }

            });




        }


        // TODO: Add updateUI() here:

        private  void updateUI(WeatherDataModel weather){
        mCityLabel.setText(weather.getmCity());
        mTemperatureLabel.setText(WeatherDataModel.getmTempeture());


        int resourceId = getResources().getIdentifier(weather.getmIconName(),"drawable",getPackageName());

        mWeatherImage.setImageResource(resourceId);

        }


        // TODO: Add onPause() here:


    @Override
    protected void onPause() {
        super.onPause();
        if (mLocationManager != null)
            mLocationManager.removeUpdates(mLocationListener);
    }
}