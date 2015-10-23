package br.usp.ime.bandex;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import br.usp.ime.bandex.model.Bandex;
import br.usp.ime.bandex.Util.Bandejao;
import br.usp.ime.bandex.Util.Periodo;
import br.usp.ime.bandex.Util.Fila;
import br.usp.ime.bandex.tasks.GetLineJsonTask;
import br.usp.ime.bandex.tasks.GetMenuJsonTask;


public class MoreDetailsActivity extends ActionBarActivity {

    int currentRestaurantOnScreen;
    int currentDayOfWeekOnScreen;
    int currentPeriodOnScreen;

    public static GoogleAnalytics analytics;
    public static Tracker tracker;
    public Handler jsonHandler;

    Spinner spinner1;
    LinearLayout ll_info_cardapio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        setJsonHandler();
        setCustomActionBar();
        currentPeriodOnScreen = Util.getPeriodToShowMenu();
        currentDayOfWeekOnScreen = Util.getDay_of_week();
        ll_info_cardapio = (LinearLayout) findViewById(R.id.info_cardapio);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                currentRestaurantOnScreen = Bandejao.CENTRAL;
            } else {
                currentRestaurantOnScreen = (int) extras.get((MainActivity.EXTRA_RESTAURANTE));
            }
        } else {
            if (savedInstanceState.getSerializable(MainActivity.EXTRA_RESTAURANTE) == null) {
                currentRestaurantOnScreen = Bandejao.CENTRAL;
            } else {
                currentRestaurantOnScreen = (int)  savedInstanceState.getSerializable(MainActivity.EXTRA_RESTAURANTE);
            }
        }
        RadioButton rbSelected = Util.getPeriodToShowMenu() == Periodo.LUNCH ?
                (RadioButton) findViewById(R.id.activity_more_details_rb_almoco) :
                (RadioButton) findViewById(R.id.activity_more_details_rb_jantar);
        rbSelected.setChecked(true);
        TextView tv_bandex = (TextView) findViewById(R.id.activity_more_details_tv_title_bandex);
        tv_bandex.setText(Util.restaurantNames[currentRestaurantOnScreen]);
        spinner1 = (Spinner) findViewById(R.id.days_spinner); // Escolhe dia da semana
        spinner1.setSelection(Util.getDay_of_week());
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
                showLineContentOnScreen(currentRestaurantOnScreen, Util.getDay_of_week(), Util.getPeriodToShowMenu());
                showMenuContentOnScreen(currentRestaurantOnScreen, Util.getDay_of_week(), Util.getPeriodToShowMenu());
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
                .setLabel("Atualizar Fila " + Bandejao.RESTAURANTES[currentRestaurantOnScreen])
                .build());
        getLineFromInternet();
    }

    public void getLineFromInternet() {
        if (Util.getPeriodToShowLine() == Periodo.NOTHING ||
                (Util.restaurantes != null && Util.isClosed(0, Util.getDay_of_week(), Util.getPeriodToShowMenu()) &&
                        Util.isClosed(1, Util.getDay_of_week(), Util.getPeriodToShowMenu()) &&
                        Util.isClosed(2, Util.getDay_of_week(), Util.getPeriodToShowMenu()))) return;
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetLineJsonTask(this, jsonHandler).execute(this.getString(R.string.line_get_service_url));
        } else {
            Toast.makeText(this.getApplicationContext(), "Sem conexão para pegar o estado das filas!", Toast.LENGTH_SHORT).show();
        }
    }

    public void setLineStrings() {
        if (Util.jsonLineRepresentation == null) {
            getLineFromInternet();
        } else {
            jsonHandler.sendEmptyMessage(Util.LINE_JSON_TASK_ID);
        }
    }

    private void setJsonHandler() {
        final Activity caller = this;
        jsonHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) { // quem manda é o método que tenta pegar da cache ou da internet (este último só manda quando é pego com sucesso)
                    case Util.MENU_JSON_TASK_ID:
                        if (Util.jsonMenuToModel(caller)) {
                            showMenuContentOnScreen(currentRestaurantOnScreen, currentDayOfWeekOnScreen, currentPeriodOnScreen);
                        } else {
                            Toast.makeText(getApplicationContext(), "Ops! Não foi possível pegar as informações de Cardápio.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case Util.LINE_JSON_TASK_ID:
                        if (Util.jsonLineToModel(caller)) {
                            showLineContentOnScreen(currentRestaurantOnScreen, currentDayOfWeekOnScreen, currentPeriodOnScreen);// mostrar na tela
                        } else {
                            // esconder info da fila
                            Toast.makeText(getApplicationContext(), "Ops! Não foi possível pegar as informações de Cardápio.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
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
                        .setLabel("Visualizar cardápio por período - " + Periodo.LUNCH_DINNER_STR[periodSelected])
                        .build());
                break;
            case R.id.activity_more_details_rb_jantar:
                if (checked)
                    periodSelected = Periodo.DINNER;
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Cardápio")
                            .setAction("Visualizar por período")
                            .setLabel("Visualizar cardápio por período - " + Periodo.LUNCH_DINNER_STR[periodSelected])
                            .build());
                break;
        }
        if (checked) {
            showLineContentOnScreen(currentRestaurantOnScreen, currentDayOfWeekOnScreen, periodSelected);
            showMenuContentOnScreen(currentRestaurantOnScreen, currentDayOfWeekOnScreen, periodSelected);
        }
    }

    public void setMenuStrings() {
        if (!getMenuFromCache()) {
            Log.d("setMenu", "Getting menu from internet");
            getMenuFromInternet();
        }
    }

    public void getMenuFromInternet() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetMenuJsonTask(this, jsonHandler).execute(this.getString(R.string.menu_service_url));
        } else {
            Toast.makeText(this.getApplicationContext(), "Sem conexão para atualizar o cardápio!", Toast.LENGTH_SHORT).show();
        }
    }

    public void showMenuContentOnScreen(int restaurant_id, int day_of_week, int period) {
        if (Util.restaurantes == null) {
            setMenuStrings();
            return;
        }
        Bandex restaurant = Util.restaurantes[restaurant_id];
        TextView tv_entry_date = (TextView) findViewById(R.id.tv_entry_date);
        tv_entry_date.setText(restaurant.getDays().get(day_of_week).getEntry_DateS());
        if (Util.isClosed(restaurant_id, day_of_week, period)) {
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
            tv_main.setText(restaurant.getDays().get(day_of_week).getDay()[period].getMain());
            tv_meat.setText(restaurant.getDays().get(day_of_week).getDay()[period].getMeat());
            tv_second.setText(restaurant.getDays().get(day_of_week).getDay()[period].getSecond());
            tv_salad.setText(restaurant.getDays().get(day_of_week).getDay()[period].getSalad());
            tv_optional.setText(restaurant.getDays().get(day_of_week).getDay()[period].getOptional().trim());
            tv_desert.setText(restaurant.getDays().get(day_of_week).getDay()[period].getDesert());
            tv_calories.setText(restaurant.getDays().get(day_of_week).getDay()[period].getCalories());
        }
    }

    public void showLineContentOnScreen(int restaurant_id, int day_of_week, int period) {
        LinearLayout ll_fila = (LinearLayout) findViewById(R.id.fila_more_details);
        if (Util.jsonLineRepresentation == null || Util.getPeriodToShowLine() == Periodo.NOTHING ||
                period != Util.getPeriodToShowLine() ||
                day_of_week != Util.getDay_of_week() ||
                Util.isClosed(restaurant_id, day_of_week, period) || Util.restaurantes[restaurant_id].getLast_submit() == null) {
            ll_fila.setVisibility(View.INVISIBLE);
        } else {
            ll_fila.setVisibility(View.VISIBLE);
            TextView tv_line_status = (TextView) findViewById(R.id.activity_more_details_tv_line_evaluation_category);
            RatingBar ratingBar_line_status = (RatingBar) findViewById(R.id.ratingBar2);

            tv_line_status.setText(Fila.CLASSIFICACAO[Util.restaurantes[restaurant_id].getLineStatus()]);
            tv_line_status.setTextColor(getResources().getColor(Fila.COR[Util.restaurantes[restaurant_id].getLineStatus()]));
            ratingBar_line_status.setNumStars(1 + Util.restaurantes[restaurant_id].getLineStatus());
            ratingBar_line_status.setRating(1 + Util.restaurantes[restaurant_id].getLineStatus());
            TextView tvLastSubmit = (TextView) findViewById(R.id.last_evaluation_time);
            tvLastSubmit.setText((new SimpleDateFormat("dd/MM/yyyy HH:mm")).format(Util.restaurantes[restaurant_id].getLast_submit()));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_more_details, menu);
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
        final ActionBarActivity me = this;
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("All")
                            .setAction("Atualizar Tudo")
                            .setLabel("Atualizar Tudo " + me.getTitle().toString())
                            .build());
                    getMenuFromInternet();
                    getLineFromInternet();
            }
        });
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        this.getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_background2));
    }

    // Returns true if could get menu from cache successfully, false otherwise. Se conseguiu, ainda verifica se está desatualizado. Se estiver, retorna false.
    public boolean getMenuFromCache() {
        SharedPreferences sharedPreferences = this.getPreferences(Activity.MODE_PRIVATE);
        String string_entry_date = sharedPreferences.getString(this.getString(R.string.preferences_entry_date_cache), null);
        Date entry_date;
        if (string_entry_date != null) { // já pegou da internet
            try {
                // Verifica se está desatualizado
                entry_date = new SimpleDateFormat("yyyy-MM-dd").parse(string_entry_date);
                Calendar cal = Calendar.getInstance();
                Date atual = cal.getTime();
                cal.setTime(entry_date);
                int weekday = cal.get(Calendar.DAY_OF_WEEK);
                if (weekday != Calendar.SUNDAY) {
                    int days = (Calendar.SATURDAY - weekday + 1) % 7;
                    cal.add(Calendar.DAY_OF_YEAR, days);
                }
                cal.add(Calendar.DAY_OF_YEAR, 1);
                entry_date = cal.getTime();
                if (entry_date.before(atual)) {
                    return false;
                    // fim Verifica se está desatualizado
                } else {
                    Util.jsonMenuRepresentation = sharedPreferences.getString(this.getString(R.string.preferences_menu_cache), null);
                    this.jsonHandler.sendEmptyMessage(Util.MENU_JSON_TASK_ID);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return Util.jsonMenuRepresentation != null;
    }

}
