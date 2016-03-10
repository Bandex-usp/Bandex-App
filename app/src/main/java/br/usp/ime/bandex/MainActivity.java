package br.usp.ime.bandex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import br.usp.ime.bandex.Util.Bandejao;
import br.usp.ime.bandex.Util.Periodo;
import br.usp.ime.bandex.model.Bandex;
import br.usp.ime.bandex.model.BandexFactory;
import br.usp.ime.bandex.model.Central;
import br.usp.ime.bandex.model.Fisica;
import br.usp.ime.bandex.model.Meal;
import br.usp.ime.bandex.model.PCO;
import br.usp.ime.bandex.model.Quimica;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    public static final String EXTRA_RESTAURANTE = "EXTRA_RESTAURANTE";
    static TextView[][] tvInfo = new TextView[4][3]; // tvInfo[0][1] é a sobremesa do central
    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Push Notifications */
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
        boolean goodNews = sharedPreferences.getBoolean("alreadyAnsweredPushNotifications", false);
        if (!goodNews) {
            Intent intent = new Intent(getApplicationContext(), NewFunctionalityActivity.class);
            startActivity(intent);
        } else {
            /* Analytics */
            analytics = GoogleAnalytics.getInstance(this);
            analytics.setLocalDispatchPeriod(1800);
            tracker = analytics.newTracker("UA-68378292-2");
            tracker.enableExceptionReporting(true);
            tracker.enableAdvertisingIdCollection(true);
            tracker.enableAutoActivityTracking(true);
            tracker.setScreenName("MainActivity");

            setContentView(R.layout.activity_main);
            setTextViews();
            prepareModel();

            /* Botões de mais detalhes */
            Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");
            TextView button = (TextView)findViewById(R.id.arrow1);
            button.setTypeface(font);
            TextView button2 = (TextView)findViewById(R.id.arrow2);
            button2.setTypeface(font);
            TextView button3 = (TextView)findViewById(R.id.arrow3);
            button3.setTypeface(font);
            TextView button4 = (TextView)findViewById(R.id.arrow4);
            button4.setTypeface(font);

            Button btn_evaluate_line = (Button) findViewById(R.id.activity_main_btn_evaluate_line);
            btn_evaluate_line.setVisibility(View.VISIBLE);
            btn_evaluate_line.setOnClickListener(this);
            setCustomActionBar();
        }
    }

    public void prepareModel() {
        String menu = Util.getMenuFromCache(this);
        if (menu == null) {
            Util.getMenuFromInternet(this);
        } else {
            Util.jsonMenuToModel(this, menu);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BandexFactory.getRestaurant(Bandejao.CENTRAL) == null) {
            prepareModel();
        }
        Log.d("[MainActivity]onResume", "onResume called!");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("[MainActivity]onRestart", "onRestart called!");
        prepareModel();
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
        final int pco_details = R.id.activity_main_ll_pco;
        final int evaluate_line = R.id.activity_main_btn_evaluate_line;
        Class clazz = MoreDetailsActivity.class;
        Bandejao extra = Bandejao.CENTRAL;
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
            case pco_details:
                extra = Bandejao.PCO;
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("All")
                        .setAction("Ir para mais detalhes")
                        .setLabel("Ir para detalhes - PCO")
                        .build());
                break;
            case evaluate_line:
                clazz = EvaluateLineActivity.class;
                if (Util.canEvaluate()) {
                    // All subsequent hits will be send with screen name = "main screen"
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Fila")
                            .setAction("Ir para avaliar Fila")
                            .setLabel("Ir para avaliar Fila - Todos")
                            .build());
                } else {
                    changeActivity = false;
                    Toast.makeText(this, "Ops! Avaliação disponível apenas nos horários de funcionamento do bandejão.", Toast.LENGTH_LONG).show();
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
        tvInfo[Bandejao.CENTRAL.getValue()][MISTURA] = (TextView) findViewById(R.id.activity_main_central_tv_mistura);
        tvInfo[Bandejao.CENTRAL.getValue()][SOBREMESA] = (TextView) findViewById(R.id.activity_main_central_tv_sobremesa);
        tvInfo[Bandejao.CENTRAL.getValue()][FILA] = (TextView) findViewById(R.id.activity_main_central_tv_line);
        tvInfo[Bandejao.QUIMICA.getValue()][MISTURA] = (TextView) findViewById(R.id.activity_main_quimica_tv_mistura);
        tvInfo[Bandejao.QUIMICA.getValue()][SOBREMESA] = (TextView) findViewById(R.id.activity_main_quimica_tv_sobremesa);
        tvInfo[Bandejao.QUIMICA.getValue()][FILA] = (TextView) findViewById(R.id.activity_main_quimica_tv_line);
        tvInfo[Bandejao.FISICA.getValue()][MISTURA] = (TextView) findViewById(R.id.activity_main_fisica_tv_mistura);
        tvInfo[Bandejao.FISICA.getValue()][SOBREMESA] = (TextView) findViewById(R.id.activity_main_fisica_tv_sobremesa);
        tvInfo[Bandejao.FISICA.getValue()][FILA] = (TextView) findViewById(R.id.activity_main_fisica_tv_line);
        tvInfo[Bandejao.PCO.getValue()][MISTURA] = (TextView) findViewById(R.id.activity_main_pco_tv_mistura);
        tvInfo[Bandejao.PCO.getValue()][SOBREMESA] = (TextView) findViewById(R.id.activity_main_pco_tv_sobremesa);
        tvInfo[Bandejao.PCO.getValue()][FILA] = (TextView) findViewById(R.id.activity_main_pco_tv_line);
    }

    public void showLineContentOnScreen() {
        int FILA = 2;
        Button btn_evaluate_line = (Button) findViewById(R.id.activity_main_btn_evaluate_line);
        btn_evaluate_line.setVisibility(View.VISIBLE);
        btn_evaluate_line.setOnClickListener(this);
        for (Bandejao bandejao : Bandejao.possibleValues()) {
            Bandex bandex = BandexFactory.getRestaurant(bandejao);
            Meal meal = bandex.getDay(Util.getDayOfWeek()).getMeal(Util.getPeriodToShowLine());
            if (meal.isAvailable()) {
                if (bandex.getLastSubmit() == null) {
                    tvInfo[bandejao.getValue()][FILA].setText("Sem informações sobre a fila.");
                    tvInfo[bandejao.getValue()][FILA].setTextColor(Color.BLACK);
                } else {
                    tvInfo[bandejao.getValue()][FILA].setText(Util.Fila.CLASSIFICACAO[bandex.getLineStatus()]);
                    tvInfo[bandejao.getValue()][FILA].setTextColor(getResources().getColor(Util.Fila.COR[bandex.getLineStatus()]));
                }
            } else {
                tvInfo[bandejao.getValue()][FILA].setText("");
            }
        }
    }

    public void showInfoBox(Bandex bandex) {
        int MISTURA = 0, SOBREMESA = 1;
        Meal meal = bandex.getDay(Util.getDayOfWeek()).getMeal(Util.getPeriodToShowMenu());
        if (meal.isAvailable()) {
            tvInfo[bandex.getId()][MISTURA].setText(meal.getMeat());
            tvInfo[bandex.getId()][SOBREMESA].setText(meal.getDesert());
        } else {
            tvInfo[bandex.getId()][MISTURA].setText("Restaurante Fechado");
            tvInfo[bandex.getId()][SOBREMESA].setText("");
        }
    }

    public void showMenuContentOnScreen() {
        if (BandexFactory.getRestaurant(Bandejao.CENTRAL) == null) {
            prepareModel();
            return;
        }

        for (Bandejao bandejao : Bandejao.possibleValues()) {
            showInfoBox(BandexFactory.getRestaurant(bandejao));
        }

        TextView tvGeneralInfo = (TextView) findViewById(R.id.activity_main_tv_general_info);
        if (Util.isMenuUpdated()) {
            tvGeneralInfo.setText(Util.Period.possibleValues()[(Util.getPeriodToShowMenu())].getName() +
                    " - " + BandexFactory.getRestaurant(Bandejao.CENTRAL).getDay(Util.getDayOfWeek()).getDateName()
                    + " (" + (getResources().getStringArray(R.array.days_array))[Util.getDayOfWeek()] + ")");
        } else {
            tvGeneralInfo.setText("O cardápio ainda não foi atualizado! Mostrando do dia " + Util.getFormattedMenuDate());
        }
        setOnClickListeners();
    }

    public void setOnClickListeners() {
        LinearLayout btn_central_more_details = (LinearLayout) findViewById(R.id.activity_main_ll_central);
        LinearLayout btn_quimica_more_details = (LinearLayout) findViewById(R.id.activity_main_ll_quimica);
        LinearLayout btn_fisica_more_details =  (LinearLayout) findViewById(R.id.activity_main_ll_fisica);
        LinearLayout btn_pco_more_details =  (LinearLayout) findViewById(R.id.activity_main_ll_pco);

        btn_pco_more_details.setOnClickListener(this);
        btn_central_more_details.setOnClickListener(this);
        btn_quimica_more_details.setOnClickListener(this);
        btn_fisica_more_details.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
        //return super.onCreateOptionsMenu(menu);
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
                Util.getMenuFromInternet(me);
            }
        });
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        this.getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_background2));
    }
}