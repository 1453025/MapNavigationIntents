package com.example.trangngo.mapnavigationintents;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.Language;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.example.trangngo.mapnavigationintents.adapter.InstructionsAdapter;
import com.example.trangngo.mapnavigationintents.model.Instructions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, GoogleMap.OnCameraMoveStartedListener, DirectionCallback {

    private static final int[] COLORS = new int[]{R.color.primary_dark, R.color.primary, R.color.primary_light, R.color.accent, R.color.primary_dark_material_light};
    private static String TAG = "MapsActivity";
    protected LatLng start;
    protected LatLng end;
    List<LatLng> latLngs = new ArrayList<>();
    boolean re_center = false;
    private LatLngBounds homePDD = new LatLngBounds(new LatLng(10.7651909, 106.6619211), new LatLng(10.7773018, 106.6999617));
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private ProgressDialog progressDialog;
    private List<Polyline> polylines;
    private LatLng myLatLng;
    private Location currentLcation;
    private Location newLocation;
    private Marker marker;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double latitude = Double.valueOf(intent.getStringExtra("latutide"));
            double longitude = Double.valueOf(intent.getStringExtra("longitude"));
            currentLcation = intent.getParcelableExtra("location");
            myLatLng = new LatLng(latitude, longitude);

            if (marker == null) {
                marker = mMap.addMarker(new MarkerOptions()
                        .position(myLatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_direction_arrows))
                        .anchor(0.5f, 0.5f)
                );
            }
            animateMarker(marker, myLatLng, false);
            if (re_center) {
                updateCameraBearing(mMap, currentLcation.getBearing());
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

//                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
//                com.google.android.gms.common.api.PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
//                        .getCurrentPlace(mGoogleApiClient, null);
//                result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
//                    @Override
//                    public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
//                        if (!likelyPlaces.getStatus().isSuccess()) {
//                            // Request did not complete successfully
//                            Log.e(TAG, "Place query did not complete. Error: " + likelyPlaces.getStatus().toString());
//                            likelyPlaces.release();
//                            return;
//                        }
//                        // Get the Place object from the buffer.
//                        final PlaceLikelihood place = likelyPlaces.get(0);
//                        Toast.makeText(MapsActivity.this, "Start: " + place.getPlace().toString(), Toast.LENGTH_SHORT).show();
//                        start = place.getPlace().getLatLng();
//                        route();
//
//                    }
//                });
//            if(preLocation==null){
//                preLocation = new Location("");
//                preLocation.setLatitude(latitude);
//                preLocation.setLongitude(longitude);
//            }
//            newLocation.setLatitude(latitude);
//            newLocation.setLongitude(longitude);
//
//
//            //marker.setPosition(mineLatLng);

//
//            goToLocation(mineLatLng,18,60,preLocation.bearingTo(newLocation));
//            preLocation.setLatitude(latitude);
//            preLocation.setLongitude(longitude);
        }
    };
    private List<Instructions> instructionsList;
    private ArrayList<Route> routes;
    private FloatingActionButton fabGetDirection;
    private FloatingActionButton fabStartNavigation;
    private FloatingActionButton fabRecenter;
    private CardView cardView;
    private PlaceAutocompleteFragment autocompleteFragment;
    private ViewPager vpInstructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initInstructions();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        newLocation = new Location("");
        polylines = new ArrayList<>();
        routes = new ArrayList<>();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        fabGetDirection = (FloatingActionButton) findViewById(R.id.fab_get_direction);
        fabStartNavigation = (FloatingActionButton) findViewById(R.id.fab_start_navigation);
        fabRecenter = (FloatingActionButton) findViewById(R.id.fab_recenter);
        //cardView = (CardView) findViewById(R.id.card_view);
        vpInstructions = (ViewPager) findViewById(R.id.vpInstructions);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        InstructionsAdapter instructionsAdapter = new InstructionsAdapter(this, instructionsList);
        vpInstructions.setAdapter(instructionsAdapter);

//        autocompleteFragment = (PlaceAutocompleteFragment)
//                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

//        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//            @Override
//            public void onPlaceSelected(Place place) {
//                // TODO: Get info about the selected place.
//                Log.i(TAG, "Place: " + place.getName());
//                Toast.makeText(MapsActivity.this, "End: " + place.getName(), Toast.LENGTH_SHORT).show();
//                end = place.getLatLng();
//
//            }
//
//            @Override
//            public void onError(Status status) {
//                // TODO: Handle the error.
//                Log.i(TAG, "An error occurred: " + status);
//            }
//        });

        fabGetDirection.setOnClickListener(this);
        fabStartNavigation.setOnClickListener(this);
        fabRecenter.setOnClickListener(this);

        //latLngs = PolyUtil.decode("}qv`Ags~iS?e@~Ci@tB[dDo@`AS`J_Bj@Kb@QlAUxGqAJAYs@eAaC{@mBI]_@w@{@iB_AuBkBmEsAyCm@aBK[We@cBuDuCoGJSJc@NoJDoHTsNF[HCPOFO?QGSMKHUlA_G`CwKlAgGbBwHV_BH@ZKLKDQA_@lMqD");

    }

    public void route() {

        Log.d(TAG, "route: ");
//
//        if (start == null) {
//            Toast.makeText(getApplicationContext(), "Can't get LatLng from start place!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (end == null) {
//            Toast.makeText(getApplicationContext(), "Can't get LatLng from end place!", Toast.LENGTH_SHORT).show();
//            return;
//        }
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Fetching route information.", true);

        GoogleDirection.withServerKey("AIzaSyA8FkLNAIyrX6xTkytf05cbKsnaOeOglso")
                .from(new LatLng(10.76737, 106.661868))
                .to(new LatLng(10.7773018, 106.6995))
                .language(Language.VIETNAMESE)
                .alternativeRoute(true)
                .execute(this);
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

        Intent gpsServiceIntent = new Intent(this, GPSService.class);
        startService(gpsServiceIntent);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }

            return;
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homePDD.getCenter(), 1));

        mMap.setOnCameraMoveStartedListener(this);

        mMap.setMyLocationEnabled(true);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MapsActivity.this, "Permission denied to get location", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void updateCameraBearing(GoogleMap googleMap, float bearing) {
        if (googleMap == null) return;
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

    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;
        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(TAG, connectionResult.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_get_direction: {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                        .getCurrentPlace(mGoogleApiClient, null);
                result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                    @Override
                    public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                        if (!likelyPlaces.getStatus().isSuccess()) {
                            // Request did not complete successfully
                            Log.e(TAG, "Place query did not complete. Error: " + likelyPlaces.getStatus().toString());
                            likelyPlaces.release();
                            return;
                        }
                        // Get the Place object from the buffer.
                        final PlaceLikelihood place = likelyPlaces.get(0);
                        Toast.makeText(MapsActivity.this, "Start: " + place.getPlace().toString(), Toast.LENGTH_SHORT).show();
                        start = place.getPlace().getLatLng();
                        route();

                    }
                });
            }
            break;
            case R.id.fab_start_navigation: {
                updateCameraBearing(mMap, currentLcation.getBearing());
                fabRecenter.setVisibility(View.VISIBLE);
                fabGetDirection.setVisibility(View.GONE);
                fabStartNavigation.setVisibility(View.GONE);
                cardView.setVisibility(View.GONE);
            }
            break;
            case R.id.fab_recenter:
                re_center = true;
                break;
            default:
                break;
        }
    }

    void drawNavigateDirection(List<Route> routes) {

        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }
        for (int i = 0; i < routes.size(); i++) {
            int colorIndex = i % COLORS.length;
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(getResources().getColor(COLORS[colorIndex]));
            polylineOptions.width(10 + i * 3);
            polylineOptions.addAll(routes.get(i).getOverviewPolyline().getPointList());
            Polyline polyline = mMap.addPolyline(polylineOptions);
            polylines.add(polyline);

            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ routes.get(i).getDistanceValue()+": duration - "+ routes.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();

        }
    }

    void drawNavigateDirection(Route routes, int index) {
        int colorIndex = index % COLORS.length;
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(getResources().getColor(COLORS[colorIndex]));
        polylineOptions.width(10 + index * 3);
        polylineOptions.addAll(routes.getOverviewPolyline().getPointList());
        Polyline polyline = mMap.addPolyline(polylineOptions);
        polylines.add(polyline);

        //Toast.makeText(getApplicationContext(),"Route "+ (index+1) +": distance - "+ routes.get(index).getDistanceValue()+": duration - "+ routes.get(index).getDurationValue(),Toast.LENGTH_SHORT).show();

    }

    void drawNavigateDirection(List<LatLng> latLngs, int index) {
        int colorIndex = index % COLORS.length;
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(getResources().getColor(COLORS[colorIndex]));
        polylineOptions.width(10 + index * 3);
        polylineOptions.addAll(latLngs);
        Polyline polyline = mMap.addPolyline(polylineOptions);
        polylines.add(polyline);

        //Toast.makeText(getApplicationContext(),"Route "+ (index+1) +": distance - "+ routes.get(index).getDistanceValue()+": duration - "+ routes.get(index).getDurationValue(),Toast.LENGTH_SHORT).show();

    }
    @Override
    public void onBackPressed() {
        fabGetDirection.setVisibility(View.VISIBLE);
        fabStartNavigation.setVisibility(View.VISIBLE);
        cardView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCameraMoveStarted(int i) {
        if (i == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            re_center = false;
        }
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {

        if (direction.isOK()) {
            Toast.makeText(getApplicationContext(), "Route success", Toast.LENGTH_SHORT).show();
            //drawNavigateDirection(direction.getRouteList());
            progressDialog.dismiss();
            for (Route route : direction.getRouteList()) {
                List<Leg> leg = route.getLegList();
                //drawNavigateDirection(route, 1);
                Log.d(TAG, "onDirectionSuccess: " + route.getLegList());
                Log.d(TAG, "onDirectionSuccess: start" + start);
                Log.d(TAG, "onDirectionSuccess: end  " + end);

            }
            Route route = direction.getRouteList().get(0);
            List<Leg> legs = route.getLegList();
            for (Leg leg : route.getLegList()) {
                drawNavigateDirection(leg.getStepList().get(3).getPolyline().getPointList(), 0);
            }
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {

    }

    void initInstructions() {
        instructionsList = new ArrayList<>();
        instructionsList.add(new Instructions("300m", "Nguyen Ngoc loc"));
        instructionsList.add(new Instructions("500m", "3 Thang 2"));
        instructionsList.add(new Instructions("700m", "Ly Thai To"));
    }
}
