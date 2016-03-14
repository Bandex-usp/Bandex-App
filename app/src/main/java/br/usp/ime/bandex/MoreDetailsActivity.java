package br.usp.ime.bandex;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import br.usp.ime.bandex.Util.Bandejao;
import br.usp.ime.bandex.Util.Fila;
import br.usp.ime.bandex.Util.Periodo;
import br.usp.ime.bandex.model.Bandex;
import br.usp.ime.bandex.model.BandexFactory;
import br.usp.ime.bandex.model.Meal;


public class MoreDetailsActivity extends ActionBarActivity {

    Bandejao currentRestaurantOnScreen;
    int currentDayOfWeekOnScreen;
    int currentPeriodOnScreen;

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    Spinner spinner1;
    LinearLayout ll_info_cardapio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Analytics */
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);
        tracker = analytics.newTracker("UA-68378292-2"); // Replace with actual tracker/property Id
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);
        tracker.setScreenName("MoreDetailsActivity");

        setContentView(R.layout.activity_more_details);
        LinearLayout ll_fila = (LinearLayout) findViewById(R.id.fila_more_details);
        ll_fila.setVisibility(View.INVISIBLE);
        setCustomActionBar();
        currentPeriodOnScreen = Util.getPeriodToShowMenu();
        currentDayOfWeekOnScreen = Util.getDayOfWeek();
        ll_info_cardapio = (LinearLayout) findViewById(R.id.info_cardapio);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                currentRestaurantOnScreen = Bandejao.CENTRAL;
            } else {
                currentRestaurantOnScreen = (Bandejao) extras.get((MainActivity.EXTRA_RESTAURANTE));
            }
        } else {
            if (savedInstanceState.getSerializable(MainActivity.EXTRA_RESTAURANTE) == null) {
                currentRestaurantOnScreen = Bandejao.CENTRAL;
            } else {
                currentRestaurantOnScreen = (Bandejao)  savedInstanceState.getSerializable(MainActivity.EXTRA_RESTAURANTE);
            }
        }
        RadioButton rbSelected = Util.getPeriodToShowMenu() == Periodo.LUNCH ?
                (RadioButton) findViewById(R.id.activity_more_details_rb_almoco) :
                (RadioButton) findViewById(R.id.activity_more_details_rb_jantar);
        rbSelected.setChecked(true);
        TextView tv_bandex = (TextView) findViewById(R.id.activity_more_details_tv_title_bandex);
        tv_bandex.setText(BandexFactory.getRestaurant(currentRestaurantOnScreen).getName());

        spinner1 = (Spinner) findViewById(R.id.days_spinner); // Escolhe dia da semana
        spinner1.setSelection(Util.getDayOfWeek());
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Cardápio")
                        .setAction("Visualizar por dia")
                        .setLabel("Visualizar cardápio por dia - " + (getResources().getStringArray(R.array.days_array))[position])
                        .build());
                currentDayOfWeekOnScreen = position;
                showLineContentOnScreen(currentRestaurantOnScreen, currentDayOfWeekOnScreen, currentPeriodOnScreen);
                showMenuContentOnScreen(currentRestaurantOnScreen, currentDayOfWeekOnScreen, currentPeriodOnScreen);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                showLineContentOnScreen(currentRestaurantOnScreen, Util.getDayOfWeek(), Util.getPeriodToShowMenu());
                showMenuContentOnScreen(currentRestaurantOnScreen, Util.getDayOfWeek(), Util.getPeriodToShowMenu());
            }
        });
    }

    public void showClosed() {
        ll_info_cardapio.setVisibility(View.INVISIBLE);
        TextView tv = (TextView) findViewById(R.id.activity_more_details_tv_main);
        tv.setText(R.string.closedRestaurant);
    }

    public void updateLineStatus(View view) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Fila")
                .setAction("Atualizar Fila")
                .setLabel("Atualizar Fila " + BandexFactory.getRestaurant(currentRestaurantOnScreen))
                .build());
        Util.getLineFromInternet(this);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        int periodSelected = Periodo.LUNCH;
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.activity_more_details_rb_almoco:
                if (checked)
                    periodSelected = Periodo.LUNCH;
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Cardápio")
                            .setAction("Visualizar por período")
                            .setLabel("Visualizar cardápio por período - " + Util.Period.possibleValues()[periodSelected])
                            .build());
                break;
            case R.id.activity_more_details_rb_jantar:
                if (checked)
                    periodSelected = Periodo.DINNER;
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Cardápio")
                            .setAction("Visualizar por período")
                            .setLabel("Visualizar cardápio por período - " + Util.Period.values()[periodSelected])
                            .build());
                break;
        }
        if (checked) {
            currentPeriodOnScreen = periodSelected;
            showLineContentOnScreen(currentRestaurantOnScreen, currentDayOfWeekOnScreen, periodSelected);
            showMenuContentOnScreen(currentRestaurantOnScreen, currentDayOfWeekOnScreen, periodSelected);
        }
    }

    public void updateMenuContentOnScreen() {
        showMenuContentOnScreen(currentRestaurantOnScreen, currentDayOfWeekOnScreen, currentPeriodOnScreen);
    }

    public void updateLineContentOnScreen() {
        showLineContentOnScreen(currentRestaurantOnScreen, currentDayOfWeekOnScreen, currentPeriodOnScreen);
    }

    public void showMenuContentOnScreen(Bandejao bandejao, int day_of_week, int period) {
        Bandex restaurant = BandexFactory.getRestaurant(bandejao);
        TextView tv_entry_date = (TextView) findViewById(R.id.tv_entry_date);
        tv_entry_date.setText(restaurant.getDay(day_of_week).getDateName());
        if (Util.isClosed(bandejao, day_of_week, period)) {
            showClosed();
        } else {
            ll_info_cardapio.setVisibility(View.VISIBLE);
            TextView tv_main = (TextView) findViewById(R.id.activity_more_details_tv_main);
            TextView tv_meat = (TextView) findViewById(R.id.activity_more_details_tv_meat);
            TextView tv_second = (TextView) findViewById(R.id.activity_more_details_tv_second);
            TextView tv_salad = (TextView) findViewById(R.id.activity_more_details_tv_salad);
            TextView tv_optional = (TextView) findViewById(R.id.activity_more_details_tv_optional);
            TextView tv_desert = (TextView) findViewById(R.id.activity_more_details_tv_desert);
            TextView tv_calories = (TextView) findViewById(R.id.activity_more_details_tv_calories);

            Meal meal = restaurant.getDay(day_of_week).getMeal(period);
            tv_main.setText(meal.getMain());
            tv_meat.setText(meal.getMeat());
            tv_second.setText(meal.getSecond());
            tv_salad.setText(meal.getSalad());
            tv_optional.setText(meal.getOptional().trim());
            tv_desert.setText(meal.getDesert());
            tv_calories.setText("Valor Calórico: " + meal.getCalories());
        }
    }

    public void showLineContentOnScreen(Bandejao bandejao, int day_of_week, int period) {
        LinearLayout ll_fila = (LinearLayout) findViewById(R.id.fila_more_details);
        if (Util.getPeriodToShowLine() == Periodo.NOTHING ||
                period != Util.getPeriodToShowLine() ||
                day_of_week != Util.getDayOfWeek() ||
                Util.isClosed(bandejao, day_of_week, period)
                || BandexFactory.getRestaurant(bandejao).getLastSubmit() == null
           ) {
            ll_fila.setVisibility(View.INVISIBLE);
        } else {
            ll_fila.setVisibility(View.VISIBLE);
            TextView tv_line_status = (TextView) findViewById(R.id.activity_more_details_tv_line_evaluation_category);
            RatingBar ratingBar_line_status = (RatingBar) findViewById(R.id.ratingBar2);
            int lineStatus = BandexFactory.getRestaurant(bandejao).getLineStatus();

            tv_line_status.setText(Fila.CLASSIFICACAO[lineStatus]);
            tv_line_status.setTextColor(getResources().getColor(Fila.COR[lineStatus]));
            ratingBar_line_status.setNumStars(1 + lineStatus);
            ratingBar_line_status.setRating(1 + lineStatus);
            TextView tvLastSubmit = (TextView) findViewById(R.id.last_evaluation_time);
            tvLastSubmit.setText(BandexFactory.getRestaurant(bandejao).getFormattedLastSubmit());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d("[menu]", "onCreateOptionsMenu called!");
        getMenuInflater().inflate(R.menu.menu_more_details, menu);
        return true;
        //return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        Log.d("[menu]", "onPrepareOptionsMenu called!");
        if (BandexFactory.getRestaurant(Bandejao.CENTRAL) == null || !Util.canEvaluate()) {
            hideOption(menu, R.id.action_update_line);
        } else {
            showOption(menu, R.id.action_update_line);
        }
        return true;
    }

    public void showOption(Menu menu, int id)
    {
        MenuItem item = menu.findItem(id);
        item.setVisible(true);
    }

    private void hideOption(Menu menu, int id)
    {
        MenuItem item = menu.findItem(id);
        item.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_update_line:
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Fila")
                        .setAction("Atualizar Fila")
                        .setLabel("Atualizar Fila - " + getTitle().toString())
                        .build());
                Util.getLineFromInternet(this);
                return true;
            case R.id.action_update_menu:
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Cardápio")
                        .setAction("Atualizar Cardápio")
                        .setLabel("Atualizar Cardápio - " + getTitle().toString())
                        .build());
                Util.getMenuFromInternet(this);
                return true;
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
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        if (upArrow != null && getSupportActionBar() != null) {
            upArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
            this.getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_background2));
        }
    }


}
