package br.usp.ime.bandex.tasks;

import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;

import br.usp.ime.bandex.R;
import br.usp.ime.bandex.Util;

/**
 * Created by Wagner on 21/08/2015.
 */
public class MenuJsonTask extends JsonTask {

    public MenuJsonTask(Activity caller, Handler handler) {
        this.caller = caller;
        this.handler = handler;
    }

    @Override
    public String getUpdateMessage() {
        return caller.getString(R.string.title_updating_menu);
    }

    @Override
    public String getFinishMessage() {
        return caller.getString(R.string.title_updated_menu);
    }

    public int getTaskId() {
        return Util.MENU_JSON_TASK_ID;
    }
}
