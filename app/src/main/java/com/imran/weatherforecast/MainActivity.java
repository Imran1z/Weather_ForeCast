package com.imran.weatherforecast;

import  androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV, conditionTV;
    private TextInputEditText cityEdt;
    private ImageView iconIV, searchIV;
    private RecyclerView weatherRV;

    private WeatherRVAdapter weatherRVAdapter;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;

    private LocationManager locationManager;
    private int PERMISSION_CODE=1;
    private String cityName;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);




        homeRL =findViewById(R.id.RelativeLayoutHome);
        loadingPB =findViewById(R.id.ProgressBar);
        cityNameTV =findViewById(R.id.TextViewCityName);
        temperatureTV =findViewById(R.id.TextViewTemperature);
        conditionTV =findViewById(R.id.TextViewCondition);
        cityEdt =findViewById(R.id.TextInputEditTextCity);
        iconIV =findViewById(R.id.ImageViewIcon);
        searchIV =findViewById(R.id.ImageViewSearch);
        weatherRV =findViewById(R.id.RecyclerviewWeather);

        weatherRVModelArrayList=new ArrayList<>();
        weatherRVAdapter=new WeatherRVAdapter(this,weatherRVModelArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);

        }
        Location location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName=getCityName(location.getLongitude(),location.getLatitude());
        getWeatherInfo(cityName);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city= cityEdt.getText().toString();
                if (city.isEmpty()){
                    Toast.makeText(MainActivity.this,"Enter a city",Toast.LENGTH_SHORT).show();

                }else{
                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSION_CODE){
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission granted",Toast.LENGTH_SHORT).show();

            }else {
                Toast.makeText(MainActivity.this,"Please give the required permissions",Toast.LENGTH_SHORT).show();
                finish();

            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName="Not found";

        Geocoder gcd=new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses=gcd.getFromLocation(latitude,longitude,10);

            for (Address adr:addresses){
                if (adr!=null){
                    String city=adr.getLocality();
                    if (city!=null && !city.equals("")){
                        cityName=city;
                    }
                    else {
                        Log.d("TAG","City not found");
                       // Toast.makeText(this,"user City not found",Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(MainActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        return cityName;
    }

    private void getWeatherInfo(String cityName){
       // String url="http://api.weatherapi.com/v1/forecast.json?key=dc6f1173669a410fb7895719230602&q="+cityName+"&days=1&aqi=no&alerts=no";
        String url="http://api.weatherapi.com/v1/forecast.json?key=dc6f1173669a410fb7895719230602&q="+cityName+"&days=1&aqi=no&alerts=no";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue= Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();

                try {
                    String temperature =response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature+"°C");

                    int isDay=response.getJSONObject("current").getInt("is_day");
                    String condition =response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon =response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);

                    //something

                    JSONObject forecastObj =response.getJSONObject("forecast");
                    JSONObject forecastO=forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray=forecastO.getJSONArray("hour");
                    for (int i=0;i<hourArray.length();i++){
                        JSONObject hourObj=hourArray.getJSONObject(i);
                        String time=hourObj.getString("time");
                        String temp=hourObj.getString("temp_c");
                        String img=hourObj.getJSONObject("condition").getString("icon");
                        String wind=hourObj.getString("wind_kph");

                        weatherRVModelArrayList.add(new WeatherRVModel(time,temp,img,wind));

                    }
                    weatherRVAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

               // Toast.makeText(MainActivity.this,"Please enter a valid city..",Toast.LENGTH_SHORT).show();
                VolleyLog.d("TAG", "Error: " + error.getMessage());
            }
        });

        requestQueue.add(jsonObjectRequest);


    }
}