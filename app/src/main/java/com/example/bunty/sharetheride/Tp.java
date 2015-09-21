package com.example.bunty.sharetheride;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by bunty on 9/19/2015.
 */
public class Tp extends Activity {

    AutoCompleteTextView atvPlaces, atvPlaces_dest;
    PlacesTask placesTask;
    ParserTask parserTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autocomplete);

        atvPlaces = (AutoCompleteTextView) findViewById(R.id.atv_places);
        atvPlaces.setThreshold(1);

        //Updated by Shikha Jain 20/9/15
        atvPlaces_dest = (AutoCompleteTextView) findViewById(R.id.atv_places_dest);
        atvPlaces_dest.setThreshold(1);

        atvPlaces.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesTask = new PlacesTask();
                placesTask.execute(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        atvPlaces_dest.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesTask = new PlacesTask();
                placesTask.execute(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });
        //end update Shikha Jain 20/9/15
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches all places from GooglePlaces AutoComplete Web Service
    private class PlacesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=AIzaSyDbqzXfYEJIMe5Py6gm4vaE5bHoZ-jy27o";

            String input="";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }

            // place type to be searched
            String types = "types=geocode";

            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = input+"&"+types+"&"+sensor+"&"+key;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;
            Log.d("url is",url);

            try{
                // Fetching the data from we service
                data = downloadUrl(url);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Creating ParserTask
            parserTask = new ParserTask();

            // Starting Parsing the JSON string returned by Web Service
            parserTask.execute(result);
        }
    }
    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);

                // Getting the parsed data as a List construct
                places = placeJsonParser.parse(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            String[] from = new String[] { "description"};
            int[] to = new int[] { android.R.id.text1 };

            // Creating a SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

            // Setting the adapter
            atvPlaces.setAdapter(adapter);
            atvPlaces_dest.setAdapter(adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
//Update by Shikha Jain 21/9/15
    public void ShowOnMap(View V){
        AutoCompleteTextView src_atc = (AutoCompleteTextView)findViewById(R.id.atv_places);
        AutoCompleteTextView dest_atc = (AutoCompleteTextView)findViewById(R.id.atv_places_dest);

        String src_addr = src_atc.getText().toString();
        String dest_addr = dest_atc.getText().toString();

        Geocoder gc = new Geocoder(this, Locale.getDefault());
        String  result = "", result1 = "";
        Address address = null, address_d= null;
       // LatLng src_pts = new LatLng(0,0);
        //LatLng dest_pts= new LatLng(0,0);
        try {
            List addressList = gc.getFromLocationName(src_addr, 1);
            if (addressList != null && addressList.size() > 0) {
                address = (Address) addressList.get(0);
                StringBuilder sb = new StringBuilder();
                sb.append(address.getLatitude()).append("\n");
                sb.append(address.getLongitude()).append("\n");
                result = sb.toString();
                //src_pts = new LatLng(address.getLatitude(),address.getLongitude());
            }
            List addressList_d = gc.getFromLocationName(dest_addr, 1);
            if (addressList_d != null && addressList_d.size() > 0) {
                address_d = (Address) addressList_d.get(0);
                StringBuilder sb_d = new StringBuilder();
                sb_d.append(address_d.getLatitude()).append("\n");
                sb_d.append(address_d.getLongitude()).append("\n");
                result1 = sb_d.toString();
                //dest_pts = new LatLng(address_d.getLatitude(),address_d.getLongitude());
            }

            Intent i = new Intent(this,GetDirection.class);
            i.putExtra("src_lat", String.valueOf(address.getLatitude()));
            i.putExtra("src_long", String.valueOf(address.getLongitude()));
            i.putExtra("dest_lat", String.valueOf(address_d.getLatitude()));
            i.putExtra("dest_long", String.valueOf(address_d.getLongitude()));
            startActivity(i);


        }
        catch(Exception e){
            Log.e("Error",e.toString());
            }
        finally {
            Toast.makeText(Tp.this, result+"\n"+result1, Toast.LENGTH_SHORT).show();
        }



    }

    //end update by SHikha Jain 21/9/15
}