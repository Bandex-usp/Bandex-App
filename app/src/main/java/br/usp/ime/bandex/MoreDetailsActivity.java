package br.usp.ime.bandex;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import br.usp.ime.bandex.model.Bandex;


public class MoreDetailsActivity extends ActionBarActivity {

    String jsonRepresentation;
    int chosenRestaurant;
    int daySelected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_details);
        chosenRestaurant = 0;

        String restaurantName;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                restaurantName = "";
                jsonRepresentation = "";
            } else {
                restaurantName = extras.getString(MainActivity.EXTRA_RESTAURANTE);
                jsonRepresentation = extras.getString(MainActivity.EXTRA_JSON);
            }
        } else {
            restaurantName = (String) savedInstanceState.getSerializable(MainActivity.EXTRA_RESTAURANTE);
            jsonRepresentation = (String) savedInstanceState.getSerializable(MainActivity.EXTRA_JSON);
        }

        if ("Química".equals(restaurantName)) {
            chosenRestaurant = 1;
        }
        else if ("Física".equals(restaurantName)) {
            chosenRestaurant = 2;
        }
        showMenuByRestaurant(chosenRestaurant, Util.day_of_week, Util.period);
        TextView tv_bandex = (TextView) findViewById(R.id.activity_more_details_tv_title_bandex);
        tv_bandex.setText(restaurantName);
        Spinner spinner1 = (Spinner) findViewById(R.id.days_spinner); // Escolhe dia da semana
        spinner1.setSelection(Util.day_of_week);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                daySelected = position;
                showMenuByRestaurant(chosenRestaurant, daySelected, Util.period);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                showMenuByRestaurant(chosenRestaurant, Util.day_of_week, Util.period);
            }
        });
    }

    public void showMenuByRestaurant(int restaurant_id, int day_of_week, int period) {
        Bandex restaurant = MainActivity.restaurantes[restaurant_id];
        if (restaurant.getDays().get(day_of_week).getDay()[period] == null) {
            Toast.makeText(getApplicationContext(), "Restaurante Fechado!", Toast.LENGTH_SHORT).show();
            return;
        }
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
        tv_optional.setText(restaurant.getDays().get(day_of_week).getDay()[period].getOptional());
        tv_desert.setText(restaurant.getDays().get(day_of_week).getDay()[period].getDesert());
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
