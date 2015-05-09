package br.usp.ime.bandex;

import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.Toast;
import com.beardedhen.androidbootstrap.BootstrapButton;


public class EvaluateLineActivity extends ActionBarActivity {

    public int chosenRestaurant = 0; //1 = Central, 2 = Química, 3 = Física, 0 = nenhum
    public int evaluation = 0; // Valores possíveis: 1 a 5

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluate_line);
        BootstrapButton btn_send = (BootstrapButton) findViewById(R.id.activity_evaluate_line_btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
                evaluation = (int) ratingBar.getRating();
                if (evaluation == 0) {
                    (Toast.makeText(getApplicationContext(), "Escolha uma nota de 1 a 5 estrelas!", Toast.LENGTH_SHORT)).show();
                } else {
                    if (chosenRestaurant == 1) {
                        (Toast.makeText(getApplicationContext(), "Central avaliado com sucesso! " + evaluation, Toast.LENGTH_SHORT)).show();
                    }
                    else if (chosenRestaurant == 2) {
                        (Toast.makeText(getApplicationContext(), "Química avaliada com sucesso!", Toast.LENGTH_SHORT)).show();
                    }
                    else if (chosenRestaurant == 3) {
                        (Toast.makeText(getApplicationContext(), "Física avaliada com sucesso!", Toast.LENGTH_SHORT)).show();
                    }
                    else {
                        (Toast.makeText(getApplicationContext(), "Escolha um restaurante!", Toast.LENGTH_SHORT)).show();
                    }
                }
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.activity_evaluate_line_rb_central:
                if (checked)
                    chosenRestaurant = 1;
                break;
            case R.id.activity_evaluate_line_rb_quimica:
                if (checked)
                    chosenRestaurant = 2;
                break;
            case R.id.activity_evaluate_line_rb_fisica:
                if (checked)
                    chosenRestaurant = 3;
                    break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_evaluate_line, menu);
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
