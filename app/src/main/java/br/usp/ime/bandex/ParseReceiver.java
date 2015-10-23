package br.usp.ime.bandex;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.parse.ParsePushBroadcastReceiver;

/**
 * Created by Wagner on 23/10/2015.
 */
public class ParseReceiver extends ParsePushBroadcastReceiver {

    @Override
    protected Bitmap getLargeIcon(Context context, Intent intent) {
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.diningicon114);
        return icon;
    }

    @Override
    protected int getSmallIconId(Context context, Intent intent) {
        return R.drawable.diningicon_smallicon_melhorado;
        //return super.getSmallIconId(context, intent);
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        Notification notification = super.getNotification(context, intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification.color = context.getResources().getColor(R.color.amarelo_usp);
        }
        return notification;
    }
}
