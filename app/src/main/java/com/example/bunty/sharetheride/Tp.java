package com.example.bunty.sharetheride;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
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

    EditText date, timehh, timess, vehicle,numseats, fare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autocomplete);

        StrictMode.ThreadPolicy tr = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(tr);

        atvPlaces = (AutoCompleteTextView) findViewById(R.id.atv_places);
        atvPlaces.setThreshold(1);

        //Updated by Shikha Jain 20/9/15
        atvPlaces_dest = (AutoCompleteTextView) findViewById(R.id.atv_places_dest);
        atvPlaces_dest.setThreshold(1);

        date = (EditText)findViewById(R.id.txtdate);
        timehh = (EditText)findViewById(R.id.txthh);
        timess = (EditText)findViewById(R.id.txtss);
        vehicle = (EditText)findViewById(R.id.vehicletype);
        numseats = (EditText)findViewById(R.id.numseats);
        fare = (EditText)findViewById(R.id.fare);


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
            Log.d("Exception", " Exception while downloading url"+e.toString());
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

        //to get Lat Long of the text selected in Autocomplete
        Geocoder gc = new Geocoder(this, Locale.getDefault());
        String  result = "", result1 = "";
        Address address = null, address_d= null;
       // LatLng src_pts = new LatLng(0,0);
        //LatLng dest_pts= new LatLng(0,0);
        //finding Source and Destination names's  Lat,Long
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
// sending Latitude and longtude values of source and destination to get deirection
            Intent i = new Intent(this,GetDirection.class);
            i.putExtra("src_lat", String.valueOf(address.getLatitude()));
            i.putExtra("src_long", String.valueOf(address.getLongitude()));
            i.putExtra("dest_lat", String.valueOf(address_d.getLatitude()));
            i.putExtra("dest_long", String.valueOf(address_d.getLongitude()));
            // For getting current lat and long from the map.
            startActivityForResult(i, 101);
        }
        catch(Exception e){
            Log.e("Error",e.toString());
            }
        finally {
            Toast.makeText(Tp.this, result+"\n"+result1, Toast.LENGTH_SHORT).show();
        }
       // send_OfferRideData();

    }

    //end update by SHikha Jain 21/9/15

    //update by Shikha Jain 23/9/15
    public void onActivityResult(int req_id, int res_id, Intent io)
    {
        if (req_id == res_id) {
            Bundle b1 = io.getExtras();

            HttpClient hclient = new DefaultHttpClient();
            HttpPost post_url = new HttpPost("http://allrounderservices.com/mypool/offer_ride.php");

            List<NameValuePair> data_list = new ArrayList<NameValuePair>();
            data_list.add(new BasicNameValuePair("userId", "1"));
            data_list.add(new BasicNameValuePair("source", atvPlaces.getText().toString()));
            data_list.add(new BasicNameValuePair("destination", atvPlaces_dest.getText().toString()));
            data_list.add(new BasicNameValuePair("date", date.getText().toString()));
            data_list.add(new BasicNameValuePair("time", timehh.getText().toString() + ":" + timess.getText().toString() + ":00"));
            data_list.add(new BasicNameValuePair("latitude", String.valueOf(b1.getDouble("mylat"))));
            data_list.add(new BasicNameValuePair("longitude", String.valueOf(b1.getDouble("mylong"))));
            data_list.add(new BasicNameValuePair("vehicleId", "1"));
            data_list.add(new BasicNameValuePair("seats", numseats.getText().toString()));
            data_list.add(new BasicNameValuePair("fare", fare.getText().toString()));

            try {

                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(data_list);
                post_url.setEntity(entity);
                HttpResponse send_response = hclient.execute(post_url);

                BufferedReader br = new BufferedReader(new InputStreamReader(send_response.getEntity().getContent()));
                String line = br.readLine();
                Toast.makeText(this, line, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        else
            Log.d("Req_id",res_id+","+req_id);
    }
    //end update by Shikha Jain 23/9/15
}