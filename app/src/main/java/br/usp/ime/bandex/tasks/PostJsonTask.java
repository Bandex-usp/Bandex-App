package br.usp.ime.bandex.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.HttpResponse;

import br.usp.ime.bandex.R;
import br.usp.ime.bandex.http.JSONPoster;

/**
 * Created by Wagner on 04/09/2015.
 */
public class PostJsonTask extends AsyncTask<String, String, String> {
    private ProgressDialog pDialog;
    Activity caller;

    public PostJsonTask(Activity caller) {
        this.caller = caller;
    }

    String getUpdateMessage() {
        return caller.getString(R.string.evaluating);
    }

    String getFinishMessage() {
        return caller.getString(R.string.sucess_evaluated);
    }

    String getErrorMessage() {
        return caller.getString(R.string.evaluate_error);
    }

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
    protected String doInBackground(String... params) { // json e url
        JSONPoster jsonPoster = new JSONPoster();
        HttpResponse response = jsonPoster.postJSONToURL(params[0], params[1]);
        if (response != null) {
            return response.toString();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        pDialog.dismiss();
        if (s != null) {
            Toast.makeText(caller.getApplicationContext(), getFinishMessage(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(caller.getApplicationContext(), getErrorMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}
