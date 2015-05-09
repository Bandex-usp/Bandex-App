package br.usp.ime.linux.wagner.tcc;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MoreDetailsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_details);

        String newString;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                newString = "";
            } else {
                newString = extras.getString(MainActivity.EXTRA_RESTAURANTE);
            }
        } else {
            newString = (String) savedInstanceState.getSerializable(MainActivity.EXTRA_RESTAURANTE);
        }

        TextView tv_correspondent_restaurant = (TextView) findViewById(R.id.activity_more_details_tv_correspondent_restaurant);
        tv_correspondent_restaurant.setText(
                "Mais detalhes sobre o bandejão " +
                        newString
        ) ;
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
