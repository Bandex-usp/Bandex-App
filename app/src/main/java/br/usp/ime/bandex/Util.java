package br.usp.ime.bandex;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import br.usp.ime.bandex.model.Bandex;
import br.usp.ime.bandex.model.BandexFactory;
import br.usp.ime.bandex.model.Central;
import br.usp.ime.bandex.model.Fisica;
import br.usp.ime.bandex.model.PCO;
import br.usp.ime.bandex.model.Quimica;
import br.usp.ime.bandex.tasks.GetLineJsonTask;
import br.usp.ime.bandex.tasks.GetMenuJsonTask;

/**
 * Created by Wagner on 26/07/2015.
 */
public class Util {

    public static final int NUMBER_OF_RESTAURANTS = 4;
    public static final int MENU_JSON_TASK_ID = 0;
    public static final int LINE_JSON_TASK_ID = 1;

    public static void setMenuDate(Date menuDate) {
        Util.menuDate = menuDate;

    }

    public static Date menuDate = null;

    public static String getFormattedMenuDate() {
        return new SimpleDateFormat("dd/MM/yyyy").format(menuDate);
    }

    public static class Fila {
        private static String ENORME = "Fila Enorme"; //5
        private static String GRANDE = "Fila Grande"; //4
        private static String MEDIA = "Fila Média"; //3
        private static String PEQUENA = "Fila Pequena";
        private static String MUITO_PEQUENA = "Fila Muito Pequena";

        public static final String CLASSIFICACAO[] = new String[] {MUITO_PEQUENA, PEQUENA, MEDIA, GRANDE, ENORME };
        public static final int COR[] = new int[] {R.color.blue, R.color.green, R.color.yellow, R.color.red2, R.color.red};

    }

    public static class Bandejao  {
        public static final int CENTRAL = 0, QUIMICA = 1, FISICA = 2, PCO = 3;
        public static String[] RESTAURANTES = {"Central", "Química", "Física"};
    }

    public static class Periodo  {
        public static int LUNCH = 0, DINNER = 1, NOTHING = 2;
        public static String []LUNCH_DINNER_STR = {"Almoço", "Jantar"};

        public static Calendar horarioAlmoco[] = new Calendar[2];
        public static Calendar horarioJantar[] = new Calendar[2];
        private static String horariosAlmocoStr[] = {"11:00:00", "14:20:00"};
        private static String horariosJantarStr[] = {"17:00:00", "19:45:00"};
        public static int INICIO = 0, FIM = 1;
        static {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                for (int i = 0; i < 2; i++) {
                    horarioAlmoco[i] = Calendar.getInstance();
                    horarioAlmoco[i].setTime(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").
                            parse(sdf.format(horarioAlmoco[i].getTime()) + " " + horariosAlmocoStr[i]));
                    horarioJantar[i] = Calendar.getInstance();
                    horarioJantar[i].setTime(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").
                            parse(sdf.format(horarioJantar[i].getTime()) + " " + horariosJantarStr[i]));
                }
            } catch (ParseException p) {
                p.printStackTrace();
            }

        }
    }


    public static void setJson(int taskID, String json, Activity caller) {
        switch (taskID) {
            case Util.LINE_JSON_TASK_ID:
                if (!jsonLineToModel(caller, json)) {
                    Toast.makeText(caller, "Ops! Não foi possível pegar as informações sobre a fila.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d("Debug setLineJson", "ok");
                break;
            case Util.MENU_JSON_TASK_ID:
                if (!jsonMenuToModel(caller, json)) {
                    Toast.makeText(caller, "Ops! Não foi possível pegar as informações de Cardápio.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    SharedPreferences sharedPref = caller.getSharedPreferences("cardapio", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(caller.getString(R.string.preferences_menu_cache), json);
                    editor.putString(caller.getString(R.string.preferences_entry_date_cache), getFormattedMenuDate());
                    editor.apply();
                }
                Log.d("Debug setMenuJson", "ok");
                break;
        }

    }

    public static boolean jsonLineToModel(Activity caller, String jsonLineRepresentation) {
        try {
            JSONObject jsonLine = new JSONObject(jsonLineRepresentation);
            for (Integer j = 0; j < NUMBER_OF_RESTAURANTS-1; j++) { // Percorre array de avaliações do json
                JSONObject jsonRestaurant = jsonLine.getJSONObject(j.toString());
                int status = (int) (jsonRestaurant.getDouble("line_status") + 0.5);
                Date submitDate = null;
                try {
                    submitDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS-02:00").parse(jsonRestaurant.getString("last_submit"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (status > 4) status = 4;
                BandexFactory.getRestaurant(j).setLineStatus(status);
                BandexFactory.getRestaurant(j).setLastSubmit(submitDate);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        if (getPeriodToShowLine() != Periodo.NOTHING) {
            if (caller instanceof MainActivity) {
                ((MainActivity) caller).showLineContentOnScreen();
            } else {

            }
        }
        return true;
    }

    // Returns the json Representation of the menu or null on failure
    public static String getMenuFromCache(Activity caller) {
        SharedPreferences sharedPreferences = caller.getSharedPreferences("cardapio", Activity.MODE_PRIVATE);
        String string_entry_date = sharedPreferences.getString(caller.getString(R.string.preferences_entry_date_cache), null);
        if (string_entry_date != null) {
            try {
                setMenuDate(new SimpleDateFormat("dd/MM/yyyy").parse(string_entry_date));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (Util.isMenuUpdated()) {
                return sharedPreferences.getString(caller.getString(R.string.preferences_menu_cache), null);
            }
        }
        return null;
    }

    public static void getMenuFromInternet(Activity caller) {
        ConnectivityManager connMgr = (ConnectivityManager)
                caller.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetMenuJsonTask(caller).execute(caller.getString(R.string.menu_service_url));
        } else {
            Toast.makeText(caller.getApplicationContext(), "Sem conexão para atualizar o cardápio!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void getLineFromInternet(Activity caller) {
        if (!Util.canEvaluate()) {
            return;
        }
        ConnectivityManager connMgr = (ConnectivityManager)
                caller.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetLineJsonTask(caller).execute(caller.getString(R.string.line_get_service_url));
        } else {
            Toast.makeText(caller.getApplicationContext(), "Sem conexão para pegar o estado das filas!", Toast.LENGTH_SHORT).show();
        }
    }

    // Returns true if could pass json to model correctly and false otherwise
    public static boolean jsonMenuToModel(Activity caller, String jsonMenuRepresentation) {
        try {
            JSONArray jsonMenu = new JSONArray(jsonMenuRepresentation);
            Log.d("debugJson", jsonMenuRepresentation);
            Central.initialize(jsonMenu.getJSONObject(Bandejao.CENTRAL));
            Quimica.initialize(jsonMenu.getJSONObject(Bandejao.QUIMICA));
            Fisica.initialize(jsonMenu.getJSONObject(Bandejao.FISICA));
            PCO.initialize(jsonMenu.getJSONObject(Bandejao.PCO));
            setMenuDate(PCO.getInstance().getDay(6).getDate());

            if (caller instanceof MainActivity) {
                ((MainActivity)caller).showMenuContentOnScreen();
            } else {

            }
        } catch (JSONException e) {
            Toast.makeText(caller, "Desculpe! Erro nos dados do servidor.", Toast.LENGTH_SHORT).show();
            Log.e("JsonParser", "Falha ao ler os atributos do json.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    static int getPeriodToShowMenu() {
        Calendar cal = Calendar.getInstance();
        if (cal.before(Periodo.horarioAlmoco[Periodo.FIM]))
            return Periodo.LUNCH;
        else return Periodo.DINNER;
    }

    static int getDayOfWeek() {
        Calendar cal = Calendar.getInstance();
        return (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Calendar.Monday == 2. In this code, Monday = 0.
    }

    public static boolean isClosed(int restaurantId, int dayOfWeek, int period) {
        Bandex bandex = BandexFactory.getRestaurant(restaurantId);
        return !(bandex.getDay(dayOfWeek).getMeal(period).isAvailable());
    }

    private static boolean isClosed(int restaurantId) {
        return isClosed(restaurantId, Util.getDayOfWeek(), Util.getPeriodToShowLine());
    }

    public static boolean canEvaluate() {
        return !(Util.getPeriodToShowLine() == Periodo.NOTHING ||
                (Util.isClosed(Bandejao.CENTRAL) && Util.isClosed(Bandejao.FISICA)
                        && Util.isClosed(Bandejao.QUIMICA) && Util.isClosed(Bandejao.PCO)));
    }

    // Verifica se está atualizado o cardápio
    public static boolean isMenuUpdated() {
        Date agora = Calendar.getInstance().getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(menuDate);
        cal.add(Calendar.DAY_OF_YEAR, 1); // fica um dia a mais... para saber se agora é <= data
        Log.d("agora: ", agora.toString());
        Log.d("data: ", cal.getTime().toString());
        return agora.before(cal.getTime());
    }

    public static boolean inRangeOfLunch() {
        Calendar now = Calendar.getInstance();
        if (Periodo.horarioAlmoco[Periodo.INICIO].before(now) &&
                now.before(Periodo.horarioAlmoco[Periodo.FIM])) {
            return true;
        } else return false;
    }

    public static boolean inRangeOfDinner() {
        Calendar now = Calendar.getInstance();
        if (Periodo.horarioJantar[Periodo.INICIO].before(now) &&
                now.before(Periodo.horarioJantar[Periodo.FIM])) {
            return true;
        } else return false;
    }

    public static int getPeriodToShowLine() {
        return Periodo.LUNCH;
        /*if (inRangeOfLunch()) {
            return Periodo.LUNCH;
        } else if (inRangeOfDinner()) {
            return Periodo.DINNER;
        } else return Periodo.NOTHING;*/
    }
}
