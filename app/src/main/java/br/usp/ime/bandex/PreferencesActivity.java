package br.usp.ime.bandex;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.concurrent.atomic.AtomicBoolean;

public class PreferencesActivity extends ActionBarActivity {

    AtomicBoolean activate;
    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);
        tracker = analytics.newTracker("UA-68378292-2"); // Replace with actual tracker/property Id
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);
        tracker.setScreenName("PreferencesActivity");
        activate =  new AtomicBoolean();
        setContentView(R.layout.activity_preferences);
        setCustomActionBar();
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
        boolean enabled = sharedPreferences.getBoolean("enableNotifications", false);
        activate.set(enabled);
        if (enabled) {
            RadioButton rbActivate = (RadioButton) findViewById(R.id.activity_preferences_notifications_activate);
            rbActivate.setChecked(true);
        } else {
            RadioButton rbDeactivate = (RadioButton) findViewById(R.id.activity_preferences_notifications_deactivate);
            rbDeactivate.setChecked(true);
        }

        Button btnSave = (Button) findViewById(R.id.activity_preferences_btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("enableNotifications", activate.get());
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Preferências")
                        .setAction("Toggle Notificações")
                        .setLabel("Toggle Notificações")
                        .build());
                editor.commit();
                (Toast.makeText(getApplicationContext(), "Configurações salvas com sucesso!", Toast.LENGTH_LONG)).show();
            }
        });
    }

    public void setCustomActionBar() {
        android.support.v7.app.ActionBar mActionBar = this.getSupportActionBar();
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView tvActionBar = (TextView) mCustomView.findViewById(R.id.title_text_action_bar);
        Typeface face= Typeface.createFromAsset(this.getAssets(), "fonts/Raleway-Bold.ttf");
        tvActionBar.setText(this.getTitle());
        tvActionBar.setTypeface(face);
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        this.getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_background2));
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.activity_preferences_notifications_activate:
                if (checked)
                    activate.set(true);
                break;
            case R.id.activity_preferences_notifications_deactivate:
                if (checked)
                    activate.set(false);
                break;
        }
    }

}
