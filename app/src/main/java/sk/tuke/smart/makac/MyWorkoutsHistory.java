package sk.tuke.smart.makac;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sk.tuke.smart.makac.helpers.DatabaseHelper;


public class MyWorkoutsHistory extends AppCompatActivity {

    private List<ParseObject> listOfLocations;
    private List<ParseObject> lastLocations;
    private List<DataItem> dataItemList;
    private List<LatLng> positionList;
    private DatabaseHelper database;
    private CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_workouts_history);

        setVariables();
        createDataItems();
        setListOfWorkouts();
    }

    private void setVariables(){
        database = new DatabaseHelper(this);
        if((listOfLocations = database.getListOfWorkouts()) == null){
            startActivity(new Intent(this, StopwatchActivity.class));
        }
        lastLocations = new ArrayList<>();
        dataItemList = new ArrayList<>();
        positionList = new ArrayList<>();
        ActionBar actionBar;
        if ((actionBar = getSupportActionBar()) != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void createDataItems(){
        if(listOfLocations != null) {
            int trackId = -1;
            for (ParseObject object : listOfLocations) {
                if (object.getInt("trackId") != trackId) {
                    trackId = object.getInt("trackId");
                    lastLocations.add(object);
                    String date = (String.valueOf(object.getCreatedAt())).split("G|E")[0];
                    int sport = object.getInt("sportActivity");
                    long duration = object.getLong("time");
                    float distance;
                    if(object.getDouble("distance") != 0){
                        distance = (float)object.getDouble("distance");
                    } else {
                        distance = 0f;
                    }
                    Float pace = (float)(object.getDouble("speed")*3.6);
                    dataItemList.add(new DataItem(date, sport, duration, distance, pace));
                }
            }
        }
    }

    private void setListOfWorkouts(){
        ListView listView = (ListView)findViewById(R.id.activity_history_list);
        adapter = new CustomAdapter(this, R.layout.item_workouts_list, dataItemList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                displayWorkoutDetails(position);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return deleteItems(position);
            }
        });
    }

    private void displayWorkoutDetails(int position){
        ParseObject workoutData = lastLocations.get(position);
        int trackId = workoutData.getInt("trackId");
        for(ParseObject object: listOfLocations){
            if(object.getInt("trackId") == trackId){
                LatLng latLng = new LatLng(object.getDouble("latitude"), object.getDouble("longitude"));
                positionList.add(latLng);
            }
        }
        positionList.remove(positionList.size()-1);

        Bundle extras = new Bundle();
        extras.putString("Date", (String.valueOf(workoutData.getCreatedAt())).split("G|E")[0]);
        extras.putLong("Duration", workoutData.getLong("time"));
        extras.putDouble("Pace", workoutData.getDouble("speed"));
        extras.putDouble("Calories", workoutData.getDouble("calories"));
        extras.putDouble("Distance", workoutData.getDouble("distance"));
        extras.putInt("Sport", workoutData.getInt("sportActivity"));
        extras.putSerializable("PositionList", (Serializable) positionList);
        extras.putBoolean("LatLng", true);

        startActivity(new Intent(getApplicationContext(), WorkoutDetailActivity.class).putExtras(extras));
    }

    private boolean deleteItems(final int position){
        final ParseObject workoutData = lastLocations.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_workout)
                .setMessage(R.string.delete_workout + String.valueOf(workoutData.getCreatedAt())+ "?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int trackId = workoutData.getInt("trackId");
                        if(database.deleteTrack(trackId)) {
                            Iterator<ParseObject> it = listOfLocations.iterator();
                            while (it.hasNext()) {
                                if (it.next().getInt("trackId") == trackId) {
                                    it.remove();
                                }
                            }
                            lastLocations.remove(position);
                            adapter.remove(dataItemList.get(position));
                            adapter.notifyDataSetChanged();
                        }
                    }
                }).setNegativeButton(R.string.no, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }


}
