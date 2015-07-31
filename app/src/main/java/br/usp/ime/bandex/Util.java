package br.usp.ime.bandex;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Wagner on 26/07/2015.
 */
public class Util {
    static Calendar cal = Calendar.getInstance();
    static int hours = cal.get(Calendar.HOUR);
    static int minutes = cal.get(Calendar.MINUTE);
    static int period = 0; // 0 = lunch, 1 = dinner
    static int day_of_week = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Calendar.Monday == 2. In this code, Monday = 0.
    static Date entry_date = null;

    static {
        // Choose whether to show the lunch or the dinner
        if (hours >= 14 && minutes >= 30)
            period = 1;
        else period = 0;
    }

    public static void setEntry_date(Date entry_date) {
        Util.entry_date = entry_date;

    }

    public static void setCustomActionBar(final ActionBarActivity context) {
        android.support.v7.app.ActionBar mActionBar = context.getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(context);

        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView tvActionBar = (TextView) mCustomView.findViewById(R.id.title_text_action_bar);
        Typeface face= Typeface.createFromAsset(context.getAssets(), "fonts/Raleway-Bold.ttf");
        tvActionBar.setText(context.getTitle());
        tvActionBar.setTypeface(face);

        ImageButton imageButton = (ImageButton) mCustomView
                .findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Refresh Clicked!",
                        Toast.LENGTH_LONG).show();
            }
        });
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        context.getSupportActionBar().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.actionbar_background2));
    }
}
