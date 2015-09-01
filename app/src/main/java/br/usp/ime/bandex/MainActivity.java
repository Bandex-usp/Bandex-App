package br.usp.ime.bandex;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;

import br.usp.ime.bandex.Util.Bandejao;
import br.usp.ime.bandex.model.Cardapio;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    public static final String EXTRA_RESTAURANTE = "EXTRA_RESTAURANTE";
    static TextView[][] tvInfo = new TextView[3][2]; // tvInfo[0][1] é a sobremesa do central
    public Handler jsonHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTextViews();
        setOnClickListeners();
        Util.setMainActivityInstance(this);
        setJsonHandler(); // aguarda pelo json e mostra na tela
        Util.setMenuStrings();
        Util.setLineStrings();
        Util.setCustomActionBar(this);
    }


    @Override
    public void onClick(View v) {
        final int central_details = R.id.activity_main_central_btn_more_details;
        final int quimica_details = R.id.activity_main_quimica_btn_more_details;
        final int fisica_details = R.id.activity_main_fisica_btn_more_details;
        final int evaluate_line = R.id.activity_main_btn_evaluate_line;
        Class clazz = MoreDetailsActivity.class;
        Bandejao extra = Bandejao.CENTRAL;
        Intent intent;
        Boolean changeActivity = true;

        switch (v.getId()) {
            case central_details:
                extra = Bandejao.CENTRAL;
                break;
            case quimica_details:
                extra = Bandejao.QUIMICA;
                break;
            case fisica_details:
                extra = Bandejao.FISICA;
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
            startActivity(intent);
        }
    }


    public void setTextViews() {
        tvInfo[0][0] = (TextView) findViewById(R.id.activity_main_central_tv_mistura);
        tvInfo[0][1] = (TextView) findViewById(R.id.activity_main_central_tv_sobremesa);
        tvInfo[1][0] = (TextView) findViewById(R.id.activity_main_quimica_tv_mistura);
        tvInfo[1][1] = (TextView) findViewById(R.id.activity_main_quimica_tv_sobremesa);
        tvInfo[2][0] = (TextView) findViewById(R.id.activity_main_fisica_tv_mistura);
        tvInfo[2][1] = (TextView) findViewById(R.id.activity_main_fisica_tv_sobremesa);
    }

    public void showModelContentOnScreen() {
        Cardapio meal;

        for (int i = 0; i < 3; i++) { // Para cada restaurante, mostra a carne e a sobremesa
            meal = Util.restaurantes[i].getDays().get(Util.getDay_of_week()).getDay()[Util.getPeriod()];
            if (meal != null) {
                tvInfo[i][0].setText(meal.getMeat());
                tvInfo[i][1].setText(meal.getDesert());
            } else {
                tvInfo[i][0].setText("Restaurante Fechado.");
                tvInfo[i][1].setText("Restaurante Fechado.");
            }
        }
    }


    public void hideModelButtons() {

    }

    private void setJsonHandler() {
        jsonHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) { // quem manda é o método que tenta pegar da cache ou da internet (este último só manda quando é pego com sucesso)
                    case 0:
                        if (Util.jsonMenuToModel()) {
                            showModelContentOnScreen();
                        } else {
                            hideModelButtons();
                            Toast.makeText(getApplicationContext(), "Desculpe! Erro nos dados do servidor.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        if (Util.jsonLineToModel()) {
                            // mostrar na tela
                        } else {
                            // esconder info da fila
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