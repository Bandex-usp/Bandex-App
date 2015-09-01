package br.usp.ime.bandex.tasks;

import android.widget.Toast;

import br.usp.ime.bandex.Util;

/**
 * Created by Wagner on 21/08/2015.
 */
public class LineJsonTask extends JsonTask {

    @Override
    public String getUpdateMessage() {
        return "Atualizando a fila...";
    }

    public int getTaskId() { return 1; }

}
