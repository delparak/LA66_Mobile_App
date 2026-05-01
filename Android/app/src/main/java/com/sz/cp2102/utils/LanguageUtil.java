package com.sz.cp2102.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import com.sz.cp2102.BleActiity;

import java.util.Locale;

/**
 * Language switching
 * Created by 41455 on 2016/10/13.
 */
public class LanguageUtil {
    /**
     * @param isEnglish true  ：when English is selected, mark Chinese as unselected
     *                  false ：when Chinese is selected, mark English as unselected
     */
    public static void set(boolean isEnglish, Activity activity) {
        Configuration configuration = activity.getResources().getConfiguration();
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        if (isEnglish) {
            //Set English
            configuration.locale = Locale.ENGLISH;
        } else {
            //Set Chinese
            configuration.locale = Locale.SIMPLIFIED_CHINESE;
        }
        //Update configuration
        activity.getResources().updateConfiguration(configuration, displayMetrics);

        //After updating the language, destroy and redraw the current screen

        activity.finish();

        Intent it = new Intent(activity, BleActiity.class);
        //Clear the task stack so the currently opened Activity is at the top of the foreground task stack
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(it);
    }
}
