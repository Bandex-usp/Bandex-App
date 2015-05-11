package br.usp.ime.bandex.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import br.usp.ime.bandex.MainActivity;
import br.usp.ime.bandex.R;
import br.usp.ime.bandex.http.JSONGetter;

/**
 * Created by Wagner on 09/05/2015.
 */
public class GetMenuTask extends AsyncTask<String, String, String> {
    private ProgressDialog pDialog;
    MainActivity caller;
    String preferences_key;

    public GetMenuTask(MainActivity caller, String preferences_key) {
        this.caller = caller;
        this.preferences_key = preferences_key;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(caller);
        pDialog.setMessage("Atualizando...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    @Override
    protected String doInBackground(String... args) {
        // Getting JSON from URL
        JSONGetter jsonGetter = new JSONGetter();
        String json = jsonGetter.getJSONFromUrl(caller.getString(R.string.menu_service_url));
        return json;
    }

    @Override
    protected void onPostExecute(String jsonStr) {
        if (jsonStr == null)
            System.out.println("not ok.json null");
        else System.out.println("ok.json: " + jsonStr);
        pDialog.dismiss();
        SharedPreferences sharedPref = caller.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(preferences_key, jsonStr);
        editor.commit();
        caller.setJson(preferences_key, jsonStr);

        try {
            caller.setJsonArrayRestaurantes(new JSONArray(jsonStr));
        } catch (JSONException e) {
            Log.e("json parsing", "could not parse string to jsonarray");
            e.printStackTrace();
        }
        if (caller.jsonMenuToModel()) {
            System.out.println("OK jsonMenuToModel");
            caller.showJsonContentOnScreen();
        }else System.out.println("not ok jsonMenuToModel");

        /*
        if (jsonMenuToModel(jsonMenu))
            showJsonContentOnScreen();
            */
    }
}
