package br.usp.ime.bandex;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.usp.ime.bandex.model.Bandex;
import br.usp.ime.bandex.model.Cardapio;
import br.usp.ime.bandex.model.Day;
import br.usp.ime.bandex.tasks.GetMenuTask;

/**
 * Created by Wagner on 26/07/2015.
 */
public class Util {

    static Date entry_date = null;
    static final int LUNCH_START_HOUR = 11;
    static final int LUNCH_START_MINUTE = 30;
    static final int LUNCH_END_HOUR = 14;
    static final int LUNCH_END_MINUTE = 30;
    static final int DINNER_START_HOUR = 17;
    static final int DINNER_START_MINUTE = 30;
    static final int DINNER_END_HOUR = 19;
    static final int DINNER_END_MINUTE = 45;
    public static MainActivity mainActivityInstance;
    private static SharedPreferences sharedPreferences;
    static String jsonMenuRepresentation;
    public static Bandex[] restaurantes;

    public static void setMainActivityInstance(MainActivity instance) {
        mainActivityInstance = instance;
    }

    public static void setMenuStrings() {
        if (!getMenuFromCache()) {
            Log.d("setMenu", "Getting menu from internet");
            getMenuFromInternet();
        }
    }

    public static void setJson(String preferences_key, String value) {
        System.out.println("not ok. preferences_key: " + preferences_key);
        if (mainActivityInstance.getString(R.string.preferences_menu_cache).equals(preferences_key)) {
            jsonMenuRepresentation = value;
            Log.d("Debug setJSon", "ok");
        } else {

        }
        // todo else: preferences_evaluation_cache
    }

    // Returns true if could get menu from cache successfully, false otherwise
    public static boolean getMenuFromCache() {
        sharedPreferences = mainActivityInstance.getPreferences(Activity.MODE_PRIVATE);
        String string_entry_date = sharedPreferences.getString(mainActivityInstance.getString(R.string.preferences_entry_date_cache), null);
        Date entry_date;
        if (string_entry_date != null) { // já pegou da internet
            try {
                // Verifica se está desatualizado
                entry_date = new SimpleDateFormat("yyyy-MM-dd").parse(string_entry_date);
                Calendar cal = Calendar.getInstance();
                Date atual = cal.getTime();
                cal.setTime(entry_date);
                int weekday = cal.get(Calendar.DAY_OF_WEEK);
                if (weekday != Calendar.SUNDAY)
                {
                    int days = (Calendar.SATURDAY - weekday + 1) % 7;
                    cal.add(Calendar.DAY_OF_YEAR, days);
                }
                entry_date = cal.getTime();
                if (entry_date.before(atual)) {
                    jsonMenuRepresentation = null;
                    // fim Verifica se está desatualizado
                }
                else {
                    jsonMenuRepresentation = sharedPreferences.getString(mainActivityInstance.getString(R.string.preferences_menu_cache), null);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return jsonMenuRepresentation != null;
    }

    // Returns true if could pass json to model correctly and false otherwise
    public static boolean jsonMenuToModel() {
        try {
            JSONArray jsonMenu = new JSONArray(jsonMenuRepresentation);
            List<Day> days;
            JSONObject jsonBandex;
            JSONArray jsonArrayBandexDays;
            String[] strings_lunch_dinner = {"lunch", "dinner"};
            String main, meat, second, salad, optional, desert;
            restaurantes = new Bandex[3];
            for (int j = 0; j < 3; j++) { // Percorre array de restaurantes do json
                jsonBandex = jsonMenu.getJSONObject(j);
                jsonArrayBandexDays = jsonBandex.getJSONArray("days");
                days = new ArrayList<Day>();
                for (int i = 0; i < jsonArrayBandexDays.length(); i++) { // percorre o array de dias do json
                    JSONObject jsonDay = jsonArrayBandexDays.getJSONObject(i);
                    Cardapio[] cardapios_lunch_dinner = {null, null};
                    String data = jsonDay.getString("entry_date");
                    for (int k = 0; k < 2; k++) { // Percorre lunch e dinner
                        JSONObject jsonMeal = jsonDay.optJSONObject(strings_lunch_dinner[k]);
                        if (jsonMeal != null) {
                            main = jsonMeal.getString("main");
                            meat = jsonMeal.getString("meat");
                            second = jsonMeal.getString("second");
                            salad = jsonMeal.getString("salad");
                            optional = jsonMeal.getString("optional");
                            desert = jsonMeal.getString("desert");
                            cardapios_lunch_dinner[k] = new Cardapio(main, meat, second, salad, optional, desert);
                        }
                    }
                    Day day = new Day(data, cardapios_lunch_dinner[0], cardapios_lunch_dinner[1], mainActivityInstance);
                    days.add(day);
                } // Array de dias do json
                restaurantes[j] = new Bandex(jsonBandex.getInt("restaurant_id"), days);
            } // array de restaurantes do json
        } catch (JSONException e) {
            Log.e("JsonParser", "Falha ao ler os atributos do json.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void getMenuFromInternet() {
        ConnectivityManager connMgr = (ConnectivityManager)
                mainActivityInstance.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //Toast.makeText(getApplicationContext(), "Com internet!", Toast.LENGTH_SHORT).show();
            new GetMenuTask(mainActivityInstance.getString(R.string.preferences_menu_cache),
                            mainActivityInstance.getString(R.string.menu_service_url))
                            .execute();
        } else {
            Toast.makeText(mainActivityInstance.getApplicationContext(), "Sem conexão!", Toast.LENGTH_SHORT).show();
        }
    }

    static int getPeriod() {
        int period = 0; // 0 = lunch, 1 = dinner
        Calendar cal = Calendar.getInstance();
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        // Choose whether to show the lunch or the dinner
        if (hours == LUNCH_END_HOUR) {
            if (minutes >= LUNCH_END_MINUTE) period = 1;
        } else if (hours > LUNCH_END_HOUR) period = 1;
        else period = 0;
        return period;
    }

    static int getDay_of_week() {
        Calendar cal = Calendar.getInstance();
        int day_of_week = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Calendar.Monday == 2. In this code, Monday = 0.
        return day_of_week;
    }

    public static void setEntry_date(Date entry_date) {
        Util.entry_date = entry_date;
    }

    public static boolean inRangeOfLunch(int hours, int minutes) {
        if (hours > LUNCH_START_HOUR && hours < LUNCH_END_HOUR) {
            return true;
        } else if (hours == LUNCH_START_HOUR) {
            if (minutes >= LUNCH_START_MINUTE) return true;
        } else if (hours == LUNCH_END_HOUR) {
            if (minutes <= LUNCH_END_MINUTE) return true;
        }
        return false;
    }

    public static boolean inRangeOfDinner(int hours, int minutes) {
        if (hours > DINNER_START_HOUR && hours < DINNER_END_HOUR) {
            return true;
        } else if (hours == DINNER_START_HOUR) {
            if (minutes >= DINNER_START_MINUTE) return true;
        } else if (hours == DINNER_END_HOUR) {
            if (minutes <= DINNER_END_MINUTE) return true;
        }
        return false;
    }

    public static int getLinePeriod() {
        int period; // 0 = lunch, 1 = dinner, 2 = nothing
        Calendar cal = Calendar.getInstance();
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        if (inRangeOfLunch(hours, minutes)) {
            period = 0;
        } else if (inRangeOfDinner(hours, minutes)) {
            period = 1;
        } else period = 2;
        return period;
    }

    public static void setCustomActionBar(final ActionBarActivity context) {
        android.support.v7.app.ActionBar mActionBar = context.getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(context);

        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView tvActionBar = (TextView) mCustomView.findViewById(R.id.title_text_action_bar);
        Typeface face= Typeface.createFromAsset(context.getAssets(), "fonts/Raleway-Bold.ttf");
        tvActionBar.setText(context.getTitle());
        tvActionBar.setTypeface(face);

        ImageButton imageButton = (ImageButton) mCustomView
                .findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {


                Toast.makeText(context, "Refresh Clicked!",
                        Toast.LENGTH_LONG).show();
            }
        });
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        context.getSupportActionBar().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.actionbar_background2));
    }
}
