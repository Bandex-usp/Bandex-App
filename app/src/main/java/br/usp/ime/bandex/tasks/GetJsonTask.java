package br.usp.ime.bandex.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import br.usp.ime.bandex.Util;
import br.usp.ime.bandex.http.StringGetter;

/**
 * Created by Wagner on 09/05/2015.
 */                                 //<parametros doInBackground, Parametros OnProgressUpdate, retorno do doInBackground>
public abstract class GetJsonTask extends AsyncTask<String, String, String> {
    private ProgressDialog pDialog;

    public abstract int getTaskId();

    public abstract String getUpdateMessage();

    public abstract String getFinishMessage();

    Activity caller;

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
        StringGetter stringGetter = new StringGetter();
        String json = stringGetter.getStringFromUrl(urls[0]);
        return json; // parametro do on post execute
    }

    @Override
    protected void onPostExecute(String json) {
        super.onPostExecute(json);
        pDialog.dismiss();
        if (json != null) {
            Util.setJson(getTaskId(), json, caller);
            Toast.makeText(caller, getFinishMessage(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(caller.getApplicationContext(), "Ops! Não foi possível conectar ao servidor.", Toast.LENGTH_SHORT).show();
        }
    }
}
