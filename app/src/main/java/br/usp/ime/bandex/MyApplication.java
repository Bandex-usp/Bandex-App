package br.usp.ime.bandex;
import android.app.Application;
import android.os.Build;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParsePushBroadcastReceiver;

/**
 * Created by Wagner on 23/10/2015.
 */
public class MyApplication extends Application
{
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(getApplicationContext(), "5umFg7qGHN5EC2Xf2zfsF0ItLohWt9DZYFuyvwtO", "HAyxaPMWjBhqShnrmQAQJR17Fev41cp6I8NcJo4a");
        ParseInstallation.getCurrentInstallation().saveInBackground();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

        }
    }
}
