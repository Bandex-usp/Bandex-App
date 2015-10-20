package br.usp.ime.bandex;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

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
import br.usp.ime.bandex.tasks.GetLineJsonTask;
import br.usp.ime.bandex.tasks.GetMenuJsonTask;

/**
 * Created by Wagner on 26/07/2015.
 */
public class Util {


    public static final int MENU_JSON_TASK_ID = 0;
    public static final int LINE_JSON_TASK_ID = 1;
    public static MainActivity mainActivityInstance;
    private static SharedPreferences sharedPreferences;
    public static String jsonMenuRepresentation;
    public static String jsonLineRepresentation = null;
    public static Bandex[] restaurantes;
    public static float[] currentLineStatus;
    public static String restaurantNames[] = {"Central", "Química", "Física"};
    public static Date entry_date = null;
    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    public static class Fila {
        private static String ENORME = "Fila Enorme"; //5
        private static String GRANDE = "Fila Grande"; //4
        private static String MEDIA = "Fila Média"; //3
        private static String PEQUENA = "Fila Pequena";
        private static String MUITO_PEQUENA = "Fila Muito Pequena";

        public static final String CLASSIFICACAO[] = new String[] {MUITO_PEQUENA, PEQUENA, MEDIA, GRANDE, ENORME };
        public static final int COR[] = new int[] {R.color.blue, R.color.green, R.color.yellow, R.color.red2, R.color.red};

    }

    public static class Bandejao  {
        public static int CENTRAL = 0, QUIMICA = 1, FISICA = 2;
        public static String[] RESTAURANTES = {"Central", "Química", "Física"};
    }

    public static class Periodo  {
        public static int LUNCH = 0, DINNER = 1, NOTHING = 2;
        public static String []LUNCH_DINNER_STR = {"Almoço", "Jantar"};

        public static Calendar horarioAlmoco[] = new Calendar[2];
        public static Calendar horarioJantar[] = new Calendar[2];
        private static String horariosAlmocoStr[] = {"11:00:00", "14:20:00"};
        private static String horariosJantarStr[] = {"17:00:00", "19:45:00"};
        public static int INICIO = 0, FIM = 1;
        static {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                for (int i = 0; i < 2; i++) {
                    horarioAlmoco[i] = Calendar.getInstance();
                    horarioAlmoco[i].setTime(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").
                            parse(sdf.format(horarioAlmoco[i].getTime()) + " " + horariosAlmocoStr[i]));
                    horarioJantar[i] = Calendar.getInstance();
                    horarioJantar[i].setTime(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").
                            parse(sdf.format(horarioJantar[i].getTime()) + " " + horariosJantarStr[i]));
                }
            } catch (ParseException p) {
                p.printStackTrace();
            }

        }
    }

    public static void setMainActivityInstance(MainActivity instance) {
        mainActivityInstance = instance;
    }

    public static void setMenuStrings(Activity caller, Handler handler) {
        if (!getMenuFromCache()) {
            Log.d("setMenu", "Getting menu from internet");
            getMenuFromInternet(caller, handler);
        }
    }

    public static void setJson(int taskID, String value) {
        switch (taskID) {
            case Util.LINE_JSON_TASK_ID:
                jsonLineRepresentation = value;
                Log.d("Debug setLineJson", "ok");
                break;
            case Util.MENU_JSON_TASK_ID:
                jsonMenuRepresentation = value;
                Log.d("Debug setMenuJson", "ok");
                break;
        }

        if (jsonMenuRepresentation != null) {
            String preferences_keys[] = {mainActivityInstance.getString(R.string.preferences_menu_cache),
                    mainActivityInstance.getString(R.string.preferences_line_cache)};
            SharedPreferences sharedPref = mainActivityInstance.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(preferences_keys[taskID], value);
            editor.apply();
        }
    }

    static void getLineFromInternet(Activity caller, Handler handler) {
        if (Util.getPeriodToShowLine() == Periodo.NOTHING ||
                (Util.restaurantes != null && Util.isClosed(0, Util.getDay_of_week(), Util.getPeriodToShowMenu()) &&
                        Util.isClosed(1, Util.getDay_of_week(), Util.getPeriodToShowMenu()) &&
                        Util.isClosed(2, Util.getDay_of_week(), Util.getPeriodToShowMenu()))) return;
        ConnectivityManager connMgr = (ConnectivityManager)
                caller.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetLineJsonTask(caller, handler).execute(caller.getString(R.string.line_get_service_url));
        } else {
            Toast.makeText(caller.getApplicationContext(), "Sem conexão para pegar o estado das filas!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void setLineStrings(MainActivity caller, Handler handler) {
        if (jsonLineRepresentation == null) {
            getLineFromInternet(caller, handler);
        } else {
            caller.jsonHandler.sendEmptyMessage(Util.LINE_JSON_TASK_ID);
        }
    }

    public static boolean jsonLineToModel(Activity caller) {
        JSONObject jsonLine = null;
        try {
            if (jsonLineRepresentation == null) {
                Toast.makeText(caller.getApplicationContext(), "Ops! Não foi possível conectar ao servidor.", Toast.LENGTH_SHORT).show();
                return false;
            }
            jsonLine = new JSONObject(jsonLineRepresentation);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        for (Integer j = 0; j < 3; j++) { // Percorre array de avaliações do json
                JSONObject jsonRestaurant = null;
                try {
                    jsonRestaurant = jsonLine.getJSONObject(j.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                int status = 0;
                try {
                    status = (int) (jsonRestaurant.getDouble("line_status") + 0.5);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String submit_dateStr = null;
                try {
                    submit_dateStr = jsonRestaurant.getString("last_submit");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Date submit_date = null;
                try {
                    submit_date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(submit_dateStr);
                } catch (ParseException e) {
                    status = 0;
                    submit_date = null;
                    e.printStackTrace();
                }
            if (status > 4) status = 4;
                restaurantes[j].setLineStatus(status);
                restaurantes[j].setLast_submit(submit_date);
            }
        return true;
    }

    // Returns true if could get menu from cache successfully, false otherwise. Se conseguiu, ainda verifica se está desatualizado. Se estiver, retorna false.
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
                cal.add(Calendar.DAY_OF_YEAR, 1);
                entry_date = cal.getTime();
                if (entry_date.before(atual)) {
                    jsonMenuRepresentation = null;
                    // fim Verifica se está desatualizado
                }
                else {
                    jsonMenuRepresentation = sharedPreferences.getString(mainActivityInstance.getString(R.string.preferences_menu_cache), null);
                    Util.mainActivityInstance.jsonHandler.sendEmptyMessage(MENU_JSON_TASK_ID);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return jsonMenuRepresentation != null;
    }

    // Returns true if could pass json to model correctly and false otherwise
    public static boolean jsonMenuToModel(Activity caller) {
        try {
            if (jsonMenuRepresentation == null) {
                return false;
            }
            JSONArray jsonMenu = new JSONArray(jsonMenuRepresentation);
            List<Day> days;
            JSONObject jsonBandex;
            JSONArray jsonArrayBandexDays;
            String[] strings_lunch_dinner = {"lunch", "dinner"};
            String main, meat, second, salad, optional, desert;
            int calories;
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
                            calories = jsonMeal.getInt("calories");
                            cardapios_lunch_dinner[k] = new Cardapio(main, meat, second, salad, optional, desert, calories);
                        }
                    }
                    Day day = new Day(data, cardapios_lunch_dinner[0], cardapios_lunch_dinner[1], mainActivityInstance);
                    days.add(day);
                } // Array de dias do json
                restaurantes[j] = new Bandex(jsonBandex.getInt("restaurant_id"), days);
            } // array de restaurantes do json
        } catch (JSONException e) {
            Toast.makeText(caller, "Desculpe! Erro nos dados do servidor.", Toast.LENGTH_SHORT).show();
            Log.e("JsonParser", "Falha ao ler os atributos do json.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void getMenuFromInternet(Activity caller, Handler handler) {
        ConnectivityManager connMgr = (ConnectivityManager)
                caller.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetMenuJsonTask(caller, handler).execute(mainActivityInstance.getString(R.string.menu_service_url));
        } else {
            Toast.makeText(caller.getApplicationContext(), "Sem conexão para atualizar o cardápio!", Toast.LENGTH_SHORT).show();
        }
    }

    static int getPeriodToShowMenu() {
        Calendar cal = Calendar.getInstance();
        if (cal.before(Periodo.horarioAlmoco[Periodo.FIM]))
            return Periodo.LUNCH;
        else return Periodo.DINNER;
    }

    static int getDay_of_week() {
        Calendar cal = Calendar.getInstance();
        int day_of_week = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Calendar.Monday == 2. In this code, Monday = 0.
        return day_of_week;
    }

    public static void setEntry_date(Date entry_date) {
        Util.entry_date = entry_date;
    }

    public static boolean isClosed(int restaurant_id, int day_of_week, int period) {
        return Util.restaurantes[restaurant_id].getDays().get(day_of_week).getDay()[period] == null;
    }

    public static boolean inRangeOfLunch() {
        Calendar now = Calendar.getInstance();
        if (Periodo.horarioAlmoco[Periodo.INICIO].before(now) &&
                now.before(Periodo.horarioAlmoco[Periodo.FIM])) {
            return true;
        } else return false;
    }

    public static boolean inRangeOfDinner() {
        Calendar now = Calendar.getInstance();
        if (Periodo.horarioJantar[Periodo.INICIO].before(now) &&
                now.before(Periodo.horarioJantar[Periodo.FIM])) {
            return true;
        } else return false;
    }

    public static int getPeriodToShowLine() {
        if (inRangeOfLunch()) {
            return Periodo.LUNCH;
        } else if (inRangeOfDinner()) {
            return Periodo.DINNER;
        } else return Periodo.NOTHING;
    }

    public static void setCustomActionBar(final ActionBarActivity context, final Handler handler) {
        analytics = GoogleAnalytics.getInstance(context);
        analytics.setLocalDispatchPeriod(1800);
        tracker = analytics.newTracker("UA-68378292-2"); // Replace with actual tracker/property Id
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);
        tracker.setScreenName("AllScreens");
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
                if (handler != null) {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("All")
                            .setAction("Atualizar Tudo")
                            .setLabel("Atualizar Tudo " + context.getTitle().toString())
                            .build());
                    getMenuFromInternet(context, handler);
                    getLineFromInternet(context, handler);
                }
            }
        });
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        context.getSupportActionBar().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.actionbar_background2));
    }
}
