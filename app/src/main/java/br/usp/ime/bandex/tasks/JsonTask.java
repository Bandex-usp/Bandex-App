package br.usp.ime.bandex.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import br.usp.ime.bandex.MainActivity;
import br.usp.ime.bandex.R;
import br.usp.ime.bandex.Util;
import br.usp.ime.bandex.http.JSONGetter;

/**
 * Created by Wagner on 09/05/2015.
*/                                 //<parametros doInBackground, Parametros OnProgressUpdate, retorno do doInBackground>
public abstract class JsonTask extends AsyncTask<String, String, String> {
    private ProgressDialog pDialog;
    public abstract int getTaskId();
    public abstract String getUpdateMessage();
    public abstract String getFinishMessage();
    Activity caller;
    Handler handler;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(caller);
        pDialog.setMessage(getUpdateMessage());
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    @Override
    protected String doInBackground(String... urls) {
        // Getting JSON from URL
        JSONGetter jsonGetter = new JSONGetter();
        String json = jsonGetter.getJSONFromUrl(urls[0]);
        return json; // parametro do on post execute
    }

    @Override
    protected void onPostExecute(String json) {
        super.onPostExecute(json);
        Util.setJson(getTaskId(), json);
        pDialog.dismiss();
        handler.sendEmptyMessage(getTaskId());
        Toast.makeText(caller.getApplicationContext(), getFinishMessage(), Toast.LENGTH_SHORT).show();
    }
}
