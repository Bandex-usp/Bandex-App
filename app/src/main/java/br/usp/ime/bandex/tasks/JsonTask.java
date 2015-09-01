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
*/                                 //<parametros doInBackground, Parametros OnProgressUpdate, retorno do doInBackground>
public abstract class JsonTask extends AsyncTask<String, String, String[]> {
    private ProgressDialog pDialog;
    public abstract int getTaskId();
    public abstract String getUpdateMessage();

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(Util.mainActivityInstance);
        pDialog.setMessage(getUpdateMessage());
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    @Override
    protected String[] doInBackground(String... args) { //preferences_key, url
        String preferences_key = args[0];
        String url = args[1];
        // Getting JSON from URL
        JSONGetter jsonGetter = new JSONGetter();
        String json = jsonGetter.getJSONFromUrl(url);
        return new String[] {preferences_key, json}; // parametro do on post execute
    }

    @Override
    protected void onPostExecute(String []args) { //preferences_key, json
        String preferences_key = args[0];
        String json = args[1];
        super.onPostExecute(args);
        Util.setJson(preferences_key, json);
        pDialog.dismiss();
        Util.mainActivityInstance.jsonHandler.sendEmptyMessage(getTaskId());
    }
}
