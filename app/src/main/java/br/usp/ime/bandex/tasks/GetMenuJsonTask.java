package br.usp.ime.bandex.tasks;

import android.app.Activity;
import android.os.Handler;

import br.usp.ime.bandex.R;
import br.usp.ime.bandex.Util;

/**
 * Created by Wagner on 21/08/2015.
 */
public class GetMenuJsonTask extends GetJsonTask {

    public GetMenuJsonTask(Activity caller) {
        this.caller = caller;
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
