package br.usp.ime.bandex;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.lang.reflect.Array;

public class NewFunctionalityActivity extends ActionBarActivity {

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_functionality);
        setCustomActionBar();
        Button butonNo = (Button) findViewById(R.id.no);
        Button butonYes = (Button) findViewById(R.id.yes);
        final SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
        butonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("alreadyAnsweredPushNotifications", true);
                editor.commit();
                finish();
            }
        });
        butonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("alreadyAnsweredPushNotifications", true);
                editor.commit();
                finish();
            }
        });
    }
    public void setCustomActionBar() {
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);
        tracker = analytics.newTracker("UA-68378292-2"); // Replace with actual tracker/property Id
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);
        tracker.setScreenName("AllScreens");
        android.support.v7.app.ActionBar mActionBar = this.getSupportActionBar();
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView tvActionBar = (TextView) mCustomView.findViewById(R.id.title_text_action_bar);
        Typeface face= Typeface.createFromAsset(this.getAssets(), "fonts/Raleway-Bold.ttf");
        ImageButton imageButton = (ImageButton) mCustomView.findViewById(R.id.imageButton);
        imageButton.setVisibility(View.INVISIBLE);
        tvActionBar.setText(this.getTitle());
        tvActionBar.setTypeface(face);
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        this.getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_background2));
    }

}
