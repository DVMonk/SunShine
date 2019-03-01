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
package com.example.android.sunshine;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements ForecastAdapter.ForecastAdapterOnClickHandler
                                                             , LoaderManager.LoaderCallbacks<String[]> {

    private TextView mErrorMessageTextView;

    private ProgressBar mLoadingIndicator;

    private RecyclerView mRecyclerView;

    private ForecastAdapter mAdapter;

    private Toast mToast;

    private static final String FORECAST_URL_EXTRA = "forecastUrlExtra";

    private static final int FORECAST_LOADER_ID = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        mErrorMessageTextView = (TextView) findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_forecast);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new ForecastAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        loadWeatherData();
    }

    public void showWeatherDataView(){
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    public void showErrorMessage(){
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.VISIBLE);
    }

    public void loadWeatherData(){
        showWeatherDataView();
        String location = SunshinePreferences.getPreferredWeatherLocation(this);
        URL forecastQueryUrl = NetworkUtils.buildUrl(location);
        Bundle forecastBundle = new Bundle();
        forecastBundle.putString(FORECAST_URL_EXTRA, forecastQueryUrl.toString());

        if(getSupportLoaderManager().getLoader(FORECAST_LOADER_ID) == null){
            getSupportLoaderManager().initLoader(FORECAST_LOADER_ID, forecastBundle, this);
        } else {
            getSupportLoaderManager().restartLoader(FORECAST_LOADER_ID, forecastBundle, this);
        }

    }

    public void openLocationInMap(){
        String location = SunshinePreferences.getPreferredWeatherLocation(this);
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("geo")
                .path("0, 0")
                .appendQueryParameter("q", location);
        Uri locationUri = builder.build();

        Intent intent = new Intent(Intent.ACTION_VIEW, locationUri);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }
    }

    @NonNull
    @Override
    public Loader<String[]> onCreateLoader(int i, @Nullable final Bundle bundle) {
        return new AsyncTaskLoader<String[]>(this) {

            String[] weatherDataCashe = null;

            @Override
            protected void onStartLoading() {
                if(weatherDataCashe != null){
                    deliverResult(weatherDataCashe);
                } else{
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }

            }

            @Nullable
            @Override
            public String[] loadInBackground() {
                if(bundle == null){
                    return null;
                }

                String forecastUrlString = bundle.getString(FORECAST_URL_EXTRA);
                URL forecastUrl;
                String searchResultsJSON;
                try {
                    forecastUrl = new URL(forecastUrlString);

                    searchResultsJSON = NetworkUtils.getResponseFromHttpUrl(forecastUrl);
                    return OpenWeatherJsonUtils.getSimpleWeatherStringsFromJson(MainActivity.this, searchResultsJSON);
                }catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String[]> loader, String[] weatherData) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if(weatherData != null){
            showWeatherDataView();
            mAdapter.setWeatherData(weatherData);
        } else {
            showErrorMessage();
        }
    }


    @Override
    public void onLoaderReset(@NonNull Loader<String[]> loader) {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.forecast, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectedItemID = item.getItemId();
        switch (selectedItemID){
            case R.id.action_refresh:
                mAdapter.setWeatherData(null);
                loadWeatherData();
                return true;
            case R.id.action_open_map:
                openLocationInMap();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onForecastListItemClick(String weatherForDay) {
        Context context = MainActivity.this;
        Class activityToLaunch = DetailActivity.class;
        Intent intentDetailActivity = new Intent(context, activityToLaunch);
        intentDetailActivity.putExtra(Intent.EXTRA_TEXT, weatherForDay);
        startActivity(intentDetailActivity);
    }
}