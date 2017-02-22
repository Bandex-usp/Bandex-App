package br.usp.ime.bandex;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import br.usp.ime.bandex.Util.Bandejao;
import br.usp.ime.bandex.model.BandexFactory;
import br.usp.ime.bandex.tasks.PostJsonTask;


public class EvaluateLineActivity extends ActionBarActivity {

    public Bandejao chosenRestaurant = Bandejao.NONE;
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
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (rating > 0) {
                    tvRatingStatus.setText(Util.Fila.CLASSIFICACAO[(int) (rating) - 1]);
                    tvRatingStatus.setTextColor(getResources().getColor(Util.Fila.COR[(int) (rating) - 1]));
                } else {
                    tvRatingStatus.setText(getResources().getString(R.string.line_status_prompt));
                    tvRatingStatus.setTextColor(Color.BLACK);
                }
            }
        });

        final Activity me = this;
        Button btn_send = (Button) findViewById(R.id.activity_evaluate_line_btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
                evaluation = (int) ratingBar.getRating();
                SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);

                String nextEvaluationTime = sharedPreferences.getString("nextEvaluationTime", null);
                final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs
                int minutesUntilEvaluation = 0;
                if (nextEvaluationTime != null) {
                    try {
                        Date nextEvaluationTimeDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(nextEvaluationTime);
                        Date now = Calendar.getInstance().getTime();
                        if (now.before(nextEvaluationTimeDate)) {
                            minutesUntilEvaluation = (int) ((nextEvaluationTimeDate.getTime() - now.getTime())/ONE_MINUTE_IN_MILLIS + 1);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if (!Util.isConnected(me)) {
                    (Toast.makeText(getApplicationContext(), "Sem conexão com a internet!", Toast.LENGTH_SHORT)).show();
                } else if (evaluation == 0) {
                    (Toast.makeText(getApplicationContext(), "Escolha uma nota de 1 a 5!", Toast.LENGTH_SHORT)).show();
                } else if (chosenRestaurant == Bandejao.NONE) {
                    (Toast.makeText(getApplicationContext(), "Escolha um restaurante!", Toast.LENGTH_SHORT)).show();
                } else if (Util.isClosed(chosenRestaurant)) {
                    (Toast.makeText(getApplicationContext(), "Esse restaurante está fechado agora!", Toast.LENGTH_SHORT)).show();
                } else if (minutesUntilEvaluation > 0){
                    (Toast.makeText(getApplicationContext(), String.format("Próxima avaliação disponível daqui a %d minuto(s)!", minutesUntilEvaluation), Toast.LENGTH_LONG)).show();
                } else {
                    avaliar(evaluation - 1, chosenRestaurant);
                }
            }
        });
    }

    public void avaliar(int evaluation, Bandejao chosenRestaurant) {
        JSONObject jsonObject = new JSONObject();
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT-3"));
            jsonObject.put("restaurant_id", chosenRestaurant.getValue());
            jsonObject.put("status", evaluation);
            jsonObject.put("submit_date", simpleDateFormat.format(Calendar.getInstance().getTime()));
            (new PostJsonTask(this)).execute(jsonObject.toString(), getString(R.string.line_post_service_url));
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Fila")
                    .setAction("Enviar avaliação")
                    .setLabel("Enviar avaliação da fila - " + BandexFactory.getRestaurant(chosenRestaurant).getName())
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
            case R.id.activity_evaluate_line_rb_pco:
                if (checked)
                    chosenRestaurant = Bandejao.PCO;
                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Preferências")
                        .setAction("Ir Para Preferências")
                        .setLabel("Ir Para Preferências - " + getTitle().toString())
                        .build());
                Intent intent = new Intent(getApplicationContext(), PreferencesActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        Util.setOverflowButtonColor(this);
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        if (upArrow != null && getSupportActionBar() != null) {
            upArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
            this.getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_background2));
        }

    }

}
