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
import br.usp.ime.bandex.Util;
import br.usp.ime.bandex.http.JSONGetter;

/**
 * Created by Wagner on 09/05/2015.
 */
public class GetMenuTask extends AsyncTask<String, String, String> {
    private ProgressDialog pDialog;
    String preferences_key;
    String url;

    public GetMenuTask(String preferences_key, String url) {
        this.preferences_key = preferences_key;
        this.url = url;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(Util.mainActivityInstance);
        pDialog.setMessage("Atualizando...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected String doInBackground(String... args) {
        // Getting JSON from URL
        JSONGetter jsonGetter = new JSONGetter();
        String json = jsonGetter.getJSONFromUrl(this.url);
        return json;
    }

    @Override
    protected void onPostExecute(String jsonStr) {
        pDialog.dismiss();
        SharedPreferences sharedPref = Util.mainActivityInstance.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(preferences_key, jsonStr);
        editor.commit();
        Util.setJson(preferences_key, jsonStr);
    }
}
