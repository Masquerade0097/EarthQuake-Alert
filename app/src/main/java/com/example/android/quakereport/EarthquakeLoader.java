package com.example.android.quakereport;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;

/**
 * Created by priyanshu on 18/7/17.
 */

public class EarthquakeLoader extends AsyncTaskLoader<ArrayList<Earthquake>> {

        String mStringUrl;

        public EarthquakeLoader(Context context, String stringUrl) {

            super(context);
            mStringUrl = stringUrl;
        }

        @Override
        public ArrayList<Earthquake> loadInBackground() {

            return QueryUtils.fetchEarthquakeData(mStringUrl);
        }

}
