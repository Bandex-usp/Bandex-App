package br.usp.ime.bandex;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.SimpleDateFormat;

import br.usp.ime.bandex.Util.Bandejao;
import br.usp.ime.bandex.Util.Fila;
import br.usp.ime.bandex.Util.Periodo;

import br.usp.ime.bandex.model.Cardapio;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    public static final String EXTRA_RESTAURANTE = "EXTRA_RESTAURANTE";
    static TextView[][] tvInfo = new TextView[3][3]; // tvInfo[0][1] é a sobremesa do central
    public Handler jsonHandler;

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    protected void onResume() {
        super.onResume();
        //Log.d("[MainActivity]onResume", "onResume called!");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //Log.d("[MainActivity]onRestart", "onRestart called!");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.d("[MainActivity]onPause", "onPause called!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Log.d("[MainActivity]onDestroy", "onDestroy called!");
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Log.d("[MainActivity]onStop", "onStop called!");
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Log.d("[MainActivity]onStart", "onStart called!");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);
        tracker = analytics.newTracker("UA-68378292-2"); // Replace with actual tracker/property Id
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);
        tracker.setScreenName("MainActivity");

        Log.d("[MainActivity]onCreate", "onCreate called!");
        setContentView(R.layout.activity_main);
        hideEvaluateButton();
        setTextViews();
        Util.setMainActivityInstance(this);
        if (Util.jsonLineRepresentation == null ||
                Util.getPeriodToShowLine() == Periodo.NOTHING ||
                (Util.isClosed(0, Util.getDay_of_week(), Util.getPeriodToShowMenu()) &&
                   Util.isClosed(1, Util.getDay_of_week(), Util.getPeriodToShowMenu()) &&
                    Util.isClosed(2, Util.getDay_of_week(), Util.getPeriodToShowMenu()))) {
            hideEvaluateButton();
        } else {
            BootstrapButton btn_evaluate_line = (BootstrapButton) findViewById(R.id.activity_main_btn_evaluate_line);
            btn_evaluate_line.setVisibility(View.VISIBLE);
            btn_evaluate_line.setOnClickListener(this);
        }
        setJsonHandler(); // aguarda pelo json e mostra na tela
        Util.setMenuStrings(this, jsonHandler);
        Util.setLineStrings(this, jsonHandler);
        Util.setCustomActionBar(this, jsonHandler);
    }


    public void hideEvaluateButton() {
        BootstrapButton btn_evaluate_line = (BootstrapButton) findViewById(R.id.activity_main_btn_evaluate_line);
        btn_evaluate_line.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        final int central_details = R.id.activity_main_central_btn_more_details;
        final int quimica_details = R.id.activity_main_quimica_btn_more_details;
        final int fisica_details = R.id.activity_main_fisica_btn_more_details;
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
                // All subsequent hits will be send with screen name = "main screen"
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Fila")
                        .setAction("Ir para avaliar Fila")
                        .setLabel("Ir para avaliar Fila - Todos")
                        .build());
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
        BootstrapButton btn_evaluate_line = (BootstrapButton) findViewById(R.id.activity_main_btn_evaluate_line);
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
                            tvGeneralInfo.setText(Periodo.LUNCH_DINNER_STR[Util.getPeriodToShowMenu()] +
                                    " - " + Util.restaurantes[0].getDays().get(Util.getDay_of_week()).getEntry_DateS()
                                    + " (" + (getResources().getStringArray(R.array.days_array))[Util.getDay_of_week()] + ")");
                            showMenuContentOnScreen();
                        } else {
                            Toast.makeText(getApplicationContext(), "Ops! Não foi possível pegar as informações de Cardápio.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case Util.LINE_JSON_TASK_ID:
                        if (Util.jsonLineToModel(caller)) {
                            showLineContentOnScreen();// mostrar na tela
                            tvGeneralInfo.setText("Exibindo informações sobre o " +
                                    Periodo.LUNCH_DINNER_STR[Util.getPeriodToShowMenu()] +
                                    " de hoje.");
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
        BootstrapButton btn_central_more_details = (BootstrapButton) findViewById(R.id.activity_main_central_btn_more_details);
        BootstrapButton btn_quimica_more_details = (BootstrapButton) findViewById(R.id.activity_main_quimica_btn_more_details);
        BootstrapButton btn_fisica_more_details = (BootstrapButton) findViewById(R.id.activity_main_fisica_btn_more_details);

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