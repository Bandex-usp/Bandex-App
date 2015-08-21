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
*/                                         //<parametros doInBackground, Parametros OnProgressUpdate, retorno do doInBackground>
public class GetMenuTask extends AsyncTask<String, String, String[]> {
    private ProgressDialog pDialog;

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
    protected String[] doInBackground(String... args) { //preferences_key, url
        // Getting JSON from URL
        JSONGetter jsonGetter = new JSONGetter();
        String json = jsonGetter.getJSONFromUrl(args[1]);
        return new String[] {args[0], json}; // parametro do on post execute
    }

    @Override
    protected void onPostExecute(String []args) { //preferences_key, json
        super.onPostExecute(args);
        Util.setJson(args[0], args[1]);
        pDialog.dismiss();
        SharedPreferences sharedPref = Util.mainActivityInstance.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(args[0], args[1]);
        editor.commit();
    }
}
