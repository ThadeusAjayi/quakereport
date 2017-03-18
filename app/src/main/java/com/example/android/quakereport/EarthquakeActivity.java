/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

//import static com.example.android.quakereport.QueryUtils.extractEarthquakes;

public class EarthquakeActivity extends AppCompatActivity {

    private static final String LOCATION_SEPARATOR = " of ";

    private static String locationOffset;

    private static String primaryLocation;

    private static String locationDisplay;

    private static String url;

    ListView earthquakeListView;





    /** Sample JSON response for a USGS query */
    private static final String SAMPLE_JSON_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&orderby=time&minmag=6&limit=10";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_image);

        earthquakeAsync task = new earthquakeAsync();
        task.execute();


    }

    private class earthquakeAsync extends AsyncTask<String, Void, ArrayList<Quake>> {


        @Override
        protected ArrayList<Quake> doInBackground(String... url) {

            ArrayList result;

            URL quakeUrl = createUrl(SAMPLE_JSON_URL);

            String JSONResponse = "";

            try {
                JSONResponse = makeHttpRequest(quakeUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }

            result = extractEarthquakes(JSONResponse);

            return result;

        }

        @Override
        protected void onPostExecute(ArrayList<Quake> result) {

            setContentView(R.layout.earthquake_activity);

       //     final ArrayList<Quake> earthquakes = extractEarthquakes(result);

            // Find a reference to the {@link ListView} in the layout
            earthquakeListView = (ListView) findViewById(R.id.list);


            // Create a new {@link ArrayAdapter} of earthquakes
            final QuakeAdapter adapter = new QuakeAdapter(EarthquakeActivity.this, result);


            // Set the adapter on the {@link ListView}
            // so the earthquake_list_item can be populated in the user interface
            earthquakeListView.setAdapter(adapter);

            earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                Quake currentQuake;
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                    // Find the current earthquake that was clicked on
                    currentQuake = adapter.getItem(position);

                    // Convert the String URL into a URI object (to pass into the Intent constructor)
                    Uri earthquakeUri = Uri.parse(currentQuake.getmUrl());

                    // Send the intent to launch a new activity
                    Intent webIntent = new Intent(Intent.ACTION_VIEW,earthquakeUri);

                    //start activity
                    startActivity(webIntent);
                }

            });

            //super.onPostExecute(quakes);
        }
    }


    public URL createUrl(String url) {
        URL quakeUrl = null;
        try {
            quakeUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        return quakeUrl;
    }

    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            jsonResponse = readFromStream(inputStream);

        } catch (IOException e) {
            // TODO: Handle the exception
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    public static ArrayList<Quake> extractEarthquakes(String response) {

        // Create an empty ArrayList that we can start adding earthquakes to
        ArrayList<Quake> earthquakes = new ArrayList<>();


        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            JSONObject JSONroot = new JSONObject(response);

            JSONArray featuresArray = JSONroot.getJSONArray("features");

            for(int i=0; i < featuresArray.length(); i++) {

                JSONObject JSONobject = featuresArray.getJSONObject(i);

                JSONObject properties = JSONobject.getJSONObject("properties");

                double  magnitude = properties.getDouble("mag");

                String location = properties.getString("place");

                Long time = properties.getLong("time");

                url = properties.getString("url");

                Log.v("url",url);

                //Create a date object of the time
                Date dateObject = new Date(time);

                //Define the date format
                SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM DD, yyyy");

                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");

                //format date object to string
                String dateToDisplay = dateFormatter.format(dateObject);

                String timeToDisplay = timeFormat.format(dateObject);

                String timeDisplay = dateToDisplay + "\n" + timeToDisplay;

                String[] stringPart =location.split(LOCATION_SEPARATOR);

                if(location.contains(LOCATION_SEPARATOR)){

                    locationOffset = stringPart[0] + LOCATION_SEPARATOR;

                    primaryLocation = stringPart[1].toUpperCase();

                    Log.v("LocationOffset", locationOffset + " " + primaryLocation);

                    locationDisplay = locationOffset + "\n" + primaryLocation;

                    location = locationDisplay;
                }

                Quake earthQuake = new Quake(magnitude,location,timeDisplay,url);
                earthquakes.add(earthQuake);

            }

            // TODO: Parse the response given by the SAMPLE_JSON_RESPONSE string and
            // build up a list of Earthquake objects with the corresponding data.


        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of earthquakes
        return earthquakes;
    }



}
