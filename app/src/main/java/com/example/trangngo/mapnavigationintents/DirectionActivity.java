package com.example.trangngo.mapnavigationintents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.directions.route.Route;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by trangngo on 7/13/17.
 */

public class DirectionActivity  extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraMoveListener {

    public static String TAG = "DirectionActivity";
    FloatingActionButton fabRecenter;
    List<Polyline> polylines;

    ArrayList<? extends Route> routes;

    GoogleMap mMap;
    boolean re_center = true;
    Marker marker;
    LatLng myLatLng;

    private static final int[] COLORS = new int[]{R.color.primary_dark, R.color.primary, R.color.primary_light, R.color.accent, R.color.primary_dark_material_light};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);

        polylines = new ArrayList<>();

        fabRecenter = (FloatingActionButton) findViewById(R.id.fab_recenter);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_direction);
        mapFragment.getMapAsync(this);

        routes = getIntent().getParcelableArrayListExtra("routes");

        fabRecenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                re_center = true;
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(GPSService.str_gps_receiver));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setPadding(0, 300, 0, 0);

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        drawNavigateDirection((ArrayList<Route>) routes);
        Intent gpsServiceIntent = new Intent(this, GPSService.class);
        startService(gpsServiceIntent);
        if(myLatLng != null){
            marker = mMap.addMarker(new MarkerOptions()
                    .position(myLatLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_direction_arrows))
                    .anchor(0.5f, 0.5f)
            );
        }

        mMap.setOnCameraMoveListener(this);


    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra("location");
            myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            ViewMap.animateMarker(mMap, marker, myLatLng, false);

            if(re_center){
                updateCameraBearing(mMap, location.getBearing());
            }
            boolean isOnPath = false;
            for (Polyline polyline : polylines) {
                if (PolyUtil.isLocationOnPath(myLatLng, polyline.getPoints(), false, 50)) {
                    isOnPath = true;
                    break;
                }
            }

            if (!isOnPath) {
                Log.d(TAG, "false");
            }
        }};

    void drawNavigateDirection(ArrayList<Route> routes){

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }
        for(int j = 0; j < routes.size(); j++){

            int colorIndex = j % COLORS.length;
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(getResources().getColor(COLORS[colorIndex]));
            polylineOptions.width(10+j*3);
            polylineOptions.addAll(routes.get(j).getPoints());
            Polyline polyline = mMap.addPolyline(polylineOptions);

            polylines.add(polyline);

            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ arrayList.get(i).getDistanceValue()+": duration - "+ arrayList.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();

        }
    }



    private void updateCameraBearing(GoogleMap googleMap, float bearing) {
        if ( googleMap == null) return;
        CameraPosition camPos = CameraPosition
                .builder(
                        googleMap.getCameraPosition() // current Camera
                )
                .target(myLatLng)
                .bearing(bearing)
                .tilt(45)
                .zoom(18)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));

    }

    @Override
    public void onCameraMove() {
        re_center = false;
    }
}
