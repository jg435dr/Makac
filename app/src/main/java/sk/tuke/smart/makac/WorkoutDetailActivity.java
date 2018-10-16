package sk.tuke.smart.makac;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sk.tuke.smart.makac.helpers.MainHelper;

public class WorkoutDetailActivity extends MenuActivity {

    @BindView(R.id.textview_workoutdetail_activitydate)
    TextView tvActivityDate;
    @BindView(R.id.textview_workoutdetail_sportactivity)
    TextView tvSportActivity;
    @BindView(R.id.textview_workoutdetail_calories)
    TextView tvCalories;
    @BindView(R.id.textview_workoutdetail_distance)
    TextView tvDistance;
    @BindView(R.id.textview_workoutdetail_duration)
    TextView tvDuration;
    @BindView(R.id.textview_workoutdetail_pace)
    TextView tvPace;
    @BindView(R.id.edittext_workoutdetail_share_message)
    EditText etShareMessage;

    private int sport;
    private long duration;
    private double distance, pace, calories;
    private boolean isLocationList;
    private Bundle extras;
    private StringBuilder stringBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);
        ButterKnife.bind(this);

        setToolbar();
        setVariables();
        setTrackedData();
    }

    private void setToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_workoutdetail);
        myToolbar.setTitle(R.string.my_workout);
        setSupportActionBar(myToolbar);
        ActionBar actionBar;
        if ((actionBar = getSupportActionBar()) != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @SuppressLint("DefaultLocale")
    private void setTrackedData() {
        if (!extras.getBoolean("LatLng")) {
            tvActivityDate.setText(SimpleDateFormat.getDateTimeInstance().format(new Date()));
        } else {
            tvActivityDate.setText(extras.getString("Date"));
        }
        tvDuration.setText(MainHelper.formatDuration(duration));
        tvDistance.setText(String.format("%.2f", distance));
        tvPace.setText(String.format("%.2f", pace));
        tvCalories.setText(String.format("%.2f", calories));
        tvSportActivity.setText(MainHelper.getSportActivity(this, sport));
    }

    @OnClick(R.id.button_workoutdetail_showmap)
    public void showMap() {
        if (isLocationList) {
            startActivity(new Intent(this, MapsActivity.class).putExtras(extras));
            return;
        }
        Toast.makeText(this, R.string.no_data, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("DefaultLocale")
    private void setVariables() {
        if ((extras = getIntent().getExtras()) != null) {
            sport = extras.getInt("Sport");
            duration = extras.getLong("Duration");
            distance = extras.getDouble("Distance") / 1000D;
            pace = extras.getDouble("Pace") * 3.6;
            calories = extras.getDouble("Calories");

            String shareString = getString(R.string.i_was_out_for) + MainHelper.getSportActivity(this, sport) +
                    getString(R.string.i_tracked) + String.format("%.2f", distance) + getString(R.string.kilometres)
                    + getString(R.string.in) + MainHelper.formatDuration(extras.getLong("Duration"));
            etShareMessage.setText(shareString);
            setLocURLList();
        }
    }

    private void setLocURLList() {
        stringBuilder = new StringBuilder();
        isLocationList = false;
        stringBuilder.append("http://maps.googleapis.com/maps/api/staticmap?size=600x600&path=");
        if (!extras.getBoolean("LatLng")) {
            locationsWorkoutDetail();
        } else {
            locationsDb();
        }
        stringBuilder.append("&sensor=false");
    }

    private void locationsDb(){
        List<LatLng> positionList = (List<LatLng>) getIntent().getExtras().getSerializable("PositionList");
        if (positionList != null && positionList.size() > 0) {
            LatLng lastPosition = positionList.get(positionList.size() - 1);
            for (LatLng position : positionList) {
                if(position != null) {
                    stringBuilder.append(position.latitude);
                    stringBuilder.append(",");
                    stringBuilder.append(position.longitude);
                    if (position != lastPosition) {
                        stringBuilder.append("|");
                    } else {
                        isLocationList = true;
                    }
                }
            }
        }
    }

    private void locationsWorkoutDetail(){
        List<List<Location>> positionList = (List<List<Location>>) extras.getSerializable("PositionList");
        if (positionList != null) {
            if(positionList.get(0) != null && positionList.get(0).get(0) != null){
                List<Location> tempList = positionList.get(positionList.size()-1);
                Location lastLocation = tempList.get(tempList.size()-1);
                for(List<Location> list : positionList){
                    for(Location location : list){
                        if (location != null) {
                            stringBuilder.append(location.getLatitude());
                            stringBuilder.append(",");
                            stringBuilder.append(location.getLongitude());
                            if (location != lastLocation) {
                                stringBuilder.append("|");
                            } else {
                                isLocationList = true;
                            }
                        }
                    }
                }
            }
        }
    }

    public void onClick(View view) {
        if (view.getId() == R.id.button_workoutdetail_emailshare) {
            if (!initShareIntent("com.google.android.gm")) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.putExtra(Intent.EXTRA_TEXT, etShareMessage.getText().toString());
                emailIntent.setData(Uri.parse("mailto:"));
                startActivity(Intent.createChooser(emailIntent, "Send feedback"));
            }
        }
        if (view.getId() == R.id.button_workoutdetail_gplusshare) {
            if (!initShareIntent("com.google.android.apps.plus")) {
                Toast.makeText(this, "Google plus was not found in your device.", Toast.LENGTH_SHORT).show();
            }
        }
        if (view.getId() == R.id.button_workoutdetail_twittershare) {
            if (!initShareIntent("com.twitter.android")) {
                Toast.makeText(this, "Twitter was not found in your device.", Toast.LENGTH_SHORT).show();
            }
        }
        if (view.getId() == R.id.button_workoutdetail_fbsharebtn) {
            if (!initShareIntent("com.facebook.katana")) {
                Toast.makeText(this, "Facebook was not found in your device.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.btGetPlace)
    public void startShowPlaces(){
        requestPermission();
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        builder.setLatLngBounds(new LatLngBounds(new LatLng(51.5152192,-0.1321900), new LatLng(51.5166013,-0.1299262)));
        try {
            Intent intent = builder.build(this);
            startActivityForResult(intent, 1);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private boolean initShareIntent(String type) {
        boolean found = false;
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");

        // gets the list of intents that can be loaded.

        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);
        if (!resInfo.isEmpty()) {
            for (ResolveInfo info : resInfo) {
                if (info.activityInfo.packageName.toLowerCase().contains(type) ||
                        info.activityInfo.name.toLowerCase().contains(type)) {
                    share.putExtra(Intent.EXTRA_TEXT, etShareMessage.getText().toString());
                    if (isLocationList) {
                        DownloadTask task = new DownloadTask();
                        task.execute(stringBuilder.toString());
                        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File("sdcard/photoalbum/downloaded_image.jpg")));
                    }
                    share.setPackage(info.activityInfo.packageName);
                    found = true;
                    break;
                }
            }
            startActivity(Intent.createChooser(share, "Select"));
        }
        return found;
    }

    public static class DownloadTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                File new_folder = new File("sdcard/photoalbum");
                if (!new_folder.exists()) {
                    new_folder.mkdir();
                }

                File input_file = new File(new_folder, "downloaded_image.jpg");
                InputStream inputStream = new BufferedInputStream(url.openStream(), 8192);
                byte[] data = new byte[1024];
                int count;
                OutputStream outputStream = new FileOutputStream(input_file);
                while ((count = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                }
                inputStream.close();
                outputStream.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 101:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "This app requires location permissions to be granted", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }
}
