package br.usp.ime.bandex.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;

import java.text.SimpleDateFormat;
import java.util.Date;

import br.usp.ime.bandex.R;
import br.usp.ime.bandex.Util;
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
        Log.d("JsonPOST:", params[0]);
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
            final long ONE_MINUTE_IN_MILLIS = 60000;
            Date nextEvaluationTimeDate = new Date();
            nextEvaluationTimeDate.setTime(nextEvaluationTimeDate.getTime() + (15 * ONE_MINUTE_IN_MILLIS));
            SharedPreferences sharedPreferences = caller.getSharedPreferences("myPrefs", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("nextEvaluationTime", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(nextEvaluationTimeDate));
            editor.apply();
            Toast.makeText(caller.getApplicationContext(), getFinishMessage(), Toast.LENGTH_SHORT).show();
            Util.getLineFromInternet(caller);
        } else {
            Toast.makeText(caller.getApplicationContext(), getErrorMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}
