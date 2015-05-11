package br.usp.ime.bandex;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.beardedhen.androidbootstrap.BootstrapButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.usp.ime.bandex.http.JSONGetter;
import br.usp.ime.bandex.model.Bandex;
import br.usp.ime.bandex.model.Cardapio;
import br.usp.ime.bandex.model.Day;
import br.usp.ime.bandex.tasks.GetMenuTask;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    public static final String EXTRA_RESTAURANTE = "EXTRA_RESTAURANTE";
    public static final String EXTRA_JSON = "EXTRA_JSON";

    TextView[][] tvInfo; // tvInfo[0][1] é a sobremesa do central
    public static Bandex[] restaurantes;
    JSONArray jsonArrayRestaurantes;
    static String jsonMenuRepresentation;
    private SharedPreferences sharedPreferences;

    @Override
    public void onClick(View v) {
        final int central_details = R.id.activity_main_central_btn_more_details;
        final int quimica_details = R.id.activity_main_quimica_btn_more_details;
        final int fisica_details = R.id.activity_main_fisica_btn_more_details;
        final int evaluate_line = R.id.activity_main_btn_evaluate_line;
        Class clazz = MoreDetailsActivity.class;
        String extra = "";
        Intent intent;
        Boolean changeActivity = true;

        switch (v.getId()) {
            case central_details:
                extra = "Central";
                break;
            case quimica_details:
                extra = "Química";
                //intent.putExtra(EXTRA_RESTAURANTE, "Química");
                break;
            case fisica_details:
                extra = "Física";
                //intent.putExtra(EXTRA_RESTAURANTE, "Química");
                break;
            case evaluate_line:
                clazz = EvaluateLineActivity.class;
                break;
            default:
                changeActivity = false;
                break;
        }
        if (changeActivity) {
            intent = new Intent(getApplicationContext(), clazz);
            intent.putExtra(EXTRA_RESTAURANTE, extra);
            intent.putExtra(EXTRA_JSON, jsonMenuRepresentation);
            startActivity(intent);
        }
    }

    public void setJsonArrayRestaurantes(JSONArray jsonArrayRestaurantes) {
        this.jsonArrayRestaurantes = jsonArrayRestaurantes;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvInfo = new TextView[3][2];

        tvInfo[0][0] = (TextView) findViewById(R.id.activity_main_central_tv_mistura);
        tvInfo[0][1] = (TextView) findViewById(R.id.activity_main_central_tv_sobremesa);
        tvInfo[1][0] = (TextView) findViewById(R.id.activity_main_quimica_tv_mistura);
        tvInfo[1][1] = (TextView) findViewById(R.id.activity_main_quimica_tv_sobremesa);
        tvInfo[2][0] = (TextView) findViewById(R.id.activity_main_fisica_tv_mistura);
        tvInfo[2][1] = (TextView) findViewById(R.id.activity_main_fisica_tv_sobremesa);
        setOnClickListeners();
        if (!getMenuFromCache()) {
            System.out.println("getting menu from internet");
            getMenuFromInternet();
        }
        else if (jsonMenuToModel())
                showJsonContentOnScreen();
    }

    public void setJson(String preferences_key, String value) {
        //Log.e("asdad", "adasda" + ((Boolean)(value == null)).toString());
        System.out.println("not ok. preferences_key: " + preferences_key);
        if (getString(R.string.preferences_menu_cache).equals(preferences_key)) {
            jsonMenuRepresentation = value;
            Log.d("Debug setJSon", "ok");
        } else {

        }
        // todo else: preferences_evaluation_cache
    }

    // Returns true if could get menu from cache successfully, false otherwise
    public boolean getMenuFromCache() {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        jsonMenuRepresentation = sharedPreferences.getString(getString(R.string.preferences_menu_cache), null);
        return jsonMenuRepresentation != null;
    }

    public void getMenuFromInternet() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Toast.makeText(getApplicationContext(), "Com internet!", Toast.LENGTH_SHORT).show();
            new GetMenuTask(this, getString(R.string.preferences_menu_cache)).execute();
        } else {
            Toast.makeText(getApplicationContext(), "Sem internet!", Toast.LENGTH_SHORT).show();
        }
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
                    Day day = new Day(data, cardapios_lunch_dinner[0], cardapios_lunch_dinner[1]);
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

    public void showJsonContentOnScreen() {
        String mistura, sobremesa;
        Cardapio meal;
        Calendar cal = Calendar.getInstance();
        int hours = cal.get(Calendar.HOUR);
        int minutes = cal.get(Calendar.MINUTE);
        int period = 0; // 0 = lunch, 1 = dinner
        int day_of_week = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Calendar.Monday == 2. In this code, Monday = 0.

        // Choose whether to show the lunch or the dinner
        if (hours >= 14 && minutes >= 30)
            period = 1;
        else period = 0;

        for (int i = 0; i < 3; i++) { // Para cada restaurante, mostra a carne e a sobremesa
            meal = restaurantes[i].getDays().get(4).getDay()[period];
            if (meal != null) {
                tvInfo[i][0].setText(meal.getMeat());
                tvInfo[i][1].setText(meal.getDesert());
            } else {
                tvInfo[i][0].setText("Restaurante Fechado");
                tvInfo[i][1].setText("Restaurante Fechado");
            }
        }
    }

    public void setOnClickListeners() {
        BootstrapButton btn_central_more_details = (BootstrapButton) findViewById(R.id.activity_main_central_btn_more_details);
        BootstrapButton btn_quimica_more_details = (BootstrapButton) findViewById(R.id.activity_main_quimica_btn_more_details);
        BootstrapButton btn_fisica_more_details = (BootstrapButton) findViewById(R.id.activity_main_fisica_btn_more_details);
        BootstrapButton btn_evaluate_line = (BootstrapButton) findViewById(R.id.activity_main_btn_evaluate_line);

        btn_central_more_details.setOnClickListener(this);
        btn_quimica_more_details.setOnClickListener(this);
        btn_fisica_more_details.setOnClickListener(this);
        btn_evaluate_line.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}

