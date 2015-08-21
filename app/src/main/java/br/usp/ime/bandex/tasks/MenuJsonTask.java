package br.usp.ime.bandex.tasks;

import android.widget.Toast;

import br.usp.ime.bandex.Util;

/**
 * Created by Wagner on 21/08/2015.
 */
public class MenuJsonTask extends JsonTask {

    public void jsonToModelAndScreen() {
        if (Util.jsonMenuToModel(Util.jsonMenuRepresentation)) {
            Util.mainActivityInstance.showModelContentOnScreen();
        } else {
            Toast.makeText(Util.mainActivityInstance, "Desculpe! Erro nos dados do servidor.", Toast.LENGTH_SHORT).show();
        }
    }
}
