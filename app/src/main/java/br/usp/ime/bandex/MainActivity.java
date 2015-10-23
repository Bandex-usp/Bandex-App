package br.usp.ime.bandex;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.parse.ParseInstallation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import br.usp.ime.bandex.Util.Bandejao;
import br.usp.ime.bandex.Util.Fila;
import br.usp.ime.bandex.Util.Periodo;
import br.usp.ime.bandex.model.Cardapio;
import br.usp.ime.bandex.tasks.GetLineJsonTask;
import br.usp.ime.bandex.tasks.GetMenuJsonTask;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    public static final String EXTRA_RESTAURANTE = "EXTRA_RESTAURANTE";
    static TextView[][] tvInfo = new TextView[3][3]; // tvInfo[0][1] é a sobremesa do central
    public Handler jsonHandler;
    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setJsonHandler(); // aguarda pelo json e mostra na tela
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
        boolean goodNews = sharedPreferences.getBoolean("alreadyAnsweredPushNotifications", false);
        if (!goodNews) {
            Intent intent = new Intent(getApplicationContext(), NewFunctionalityActivity.class);
            startActivity(intent);
        } else {
            setMenuStrings();
            setLineStrings();
        }
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);
        tracker = analytics.newTracker("UA-68378292-2"); // Replace with actual tracker/property Id
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);
        tracker.setScreenName("MainActivity");
        Log.d("[MainActivity]onCreate", "onCreate called!");
        //Parse.initialize(this, "5umFg7qGHN5EC2Xf2zfsF0ItLohWt9DZYFuyvwtO", "HAyxaPMWjBhqShnrmQAQJR17Fev41cp6I8NcJo4a");
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("[MainActivity]onResume", "onResume called!");
        setContentView(R.layout.activity_main);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");
        TextView button = (TextView)findViewById(R.id.arrow1);
        button.setTypeface(font);
        TextView button2 = (TextView)findViewById(R.id.arrow2);
        button2.setTypeface(font);
        TextView button3 = (TextView)findViewById(R.id.arrow3);
        button3.setTypeface(font);
        setTextViews();
        Button btn_evaluate_line = (Button) findViewById(R.id.activity_main_btn_evaluate_line);
        btn_evaluate_line.setVisibility(View.VISIBLE);
        btn_evaluate_line.setOnClickListener(this);
        setCustomActionBar();
        Log.d("[MainActivity]onResume", "onResume to the end!");
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

    public void setMenuStrings() {
        if (!getMenuFromCache()) {
            Log.d("setMenu", "Getting menu from internet");
            getMenuFromInternet();
        }
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

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("[MainActivity]onRestart", "onRestart called!");
        setMenuStrings();
        setLineStrings();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("[MainActivity]onPause", "onPause called!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("[MainActivity]onDestroy", "onDestroy called!");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("[MainActivity]onStop", "onStop called!");
    }

    @Override
    public void onClick(View v) {
        final int central_details = R.id.activity_main_ll_central;
        final int quimica_details = R.id.activity_main_ll_quimica;
        final int fisica_details = R.id.activity_main_ll_fisica;
        final int evaluate_line = R.id.activity_main_btn_evaluate_line;
        Class clazz = MoreDetailsActivity.class;
        int extra = Bandejao.CENTRAL;
        Intent intent;
        Boolean changeActivity = true;

        switch (v.getId()) {
            case central_details:
                extra = Bandejao.CENTRAL;
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("All")
                        .setAction("Ir para mais detalhes")
                        .setLabel("Ir para mais detalhes - Central")
                        .build());
                break;
            case quimica_details:
                extra = Bandejao.QUIMICA;
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("All")
                        .setAction("Ir para mais detalhes")
                        .setLabel("Ir para mais detalhes - Química")
                        .build());
                break;
            case fisica_details:
                extra = Bandejao.FISICA;
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("All")
                        .setAction("Ir para mais detalhes")
                        .setLabel("Ir para detalhes - Física")
                        .build());
                break;
            case evaluate_line:
                clazz = EvaluateLineActivity.class;
                if (Util.getPeriodToShowLine() == Periodo.NOTHING ||
                        (Util.restaurantes != null && Util.isClosed(0, Util.getDay_of_week(), Util.getPeriodToShowMenu()) &&
                                Util.isClosed(1, Util.getDay_of_week(), Util.getPeriodToShowMenu()) &&
                                Util.isClosed(2, Util.getDay_of_week(), Util.getPeriodToShowMenu()))) {
                    changeActivity = false;
                    Toast.makeText(this, "Ops! Avaliação disponível apenas nos horários de funcionamento do bandejão.", Toast.LENGTH_LONG).show();
                } else {
                    // All subsequent hits will be send with screen name = "main screen"
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Fila")
                            .setAction("Ir para avaliar Fila")
                            .setLabel("Ir para avaliar Fila - Todos")
                            .build());
                }
                break;
            default:
                changeActivity = false;
                break;
        }
        if (changeActivity) {
            intent = new Intent(getApplicationContext(), clazz);
            intent.putExtra(EXTRA_RESTAURANTE, extra);
            startActivity(intent);
        }
    }


    public void setTextViews() {
        int MISTURA = 0, SOBREMESA = 1, FILA = 2;
        tvInfo[Bandejao.CENTRAL][MISTURA] = (TextView) findViewById(R.id.activity_main_central_tv_mistura);
        tvInfo[Bandejao.CENTRAL][SOBREMESA] = (TextView) findViewById(R.id.activity_main_central_tv_sobremesa);
        tvInfo[Bandejao.CENTRAL][FILA] = (TextView) findViewById(R.id.activity_main_central_tv_line);
        tvInfo[Bandejao.QUIMICA][MISTURA] = (TextView) findViewById(R.id.activity_main_quimica_tv_mistura);
        tvInfo[Bandejao.QUIMICA][SOBREMESA] = (TextView) findViewById(R.id.activity_main_quimica_tv_sobremesa);
        tvInfo[Bandejao.QUIMICA][FILA] = (TextView) findViewById(R.id.activity_main_quimica_tv_line);
        tvInfo[Bandejao.FISICA][MISTURA] = (TextView) findViewById(R.id.activity_main_fisica_tv_mistura);
        tvInfo[Bandejao.FISICA][SOBREMESA] = (TextView) findViewById(R.id.activity_main_fisica_tv_sobremesa);
        tvInfo[Bandejao.FISICA][FILA] = (TextView) findViewById(R.id.activity_main_fisica_tv_line);

    }

    public void showLineContentOnScreen() {
        int FILA = 2;
        /*if (Util.getPeriodToShowLine() != Periodo.NOTHING) {
            return;
        }*/
        Button btn_evaluate_line = (Button) findViewById(R.id.activity_main_btn_evaluate_line);
        btn_evaluate_line.setVisibility(View.VISIBLE);
        btn_evaluate_line.setOnClickListener(this);
        for (int i = 0; i < 3; i++) {
            Cardapio meal = Util.restaurantes[i].getDays().get(Util.getDay_of_week()).getDay()[Util.getPeriodToShowMenu()];
            if (meal != null) {
                if (Util.restaurantes[i].getLast_submit() == null) {
                    tvInfo[i][FILA].setText("Sem informações sobre a fila.");
                    tvInfo[i][FILA].setTextColor(Color.BLACK);
                } else {
                    tvInfo[i][FILA].setText(Fila.CLASSIFICACAO[Util.restaurantes[i].getLineStatus()]);
                    tvInfo[i][FILA].setTextColor(getResources().getColor(Fila.COR[Util.restaurantes[i].getLineStatus()]));
                }
            } else {
                tvInfo[i][FILA].setText("");
            }
        }
    }

    public void showMenuContentOnScreen() {
        Cardapio meal;
        int MISTURA = 0, SOBREMESA = 1;

        for (int i = 0; i < 3; i++) { // Para cada restaurante, mostra a carne e a sobremesa
            meal = Util.restaurantes[i].getDays().get(Util.getDay_of_week()).getDay()[Util.getPeriodToShowMenu()];
            if (meal != null) {
                tvInfo[i][MISTURA].setText(meal.getMeat());
                tvInfo[i][SOBREMESA].setText(meal.getDesert());
            } else {
                tvInfo[i][MISTURA].setText("Restaurante Fechado.");
                tvInfo[i][SOBREMESA].setText("");
            }
        }
        setOnClickListeners();
    }

    private void setJsonHandler() {
        final Activity caller = this;
        jsonHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                TextView tvGeneralInfo = (TextView) caller.findViewById(R.id.activity_main_tv_general_info);
                switch (msg.what) { // quem manda é o método que tenta pegar da cache ou da internet (este último só manda quando é pego com sucesso)
                    case Util.MENU_JSON_TASK_ID:
                        if (Util.jsonMenuToModel(caller)) {
                            // Verifica se está desatualizado
                            String string_entry_date = Util.restaurantes[0].getDays().get(Util.getDay_of_week()).getEntry_DateS();
                            Date entry_date = null;
                            try {
                                entry_date = new SimpleDateFormat("dd/MM/yyyy").parse(string_entry_date);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
                                tvGeneralInfo.setText("O cardápio ainda não foi atualizado! Mostrando do dia " + Util.restaurantes[0].getDays().get(Util.getDay_of_week()).getEntry_DateS());
                            } else {
                                tvGeneralInfo.setText(Periodo.LUNCH_DINNER_STR[Util.getPeriodToShowMenu()] +
                                        " - " + Util.restaurantes[0].getDays().get(Util.getDay_of_week()).getEntry_DateS()
                                        + " (" + (getResources().getStringArray(R.array.days_array))[Util.getDay_of_week()] + ")");
                            }
                            showMenuContentOnScreen();
                        } else {
                            Toast.makeText(getApplicationContext(), "Ops! Não foi possível pegar as informações de Cardápio.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case Util.LINE_JSON_TASK_ID:
                        if (Util.jsonLineToModel(caller)) {
                            showLineContentOnScreen();// mostrar na tela
                            /*tvGeneralInfo.setText(Periodo.LUNCH_DINNER_STR[Util.getPeriodToShowMenu()] +
                                    " - " + Util.restaurantes[0].getDays().get(Util.getDay_of_week()).getEntry_DateS()
                                    + " (" + (getResources().getStringArray(R.array.days_array))[Util.getDay_of_week()] + ")");*/
                        } else {
                            // esconder info da fila
                            Toast.makeText(getApplicationContext(), "Ops! Não foi possível pegar as informações de Fila.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    public void setOnClickListeners() {
        LinearLayout btn_central_more_details = (LinearLayout) findViewById(R.id.activity_main_ll_central);
        LinearLayout btn_quimica_more_details = (LinearLayout) findViewById(R.id.activity_main_ll_quimica);
        LinearLayout btn_fisica_more_details =  (LinearLayout) findViewById(R.id.activity_main_ll_fisica);

        btn_central_more_details.setOnClickListener(this);
        btn_quimica_more_details.setOnClickListener(this);
        btn_fisica_more_details.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
                if (jsonHandler != null) {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("All")
                            .setAction("Atualizar Tudo")
                            .setLabel("Atualizar Tudo " + me.getTitle().toString())
                            .build());
                    getMenuFromInternet();
                    getLineFromInternet();
                }
            }
        });
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        this.getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_background2));
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

}