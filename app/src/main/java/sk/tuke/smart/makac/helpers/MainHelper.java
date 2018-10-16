package sk.tuke.smart.makac.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import sk.tuke.smart.makac.R;

/**
 * Created by Jakub on 5.11.2017.
 *
 */

public final class MainHelper {

    public static final int RUNNING = 0;
    public static final int WALKING = 1;
    public static final int CYCLING = 2;

    public static String user;
    public static String userPassword;
    public static String userGender;
    public static int userWeight = 80;
    public static int userAge;
    public static int trackId;

    public static boolean dbIsInitialized = false;


    /** constants */
    private static final float MpS_TO_MIpH = 2.23694f;
    private static final float KM_TO_MI = 0.62137119223734f;
    private static final float MINpKM_TO_MINpMI = 1.609344f;


    /**
     * return string of time in format HH:MM:SS
     */
    @SuppressLint("DefaultLocale")
    public static String formatDuration(long time){
        long hours = time / 3600;
        long minutes = (time % 3600) / 60;
        long seconds = time % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * convert m to km and round to 2 decimal places and return as string
     */
    public static String formatDistance(double n){
        n = n / 1000;
        n = Math.round(n * 100);
        n = n / 100;
        return Double.toString(n);
    }

    /**
     * round number to 2 decimal places and return as string
     */
    public static String formatPace(double n){
        n = Math.round(n * 100);
        n = n / 100;
        return Double.toString(n);
    }

    /**
     * round number to integer
     */
    public static String formatCalories(double n){
        int cal = (int)Math.round(n);
        return Integer.toString(cal);
    }

    /**
     * convert km to mi (multiply with corresponding constant)
     */
    public static double kmToMi(double n){
        return n * KM_TO_MI;
    }

    /**
     * convert m/s to mi/h (multiply with corresponding constant)
     */
    public static double mpsToMiph(double n){
        return n * MpS_TO_MIpH;
    }

    /**
     * convert min/km to min/mi (multiply with corresponding constant)
     */
    public static double minpkmToMinpmi(double n){
        return n * MINpKM_TO_MINpMI;
    }

    public static String getSportActivity(Context context, int sport){
        switch(sport){
            case MainHelper.RUNNING:
                return context.getResources().getString(R.string.running);
            case MainHelper.WALKING:
                return context.getResources().getString(R.string.walking);
            case MainHelper.CYCLING:
                return context.getResources().getString(R.string.cycling);
            default:
                return context.getResources().getString(R.string.unk);
        }
    }

    /**
     * check internet connection
     */
    public static boolean isNetworkOnline(Context con) {
        boolean status;
        try {
            ConnectivityManager cm = (ConnectivityManager) con
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                netInfo = cm.getNetworkInfo(1);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                    status = true;
                } else {
                    status = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;
    }
}

