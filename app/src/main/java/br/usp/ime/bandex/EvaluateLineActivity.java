package br.usp.ime.bandex;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import br.usp.ime.bandex.Util.Bandejao;

import br.usp.ime.bandex.tasks.PostJsonTask;


public class EvaluateLineActivity extends ActionBarActivity {

    public int NOTHING = 3;
    public int chosenRestaurant = NOTHING;
    public int evaluation = 0; // Valores possíveis: 1 a 5
    public static TextView tvRatingStatus;
    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluate_line);
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);
        tracker = analytics.newTracker("UA-68378292-2"); // Replace with actual tracker/property Id
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);
        tracker.setScreenName("EvaluateLineActivity");

        setCustomActionBar();
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        tvRatingStatus = (TextView) findViewById(R.id.textViewTitleStatus);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener(){

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (rating > 0) {
                    tvRatingStatus.setText(Util.Fila.CLASSIFICACAO[(int) (rating) - 1]);
                    tvRatingStatus.setTextColor(getResources().getColor(Util.Fila.COR[(int) (rating) - 1]));
                } else {
                    tvRatingStatus.setText(getResources().getString(R.string.line_status_prompt));
                    tvRatingStatus.setTextColor(Color.BLACK);
                }
            }});

        Button btn_send = (Button) findViewById(R.id.activity_evaluate_line_btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
                evaluation = (int) ratingBar.getRating();
                if (evaluation == 0) {
                    (Toast.makeText(getApplicationContext(), "Escolha uma nota de 1 a 5!", Toast.LENGTH_SHORT)).show();
                } else {
                    if (chosenRestaurant == NOTHING) {
                        (Toast.makeText(getApplicationContext(), "Escolha um restaurante!", Toast.LENGTH_SHORT)).show();
                    } else {
                        avaliar(evaluation-1, chosenRestaurant);
                    }
                }
            }
        });
    }

    public void avaliar(int evaluation, int chosenRestaurant) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("restaurant_id", chosenRestaurant);
            jsonObject.put("status", evaluation);
            jsonObject.put("submit_date", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Calendar.getInstance().getTime()));
            (new PostJsonTask(this)).execute(jsonObject.toString(), getString(R.string.line_post_service_url));
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Fila")
                    .setAction("Enviar avaliação")
                    .setLabel("Enviar avaliação da fila - " + Bandejao.RESTAURANTES[chosenRestaurant])
                    .build());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.activity_evaluate_line_rb_central:
                if (checked)
                    chosenRestaurant = Bandejao.CENTRAL;
                break;
            case R.id.activity_evaluate_line_rb_quimica:
                if (checked)
                    chosenRestaurant = Bandejao.QUIMICA;
                break;
            case R.id.activity_evaluate_line_rb_fisica:
                if (checked)
                    chosenRestaurant = Bandejao.FISICA;
                    break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_evaluate_line, menu);
        return true;
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
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView tvActionBar = (TextView) mCustomView.findViewById(R.id.title_text_action_bar);
        Typeface face= Typeface.createFromAsset(this.getAssets(), "fonts/Raleway-Bold.ttf");
        tvActionBar.setText(this.getTitle());
        tvActionBar.setTypeface(face);

        ImageButton imageButton = (ImageButton) mCustomView
                .findViewById(R.id.imageButton);
        imageButton.setVisibility(View.INVISIBLE);
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        this.getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_background2));
    }

}
