package br.usp.ime.bandex;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

    public enum Period {
        LUNCH(0) {
            @Override
            public String getName() {
                return "Almoço";
            }

            @Override
            public String getStart() {
                return "11:00";
            }
        },
        DINNER(1) {
            @Override
            public String getName() {
                return "Jantar";
            }

            @Override
            public String getStart() {
                return "17:00";
            }
        },
        NONE(-1) {
            @Override
            public String getName() {
                return "Not a valid period";
            }

            @Override
            public String getStart() {
                return "00:00";
            }
        };


        /* Eliminates the null object */
        public static Period[] possibleValues() {
            return Arrays.copyOf(Period.values(), Period.values().length - 1);
        }

        int id;
        Period(int i) {
            this.id = i;
        }

        public abstract String getName();
        public abstract String getStart();
    }

    public enum Bandejao  {
        CENTRAL(0) {
            @Override
            public Bandex getInstance() {
                return Central.getInstance();
            }
        },
        QUIMICA(1) {
            @Override
            public Bandex getInstance() {
                return Quimica.getInstance();
            }
        }, FISICA(2) {
            @Override
            public Bandex getInstance() {
                return Fisica.getInstance();
            }
        }, PCO(3) {
            @Override
            public Bandex getInstance() {
                return br.usp.ime.bandex.model.PCO.getInstance();
            }
        }, NONE(-1) {
            @Override
            public Bandex getInstance() {
                return null;
            }
        };

        /* Eliminates the null object */
        public static Bandejao[] possibleValues() {
            return Arrays.copyOf(Bandejao.values(), Bandejao.values().length - 1);
        }

        private int value;
        Bandejao(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public abstract Bandex getInstance();
    }

    public static class Periodo  {
        public static int LUNCH = 0, DINNER = 1, NOTHING = 2;

        public static Calendar horarioAlmoco[] = new Calendar[2];
        public static Calendar horarioJantar[] = new Calendar[2];
        private static String horariosAlmocoStr[] = {"11:00", "14:20"};
        private static String horariosJantarStr[] = {"17:00", "19:45"};
        public static int INICIO = 0, FIM = 1;
        static {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                for (int i = 0; i < 2; i++) {
                    horarioAlmoco[i] = Calendar.getInstance();
                    horarioAlmoco[i].setTime(new SimpleDateFormat("dd-MM-yyyy HH:mm").
                            parse(sdf.format(horarioAlmoco[i].getTime()) + " " + horariosAlmocoStr[i]));
                    horarioJantar[i] = Calendar.getInstance();
                    horarioJantar[i].setTime(new SimpleDateFormat("dd-MM-yyyy HH:mm").
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
                SharedPreferences sharedPreferences = caller.getSharedPreferences("fila", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(caller.getString(R.string.preferences_line_cache), json);
                editor.putString(caller.getString(R.string.preferences_line_cache_time),
                        new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Calendar.getInstance().getTime()));
                editor.apply();
                Log.d("Debug setLineJson", "ok");
                break;
            case Util.MENU_JSON_TASK_ID:
                if (!jsonMenuToModel(caller, json)) {
                    Toast.makeText(caller, "Ops! Não foi possível pegar as informações de Cardápio.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    SharedPreferences sharedPref = caller.getSharedPreferences("cardapio", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = sharedPref.edit();
                    edit.putString(caller.getString(R.string.preferences_menu_cache), json);
                    edit.putString(caller.getString(R.string.preferences_entry_date_cache), getFormattedMenuDate());
                    edit.apply();
                }
                Log.d("Debug setMenuJson", "ok");
                break;
        }

    }

    public static boolean jsonLineToModel(Activity caller, String jsonLineRepresentation) {
        try {
            JSONObject jsonLine = new JSONObject(jsonLineRepresentation);
            for (Bandejao bandejao : Bandejao.possibleValues()) {
                JSONObject jsonRestaurant = jsonLine.getJSONObject("" + bandejao.value);
                int status = (int) (jsonRestaurant.getDouble("line_status") + 0.5);
                Date submitDate = null;
                boolean temAvaliacao = false;
                try {
                    submitDate = new SimpleDateFormat("-4712-01-01'T'00:00:00.000+00:00").parse(jsonRestaurant.getString("last_submit"));
                } catch (ParseException e) {
                    temAvaliacao = true;
                    e.printStackTrace();
                    try {
                        submitDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(jsonRestaurant.getString("last_submit"));
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                if (status > 4) status = 4;
                BandexFactory.getRestaurant(bandejao).setLineStatus(status);
                BandexFactory.getRestaurant(bandejao).setLastSubmit(temAvaliacao ? submitDate : null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        /*if (Util.canEvaluate()) {
            if (caller instanceof MainActivity) {
                ((MainActivity) caller).showOption(R.id.action_update_line);
            }
        }*/

        if (getPeriodToShowLine() != Periodo.NOTHING) {
            if (caller instanceof MainActivity) {
                ((MainActivity) caller).showLineContentOnScreen();
            } else if (caller instanceof MoreDetailsActivity) {
                ((MoreDetailsActivity) caller).updateLineContentOnScreen();
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

    public static String getLineFromCache(Activity caller) {
        SharedPreferences sharedPreferences = caller.getSharedPreferences("fila", Activity.MODE_PRIVATE);
        Date lastLineSave = null;
        try {
            String timeString = sharedPreferences.getString(caller.getString(R.string.preferences_line_cache_time), null);
            if (timeString != null) {
                lastLineSave = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(timeString);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (lastLineSave != null) {
            Calendar saved = Calendar.getInstance();
            saved.setTime(lastLineSave);
            saved.add(Calendar.MINUTE, 2);

            if (saved.before(Calendar.getInstance())) {
                return null;
            }
        }
        return sharedPreferences.getString(caller.getString(R.string.preferences_line_cache), null);
    }

    public static boolean isConnected(Activity caller) {
        ConnectivityManager connMgr = (ConnectivityManager)
                caller.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static void getMenuFromInternet(Activity caller) {
        if (isConnected(caller)) {
            new GetMenuJsonTask(caller).execute(caller.getString(R.string.menu_service_url));
        } else {
            Toast.makeText(caller.getApplicationContext(), "Sem conexão para atualizar o cardápio!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void getLineFromInternet(Activity caller) {
        if (!Util.canEvaluate()) {
            return;
        }
        if (isConnected(caller)) {
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
            Central.initialize(jsonMenu.getJSONObject(Bandejao.CENTRAL.value));
            Quimica.initialize(jsonMenu.getJSONObject(Bandejao.QUIMICA.value));
            Fisica.initialize(jsonMenu.getJSONObject(Bandejao.FISICA.value));
            PCO.initialize(jsonMenu.getJSONObject(Bandejao.PCO.value));
            setMenuDate(PCO.getInstance().getDay(6).getDate());

            String jsonLine = Util.getLineFromCache(caller);
            if (jsonLine == null) {
                Util.getLineFromInternet(caller);
            } else {
                Util.jsonLineToModel(caller, jsonLine);
            }

            if (caller instanceof MainActivity) {
                ((MainActivity)caller).showMenuContentOnScreen();
            } else {
                ((MoreDetailsActivity) caller).updateMenuContentOnScreen();
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

    public static boolean isClosed(Bandejao bandejao, int dayOfWeek, int period) {
        Bandex bandex = BandexFactory.getRestaurant(bandejao);
        return !(bandex.getDay(dayOfWeek).getMeal(period).isAvailable());
    }

    public static boolean isClosed(Bandejao bandejao) {
        return isClosed(bandejao, Util.getDayOfWeek(), Util.getPeriodToShowMenu());
    }

    public static boolean canEvaluate() {
        return !(getPeriodToShowLine() == Periodo.NOTHING || allClosed());
    }

    public static boolean allClosed() {
        boolean closed = true;
        for (Bandejao bandejao : Bandejao.possibleValues()) {
            closed = closed && isClosed(bandejao);
        }
        return closed;
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
        //return getPeriodToShowMenu(); // P/ testes
        if (inRangeOfLunch()) {
            return Periodo.LUNCH;
        } else if (inRangeOfDinner()) {
            return Periodo.DINNER;
        } else return Periodo.NOTHING;
    }

    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        }
        else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }


    static void findViewsWithText(List<View> outViews, ViewGroup parent, String targetDescription) {
        if (parent == null || TextUtils.isEmpty(targetDescription)) {
            return;
        }
        final int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = parent.getChildAt(i);
            final CharSequence desc = child.getContentDescription();
            if (!TextUtils.isEmpty(desc) && targetDescription.equals(desc.toString())) {
                outViews.add(child);
            } else if (child instanceof ViewGroup && child.getVisibility() == View.VISIBLE) {
                findViewsWithText(outViews, (ViewGroup) child, targetDescription);
            }
        }
    }


    public static void setOverflowButtonColor(final Activity activity) {
        final String overflowDescription = activity.getString(R.string.abc_action_menu_overflow_description);
        final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        final ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ArrayList<View> outViews = new ArrayList<View>();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    decorView.findViewsWithText(outViews, overflowDescription,
                            View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                } else {
                    findViewsWithText(outViews, decorView,
                            overflowDescription);
                }
                if (outViews.isEmpty()) {
                    return;
                }
                ImageView overflow= (ImageView) outViews.get(0);
                overflow.setColorFilter(Color.WHITE);
                removeOnGlobalLayoutListener(decorView,this);
            }
        });
    }


}
