package sk.tuke.smart.makac;


import android.support.annotation.NonNull;

/**
 * Created by Jakub on 18.11.2017.
 *
 */

public class DataItem {
    String date;
    long duration;
    int sport;
    float distance, avgPace;

    public DataItem(@NonNull String date, int sport, long duration, float distance, float avgPace) {
        this.date = date;
        this.duration = duration;
        this.distance = distance;
        this.avgPace = avgPace;
        this.sport = sport;
    }
}
