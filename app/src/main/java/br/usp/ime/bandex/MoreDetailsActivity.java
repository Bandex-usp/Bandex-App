package br.usp.ime.bandex;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import br.usp.ime.bandex.model.Bandex;
import br.usp.ime.bandex.Util.Bandejao;


public class MoreDetailsActivity extends ActionBarActivity {

    int currentRestaurantOnScreen;
    int currentDayOfWeekOnScreen;
    int currentPeriodOnScreen;

    Spinner spinner1;
    LinearLayout ll_info_cardapio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_details);
        Util.setCustomActionBar(this);
        currentPeriodOnScreen = Util.getPeriod();
        currentDayOfWeekOnScreen = Util.getDay_of_week();
        ll_info_cardapio = (LinearLayout) findViewById(R.id.info_cardapio);

        int restaurantId;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                currentRestaurantOnScreen = Bandejao.getValor(Bandejao.CENTRAL);
            } else {
                currentRestaurantOnScreen = Bandejao.getValor((Bandejao) extras.get((MainActivity.EXTRA_RESTAURANTE)));
            }
        } else {
            currentRestaurantOnScreen = Bandejao.getValor((Bandejao) savedInstanceState.getSerializable(MainActivity.EXTRA_RESTAURANTE));
        }

        RadioButton rbSelected = Util.getPeriod() == 0 ?
                (RadioButton) findViewById(R.id.activity_more_details_rb_almoco) :
                (RadioButton) findViewById(R.id.activity_more_details_rb_jantar);
        rbSelected.setChecked(true);
        showMenuAndLineByRestaurant(currentRestaurantOnScreen, Util.getDay_of_week(), Util.getPeriod());
        TextView tv_bandex = (TextView) findViewById(R.id.activity_more_details_tv_title_bandex);
        tv_bandex.setText(Util.restaurantNames[currentRestaurantOnScreen]);
        spinner1 = (Spinner) findViewById(R.id.days_spinner); // Escolhe dia da semana
        spinner1.setSelection(Util.getDay_of_week());
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentDayOfWeekOnScreen = position;
                showMenuAndLineByRestaurant(currentRestaurantOnScreen, currentDayOfWeekOnScreen, currentPeriodOnScreen);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                showMenuAndLineByRestaurant(currentRestaurantOnScreen, Util.getDay_of_week(), Util.getPeriod());
            }
        });
    }

    public void showClosed() {
        ll_info_cardapio.setVisibility(View.INVISIBLE);
        TextView tv = (TextView) findViewById(R.id.activity_more_details_tv_main);
        tv.setText(R.string.closedRestaurant);
    }

    public void updateLineStatus(View view) {
        Toast.makeText(this, "Status da fila atualizado!", Toast.LENGTH_SHORT).show();
    }

    public void updateMenu(View view) {
        Util.getMenuFromInternet();
        Toast.makeText(this, "Cardápio atualizado!", Toast.LENGTH_SHORT).show();
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        int periodSelected = 0;
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.activity_more_details_rb_almoco:
                if (checked)
                    periodSelected = 0;
                break;
            case R.id.activity_more_details_rb_jantar:
                if (checked)
                    periodSelected = 1;
                break;
        }
        if (checked) {
            showMenuAndLineByRestaurant(currentRestaurantOnScreen, currentDayOfWeekOnScreen, periodSelected);
        }
    }

    public boolean isClosed(Bandex restaurant, int day_of_week, int period) {
        return restaurant.getDays().get(day_of_week).getDay()[period] == null;
    }

    public void showMenu(Bandex restaurant, int day_of_week, int period) {
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

    public void showMenuAndLineByRestaurant(int restaurant_id, int day_of_week, int period) {
        currentDayOfWeekOnScreen = day_of_week;
        currentPeriodOnScreen = period;
        LinearLayout ll_fila = (LinearLayout) findViewById(R.id.fila_more_details);
        if (currentPeriodOnScreen != Util.getLinePeriod() || currentDayOfWeekOnScreen != Util.getDay_of_week()) {
            ll_fila.setVisibility(View.INVISIBLE);
        } else {
            ll_fila.setVisibility(View.VISIBLE);
        }
        Bandex restaurant = Util.restaurantes[restaurant_id];
        TextView tv_entry_date = (TextView) findViewById(R.id.tv_entry_date);
        tv_entry_date.setText(restaurant.getDays().get(day_of_week).getEntry_DateS());
        if (isClosed(restaurant, day_of_week, period)) {
            showClosed();
        } else {
            showMenu(restaurant, day_of_week, period);
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
