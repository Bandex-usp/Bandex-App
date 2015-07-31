package br.usp.ime.bandex;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.internal.view.SupportActionModeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    public void setTextViews() {
        tvInfo = new TextView[3][2];
        tvInfo[0][0] = (TextView) findViewById(R.id.activity_main_central_tv_mistura);
        tvInfo[0][1] = (TextView) findViewById(R.id.activity_main_central_tv_sobremesa);
        tvInfo[1][0] = (TextView) findViewById(R.id.activity_main_quimica_tv_mistura);
        tvInfo[1][1] = (TextView) findViewById(R.id.activity_main_quimica_tv_sobremesa);
        tvInfo[2][0] = (TextView) findViewById(R.id.activity_main_fisica_tv_mistura);
        tvInfo[2][1] = (TextView) findViewById(R.id.activity_main_fisica_tv_sobremesa);
    }


    public void setMenu() {
        if (!getMenuFromCache()) {
            Log.d("setMenu", "Getting menu from internet");
            getMenuFromInternet();
        }
        else if (jsonMenuToModel())
            showJsonContentOnScreen();
    }

    public void setLineStatus() {
        //todo: Fazer o get do serviço de filas
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Util.setCustomActionBar(this);
        setTextViews();
        setOnClickListeners();
        setMenu();
        setLineStatus();
    }

    public void setJson(String preferences_key, String value) {
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
        String string_entry_date = sharedPreferences.getString(getString(R.string.preferences_entry_date_cache), null);
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
                //if (entry_date.before(atual)) {
                  //  jsonMenuRepresentation = null;
                    // fim Verifica se está desatualizado
                //}
                //else {
                    jsonMenuRepresentation = sharedPreferences.getString(getString(R.string.preferences_menu_cache), null);
                //}
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return jsonMenuRepresentation != null;
    }

    public void getMenuFromInternet() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //Toast.makeText(getApplicationContext(), "Com internet!", Toast.LENGTH_SHORT).show();
            new GetMenuTask(this, getString(R.string.preferences_menu_cache)).execute();
        } else {
            Toast.makeText(getApplicationContext(), "Sem conexão!", Toast.LENGTH_SHORT).show();
        }
    }

    // Returns true if could pass json to model correctly and false otherwise
    public boolean jsonMenuToModel() {
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
                    Day day = new Day(data, cardapios_lunch_dinner[0], cardapios_lunch_dinner[1], this);
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

        for (int i = 0; i < 3; i++) { // Para cada restaurante, mostra a carne e a sobremesa
            meal = restaurantes[i].getDays().get(Util.day_of_week).getDay()[Util.period];
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