package com.udacity.demur.bakingapp.service;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.BulletSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import java.io.IOException;

public class Utilities {
    /*
     * This function is the product of gar at https://stackoverflow.com/a/4009133
     * suggested to use by Udacity to implement network connection check
     * */
    public static boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getGridLayoutColumnCount(Context context) {
        boolean isLandscape = false;
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            isLandscape = true;
        switch (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                if (isLandscape)
                    return 3;
                else
                    return 2;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
            case Configuration.SCREENLAYOUT_SIZE_UNDEFINED:
            default:
                if (isLandscape)
                    return 2;
                else
                    return 1;
        }
    }

    public static SpannableString getLargeBoldItalic(String string) {
        SpannableString spannableString = new SpannableString(string);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, string.length(), 0);
        spannableString.setSpan(new RelativeSizeSpan(1.35f), 0, string.length(), 0);
        return spannableString;
    }

    public static SpannableString getBulletItem(String string) {
        /*
         * The solution how to display bulleted list was publish by Diego Frehner at
         * https://stackoverflow.com/a/6954941
         * */
        SpannableString spannableString = new SpannableString(string);
        spannableString.setSpan(new BulletSpan(15), 0, string.length(), 0);
        return spannableString;
    }
}