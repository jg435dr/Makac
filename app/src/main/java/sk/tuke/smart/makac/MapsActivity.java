package sk.tuke.smart.makac;

import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jakub on 2.11.2017.
 *
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<LatLng> latLngs;
    private List<List<Location>> finalPositionList;
    private LatLng firstLocation;
    private LatLngBounds.Builder builder;

    private static final CharSequence[] MAP_TYPE_ITEMS =
            {"Road Map", "Hybrid", "Satellite", "Terrain"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_maps_workoutmap);
        mapFragment.getMapAsync(this);

        initiateVariables();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        centerCamera();
        showMapTypeSelectorDialog();
    }

    @SuppressWarnings("unchecked")
    private void initiateVariables(){
        latLngs = new ArrayList<>();
        builder = new LatLngBounds.Builder();

        if(getIntent().getExtras().getBoolean("LatLng")){
            List<LatLng> positionList = (List<LatLng>)getIntent().getExtras().getSerializable("PositionList");
            if(positionList != null) {
                firstLocation = positionList.get(0);
                latLngs = positionList;
                for (LatLng position : positionList) {
                    builder.include(position);
                }
            }
        } else {
            finalPositionList = (List<List<Location>>) getIntent().getExtras().getSerializable("PositionList");
            if (finalPositionList != null && finalPositionList.size() > 0 &&
                    finalPositionList.get(0).size() > 0) {
                firstLocation = new LatLng(finalPositionList.get(0).get(0).getLatitude(), finalPositionList.get(0).get(0).getLongitude());
                extractLatLng();
            }
        }
    }

    private void centerCamera(){
        if(firstLocation != null && builder != null) {
            mMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .addAll(latLngs)
                    .color(Color.RED)
                    .width(5)
                    .geodesic(true));

            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            mMap.addMarker(new MarkerOptions().position(firstLocation).title("Start"));
            int padding = 50;
            LatLngBounds bounds = builder.build();
            final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mMap.animateCamera(cameraUpdate);
                }
            });
        }
    }


     private void extractLatLng(){
        LatLng latLng;
        for(List<Location> list: finalPositionList){
            for(Location location : list){
                latLng = new LatLng(location.getLatitude(), location.getLongitude());
                latLngs.add(latLng);
                builder.include(latLng);
            }
        }
    }

    private void showMapTypeSelectorDialog() {
        // Prepare the dialog by setting up a Builder.
        final String fDialogTitle = getString(R.string.select_map_type);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fDialogTitle);

        // Find the current map type to pre-check the item representing the current state.
        int checkItem = mMap.getMapType() - 1;

        // Add an OnClickListener to the dialog, so that the selection will be handled.
        builder.setSingleChoiceItems(
                MAP_TYPE_ITEMS,
                checkItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        // Locally create a finalised object.

                        // Perform an action depending on which item was selected.
                        switch (item) {
                            case 1:
                                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                break;
                            case 2:
                                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                break;
                            case 3:
                                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                break;
                            default:
                                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        }
                        dialog.dismiss();
                    }
                }
        );

        // Build the dialog and show it.
        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();
    }
}

/**/

