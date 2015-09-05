package br.usp.ime.bandex;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;

import br.usp.ime.bandex.model.Bandex;
import br.usp.ime.bandex.Util.Bandejao;
import br.usp.ime.bandex.Util.Periodo;
import br.usp.ime.bandex.Util.Fila;


public class MoreDetailsActivity extends ActionBarActivity {

    int currentRestaurantOnScreen;
    int currentDayOfWeekOnScreen;
    int currentPeriodOnScreen;

    Spinner spinner1;
    LinearLayout ll_info_cardapio;
    public Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_details);
        setJsonHandler();
        Util.setCustomActionBar(this, handler);
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
            currentRestaurantOnScreen = (int)  savedInstanceState.getSerializable(MainActivity.EXTRA_RESTAURANTE);
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
        Util.getLineFromInternet(this, handler);
    }

    private void setJsonHandler() {
        final Activity caller = this;
        handler = new Handler() {
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

    public void updateMenu() {
        Util.getMenuFromInternet(this, handler);
        Toast.makeText(this, "Cardápio atualizado!", Toast.LENGTH_SHORT).show();
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
                break;
            case R.id.activity_more_details_rb_jantar:
                if (checked)
                    periodSelected = Periodo.DINNER;
                break;
        }
        if (checked) {
            showLineContentOnScreen(currentRestaurantOnScreen, currentDayOfWeekOnScreen, periodSelected);
            showMenuContentOnScreen(currentRestaurantOnScreen, currentDayOfWeekOnScreen, periodSelected);
        }
    }

    public boolean isClosed(int restaurant_id, int day_of_week, int period) {
        return Util.restaurantes[restaurant_id].getDays().get(day_of_week).getDay()[period] == null;
    }

    public void showMenuContentOnScreen(int restaurant_id, int day_of_week, int period) {
        Bandex restaurant = Util.restaurantes[restaurant_id];
        TextView tv_entry_date = (TextView) findViewById(R.id.tv_entry_date);
        tv_entry_date.setText(restaurant.getDays().get(day_of_week).getEntry_DateS());
        if (isClosed(restaurant_id, day_of_week, period)) {
            showClosed();
        } else {
            ll_info_cardapio.setVisibility(View.VISIBLE);
            TextView tv_main = (TextView) findViewById(R.id.activity_more_details_tv_main);
            TextView tv_meat = (TextView) findViewById(R.id.activity_more_details_tv_meat);
            TextView tv_second = (TextView) findViewById(R.id.activity_more_details_tv_second);
            TextView tv_salad = (TextView) findViewById(R.id.activity_more_details_tv_salad);
            TextView tv_optional = (TextView) findViewById(R.id.activity_more_details_tv_optional);
            TextView tv_desert = (TextView) findViewById(R.id.activity_more_details_tv_desert);
            tv_main.setText(restaurant.getDays().get(day_of_week).getDay()[period].getMain());
            tv_meat.setText(restaurant.getDays().get(day_of_week).getDay()[period].getMeat());
            tv_second.setText(restaurant.getDays().get(day_of_week).getDay()[period].getSecond());
            tv_salad.setText(restaurant.getDays().get(day_of_week).getDay()[period].getSalad());
            tv_optional.setText(restaurant.getDays().get(day_of_week).getDay()[period].getOptional().trim());
            tv_desert.setText(restaurant.getDays().get(day_of_week).getDay()[period].getDesert());
        }
    }

    public void showLineContentOnScreen(int restaurant_id, int day_of_week, int period) {
        LinearLayout ll_fila = (LinearLayout) findViewById(R.id.fila_more_details);
        if (Util.jsonLineRepresentation == null || Util.getPeriodToShowLine() == Periodo.NOTHING ||
                period != Util.getPeriodToShowLine() ||
                day_of_week != Util.getDay_of_week() ||
                isClosed(restaurant_id, day_of_week, period) || Util.restaurantes[restaurant_id].getLast_submit() == null) {
            ll_fila.setVisibility(View.INVISIBLE);
        } else {
            ll_fila.setVisibility(View.VISIBLE);
            TextView tv_line_status = (TextView) findViewById(R.id.activity_more_details_tv_line_evaluation_category);
            RatingBar ratingBar_line_status = (RatingBar) findViewById(R.id.ratingBar2);

            tv_line_status.setText(Fila.CLASSIFICACAO[Util.restaurantes[restaurant_id].getLineStatus()]);
            tv_line_status.setTextColor(getResources().getColor(Fila.COR[Util.restaurantes[restaurant_id].getLineStatus()]));
            ratingBar_line_status.setRating(1 + Util.restaurantes[restaurant_id].getLineStatus());
            ratingBar_line_status.setNumStars(1 + Util.restaurantes[restaurant_id].getLineStatus());
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
