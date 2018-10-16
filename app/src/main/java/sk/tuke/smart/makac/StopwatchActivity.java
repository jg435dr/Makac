package sk.tuke.smart.makac;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sk.tuke.smart.makac.helpers.MainHelper;
import sk.tuke.smart.makac.services.TrackerService;

public class StopwatchActivity extends MenuActivity {


    @BindView(R.id.button_stopwatch_start)
    Button stopwatch_start;
    @BindView(R.id.button_stopwatch_endworkout)
    Button stopwatch_endWorkout;
    @BindView(R.id.button_stopwatch_selectsport)
    Button stopwatch_selectSport;
    @BindView(R.id.textview_stopwatch_duration)
    TextView stopwatch_duration;
    @BindView(R.id.textview_stopwatch_distance)
    TextView stopwatch_distance;
    @BindView(R.id.textview_stopwatch_pace)
    TextView stopwatch_pace;
    @BindView(R.id.textview_stopwatch_calories)
    TextView stopwatch_calories;
    @BindView(R.id.textview_stopwatch_distanceunit)
    TextView stopwatch_distanceUnit;
    private BroadcastReceiver receiver;
    private boolean state;
    private int tempSelectSport;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);
        ButterKnife.bind(this);

        setToolbar();
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        initializeVariables();
        if (!isMyServiceRunning(TrackerService.class)) {
            this.startService(new Intent(this, TrackerService.class));
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateScreen(intent);
            }
        };
    }

    private void setToolbar(){
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_stopwatch);
        myToolbar.setTitle(R.string.stopwatch);
        setSupportActionBar(myToolbar);
    }

    @OnClick(R.id.button_stopwatch_start)
    public void toggle() {
        if (state) {
            if (stopwatch_start.getText().equals(getResources().getString(R.string.start))) {
                stopwatch_selectSport.setClickable(false);
                stopwatch_selectSport.setVisibility(View.GONE);
                sendBroadcast(new Intent().setAction("sk.tuke.smart.makac.COMMAND_START").putExtra("Sport", tempSelectSport % 3));
            } else {
                sendBroadcast(new Intent().setAction("sk.tuke.smart.makac.COMMAND_CONTINUE"));
            }
            stopwatch_start.setText(R.string.stopwatch_stop);
            stopwatch_endWorkout.setVisibility(View.GONE);

        } else {
            stopwatch_start.setText(R.string.stopwatch_continue);
            sendBroadcast(new Intent().setAction("sk.tuke.smart.makac.COMMAND_PAUSE"));
            stopwatch_endWorkout.setVisibility(View.VISIBLE);
        }
        state = !state;
    }

    @OnClick(R.id.button_stopwatch_endworkout)
    public void endProcess() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.end_actual_workout)
                .setMessage(R.string.reset_counters)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sendBroadcast(new Intent().setAction("sk.tuke.smart.makac.COMMAND_STOP"));
                        receiver = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                if(intent.getExtras().getInt("State") == TrackerService.STATE_STOPPED) {
                                    initialSettings();
                                    if (MainHelper.trackId >= 0) {
                                        MainHelper.trackId++;
                                    }
                                    startActivity(new Intent(getApplicationContext(), WorkoutDetailActivity.class).putExtras(intent.getExtras()));
                                }
                            }
                        };
                        registerReceiver(receiver, new IntentFilter("sk.tuke.smart.makac.TICK"));
                    }
                }).setNegativeButton(R.string.no, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @OnClick(R.id.button_stopwatch_selectsport)
    public void selectSport(View view) {
        switch (++tempSelectSport % 3) {
            case 0:
                stopwatch_selectSport.setText(R.string.running);
                break;
            case 1:
                stopwatch_selectSport.setText(R.string.walking);
                break;
            case 2:
                stopwatch_selectSport.setText(R.string.cycling);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter("sk.tuke.smart.makac.TICK"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @SuppressLint("DefaultLocale")
    private void updateScreen(Intent intent) {
        Double distance;
        Bundle extras;
        if((extras = intent.getExtras()) != null){
            distance = extras.getDouble("Distance");
            stopwatch_duration.setText(MainHelper.formatDuration(extras.getLong("Duration")));
            stopwatch_pace.setText(String.format("%.2f", extras.getDouble("Pace")*3.6));
            if (distance < 1000) {
                stopwatch_distance.setText(String.format("%.2f", extras.getDouble("Distance")));
                stopwatch_distanceUnit.setText(R.string.metres);
            } else {
                stopwatch_distance.setText(String.format("%.2f", extras.getDouble("Distance") / 1000));
                stopwatch_distanceUnit.setText(R.string.kilometres);
            }
            stopwatch_calories.setText(String.format("%.2f", extras.getDouble("Calories")));
        }
    }

    private void initializeVariables() {
        state = true;
        tempSelectSport = 0;
    }

    private void initialSettings(){
        initializeVariables();
        stopwatch_selectSport.setVisibility(View.VISIBLE);
        stopwatch_start.setText(R.string.start);
        stopwatch_endWorkout.setVisibility(View.INVISIBLE);
        stopwatch_selectSport.setClickable(true);
        stopwatch_selectSport.setText(R.string.running);

        stopwatch_duration.setText(R.string.zero_time);
        stopwatch_pace.setText(R.string.zero);
        stopwatch_distance.setText(R.string.zero);
        stopwatch_distanceUnit.setText(R.string.kilometres);
        stopwatch_calories.setText(R.string.start_calories);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if(manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
