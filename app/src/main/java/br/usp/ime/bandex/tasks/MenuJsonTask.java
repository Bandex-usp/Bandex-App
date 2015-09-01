package br.usp.ime.bandex.tasks;

import android.widget.Toast;

import br.usp.ime.bandex.Util;

/**
 * Created by Wagner on 21/08/2015.
 */
public class MenuJsonTask extends JsonTask {

    @Override
    public String getUpdateMessage() {
        return "Atualizando o cardápio...";
    }

    public int getTaskId() { return 0; }
}
