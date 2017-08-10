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

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import static android.view.View.GONE;
import static com.example.android.quakereport.QueryUtils.SAMPLE_JSON_RESPONSE;

public class EarthquakeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Earthquake>> {

    public static final String LOG_TAG = EarthquakeActivity.class.getName();
    TextView emptyTextView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        ListView earthquakeListView = (ListView) findViewById(R.id.list);
        emptyTextView = (TextView) findViewById(R.id.empty_state);
        earthquakeListView.setEmptyView(emptyTextView);

        getLoaderManager().initLoader(0,null,EarthquakeActivity.this).forceLoad();

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(0, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.progress_bar);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            emptyTextView.setText(R.string.no_internet);
        }


    }

    @Override
    public Loader<ArrayList<Earthquake>> onCreateLoader(int id, Bundle args){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude = sharedPref.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));

        Uri originalUri = Uri.parse(SAMPLE_JSON_RESPONSE);
        Uri.Builder uriBuild = originalUri.buildUpon();

        uriBuild.appendQueryParameter("format", "geojson");
        uriBuild.appendQueryParameter("limit", "10");
        uriBuild.appendQueryParameter("minmag", minMagnitude);
        uriBuild.appendQueryParameter("orderby", "time");

        return new EarthquakeLoader(this,uriBuild.toString());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Earthquake>> loader, ArrayList<Earthquake> data) {
        ProgressBar progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
        progress_bar.setVisibility(GONE);
        updateUi(data);
        emptyTextView.setText(R.string.no_internet);
//        Log.v(LOG_TAG,"\tonLoaderfinished done!!\n ");

    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Earthquake>> loader) {
//        Log.v(LOG_TAG,"\tonLoaderReset done!!\n ");
    }


    private void updateUi(ArrayList<Earthquake> earthquakes){

//        Log.v(LOG_TAG,"\tupdateUi done!!\n ");

        // Find a reference to the {@link ListView} in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

        // Create a new adapter that takes the list of earthquakes as input
        final EarthquakeAdapter adapter = new EarthquakeAdapter(this, earthquakes);

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        earthquakeListView.setAdapter(adapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected earthquake.
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current earthquake that was clicked on
                Earthquake currentEarthquake = adapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri earthquakeUri = Uri.parse(currentEarthquake.getUrl());

                // Create a new intent to view the earthquake URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
