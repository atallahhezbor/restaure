package com.example.atallahhezbor.restaure;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.CoordinateOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Call;
import retrofit.Response;

public class MainActivity extends AppCompatActivity {
    YelpAPI yelpAPI;
    public double latitude;
    public double longitude;
    public ArrayAdapter<String> restaurantsAdapter;
    public ArrayList<String> restaurantsList = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        // Set up array adapter and list
        restaurantsAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, restaurantsList);
        final ListView restaurantsView = (ListView) findViewById(R.id.restaurants);
        restaurantsView.setAdapter(restaurantsAdapter);

        // Get keys and set up api factory
        String consumerKey = getResources().getString(R.string.consumer_key);
        String consumerSecret = getResources().getString(R.string.consumer_secret);
        String token = getResources().getString(R.string.token);
        String tokenSecret = getResources().getString(R.string.token_secret);

        YelpAPIFactory apiFactory = new YelpAPIFactory(consumerKey, consumerSecret, token, tokenSecret);
        yelpAPI = apiFactory.createAPI();

        GPSTracker gps = new GPSTracker(this);
        latitude = gps.getLatitude();
        longitude = gps.getLongitude();
        TextView locationView = (TextView)findViewById(R.id.location);
        locationView.setText("Location used: " + Double.toString(latitude) + ", " + Double.toString(longitude));
        // Set up async task to make api call
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();

        restaurantsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, SingleView.class);
                intent.putExtra("Restaurant", (String) restaurantsView.getItemAtPosition(position));

                startActivityForResult(intent, 0);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        protected String doInBackground(String... params) {
            Map<String, String> searchParams = new HashMap<>();

            searchParams.put("term", "food");
            CoordinateOptions coordinate = CoordinateOptions.builder()
                    .latitude(latitude)
                    .longitude(longitude).build();
            Call<SearchResponse> call = yelpAPI.search(coordinate, searchParams);
            try {
                Response<SearchResponse> response = call.execute();
                for (Business business : response.body().businesses()) {
                    restaurantsList.add(business.name());
                }
                return "Done";
            } catch (Exception e) {
                Log.e("API ERR", e.toString());
                return "Error";
            }
        }

        protected void onPostExecute(String result) {
            restaurantsAdapter.notifyDataSetChanged();
//            ((TextView)findViewById(R.id.centerView)).setText(restaurantsList.toString());
        }
    }


}
