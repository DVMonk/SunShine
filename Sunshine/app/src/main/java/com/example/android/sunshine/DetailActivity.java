package com.example.android.sunshine;

import android.content.Intent;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    private String weatherData;

    private TextView weatherDataTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        weatherDataTextView = (TextView) findViewById(R.id.tv_detail_weather_data);

        Intent intent = getIntent();
        if(intent.hasExtra(Intent.EXTRA_TEXT)){
            weatherData = intent.getStringExtra(Intent.EXTRA_TEXT);
            weatherDataTextView.setText(weatherData);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);

        Intent shareIntent = createShareIntent();
        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareItem.setIntent(shareIntent);

        return true;
    }

    private Intent createShareIntent(){
        String chooserTitle = "Share weather data with";
        String textToShare = weatherData;
        String mimeType = "text/plain";

        Intent shareIntent =ShareCompat.IntentBuilder.from(this)
                .setChooserTitle(chooserTitle)
                .setType(mimeType)
                .setText(textToShare)
                .getIntent();
        return shareIntent;
    }

}
