package br.usp.ime.bandex.tasks;

import android.app.Activity;
import android.os.Handler;

import br.usp.ime.bandex.R;
import br.usp.ime.bandex.Util;

/**
 * Created by Wagner on 21/08/2015.
 */
public class GetLineJsonTask extends GetJsonTask {

    public GetLineJsonTask(Activity caller, Handler handler) {
        this.caller = caller;
        this.handler = handler;
    }

    @Override
    public String getUpdateMessage() {
        return caller.getString(R.string.title_updating_line);
    }

    @Override
    public String getFinishMessage() {
        return caller.getString(R.string.title_updated_line);
    }

    public int getTaskId() {
        return Util.LINE_JSON_TASK_ID; }

}
