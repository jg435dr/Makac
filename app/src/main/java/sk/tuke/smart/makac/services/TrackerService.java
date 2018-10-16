package sk.tuke.smart.makac.services;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import sk.tuke.smart.makac.helpers.DatabaseHelper;
import sk.tuke.smart.makac.helpers.MainHelper;
import sk.tuke.smart.makac.helpers.SportActivities;

/**
 * Created by Jakub on 4.11.2017.
 *
 */

public class TrackerService extends Service {

    public static final int STATE_RUNNING = 0;
    public static final int STATE_STOPPED = 1;
    public static final int STATE_CONTINUE = 2;
    public static final int STATE_PAUSED = 3;

    private static final int locationTime = 3;

    private BroadcastReceiver broadcastReceiver;
    private LocationManager timeLocManager, distanceLocManager;
    private LocationListener locationListener, distanceLocListener;
    private Location oldLocation, currentLocation;
    private int sportActivity, stateOfService;
    private long time, oldTime, caloriesTime;
    private double distance, calories;
    private boolean toggle, tempDistance;
    private float pace;
    private List<List<Location>> finalPositionList;
    private List<Location> positionList;
    private List<Float> speedList;
    private Timer timer;
    private DatabaseHelper database;

    public long getDuration() {
        return time;
    }
    public double getDistance() {
        return distance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setVariables();
        setLocManager();
        setBroadcastReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        timeLocManager.removeUpdates(locationListener);
        distanceLocManager.removeUpdates(distanceLocListener);
        timer.cancel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setVariables() {
        currentLocation = null;
        distance = 0;
        time = 0;
        oldTime = 0;
        pace = 0;
        calories = 0;
        toggle = false;
        speedList = new ArrayList<>();
        finalPositionList = new ArrayList<>();
        positionList = new ArrayList<>();
        database = new DatabaseHelper(getApplicationContext());
    }

    private void setBroadcastReceiver() {

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                if (intent.getAction().equals("sk.tuke.smart.makac.COMMAND_PAUSE")) {
                    stateOfService = STATE_PAUSED;
                    toggle = false;
                    finalPositionList.add(positionList);
                    positionList = new ArrayList<>();
                } else if (intent.getAction().equals("sk.tuke.smart.makac.COMMAND_START")) {
                    setVariables();
                    // musis jak prve ziskat pozicu a nie za timerom a za setVariables
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    oldLocation = timeLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    positionList.add(oldLocation);
                    setTimer();
                    stateOfService = STATE_RUNNING;
                    sportActivity = intent.getIntExtra("Sport", 0);
                    toggle = true;
                } else if (intent.getAction().equals("sk.tuke.smart.makac.COMMAND_CONTINUE")) {
                    stateOfService = STATE_CONTINUE;
                    toggle = true;
                    tempDistance = true;
                } else if (intent.getAction().equals("sk.tuke.smart.makac.COMMAND_STOP")) {
                    stateOfService = STATE_STOPPED;
                    toggle = false;
                    currentLocation = timeLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    positionList.add(currentLocation);
                    sendMessage();
                }
            }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction("sk.tuke.smart.makac.COMMAND_START");
        filter.addAction("sk.tuke.smart.makac.COMMAND_PAUSE");
        filter.addAction("sk.tuke.smart.makac.COMMAND_CONTINUE");
        filter.addAction("sk.tuke.smart.makac.COMMAND_STOP");
        registerReceiver(broadcastReceiver, filter);
    }

    private void setTimer(){
        if(timer != null){
            return;
        }
        timer = new java.util.Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if(toggle) {
                    sendMessage();
                    time++;
                }
            }
        }, 0, 1000);
    }


    private void setLocManager() {
        distanceLocManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        timeLocManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(currentLocation != null) {
                    if (currentLocation.getLatitude() == location.getLatitude() &&
                            currentLocation.getLongitude() == location.getLongitude()) {
                        return;
                    }
                }
                currentLocation = location;
                positionList.add(currentLocation);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        distanceLocListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(currentLocation != null) {
                    if (currentLocation.getLatitude() == location.getLatitude() &&
                            currentLocation.getLongitude() == location.getLongitude()) {
                        return;
                    }
                }
                currentLocation = location;
                positionList.add(currentLocation);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        timeLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationTime, 2, locationListener);
        distanceLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, distanceLocListener);
    }

    private void sendMessage(){
        Intent intent = new Intent();
        intent.setAction("sk.tuke.smart.makac.TICK");
        Bundle extras = new Bundle();

        extras.putLong("Duration", time);
        extras.putInt("Sport", sportActivity);
        extras.putInt("State", stateOfService);

        pace = 0f;
        if(currentLocation != null && oldLocation != null && currentLocation != oldLocation){
            locationUpdated();
        } else {
            extras.putDouble("Pace", pace);
        }

        if(stateOfService == STATE_STOPPED){
            extras.putSerializable("PositionList", (Serializable) finalPositionList);
            extras.putDouble("Pace", countAvgSpeed());
            extras.putBoolean("LatLng", false);
        } else {
            extras.putSerializable("PositionList", (Serializable) positionList);
            extras.putDouble("Pace", pace);
        }

        extras.putDouble("Calories", calories);
        extras.putDouble("Distance", distance);

        if(oldLocation == null){
            oldLocation = currentLocation;
        }

        saveInDB(extras);
        intent.putExtras(extras);
        sendBroadcast(intent);
    }

    private void locationUpdated(){
        Float tickDistance;
        if((tickDistance = currentLocation.distanceTo(oldLocation)) >= 2) {
            if (!tempDistance) {
                distance += tickDistance;
            } else {
                if (tickDistance <= 100) {
                    distance += tickDistance;
                    tempDistance = false;
                } else {
                    // ked sa po continue cas nepripocita tak treba pocitadlo vynulovat
                    oldTime = 0;
                }
            }
            pace = tickDistance / (time - oldTime);
            speedList.add(pace);
            caloriesTime += time - oldTime;
            if (speedList.size() >= 2) {
                calories = SportActivities.countCalories(sportActivity, MainHelper.userWeight, speedList, caloriesTime / 3600D);
            }

            oldTime = time;
        }
        oldLocation = currentLocation;
    }

    private double countAvgSpeed(){
        if(speedList.size() == 0){
            return 0.0;
        }
        double temp = 0;
        for(float pace : speedList){
            temp += pace;
        }
        if(temp == 0){
            return 0.0;
        } else {
            return temp/speedList.size();
        }
    }

    private void saveInDB(Bundle extras){
        if(MainHelper.user != null) {
            if (pace > 0 || stateOfService == STATE_STOPPED) {
                database.insertLocationDB(extras);
            }
        }
    }
}
