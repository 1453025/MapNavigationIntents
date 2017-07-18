package com.example.trangngo.mapnavigationintents;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.example.trangngo.mapnavigationintents.Navigation.animatedmarker.LatLngInterpolator;
import com.example.trangngo.mapnavigationintents.Navigation.fragment.NavigationFragment;
import com.example.trangngo.mapnavigationintents.Navigation.fragment.listenerimplement.MyLocationListener;
import com.example.trangngo.mapnavigationintents.Navigation.utils.Key;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraMoveListener {

    private static final int[] COLORS = new int[]{R.color.primary_dark, R.color.primary, R.color.primary_light, R.color.accent, R.color.primary_dark_material_light};
    private final static String TAG_FRAGMENT = "TAG_FRAGMENT";
    private static String TAG = "MapsActivity";
    protected LatLng fromPosition;
    protected LatLng toPosition;
    List<LatLng> latLngs = new ArrayList<>();
    boolean re_center = false;
    boolean isGetMyLocation = false;
    LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Spherical();
    IconGenerator iconFactory;
    List<MarkerOptions> markerOptionsList = new ArrayList<>();

    MyLocationListener myLocationListener = new MyLocationListener();
    private LatLngBounds homePDD = new LatLngBounds(new LatLng(10.7651909, 106.6619211), new LatLng(10.7773018, 106.6999617));
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private ProgressDialog progressDialog;
    private List<Polyline> polylineList;
    private LatLng myLatLng;
    private Location currentLcation;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double latitude = Double.valueOf(intent.getStringExtra("latutide"));
            double longitude = Double.valueOf(intent.getStringExtra("longitude"));
            currentLcation = intent.getParcelableExtra("location");
            myLatLng = new LatLng(latitude, longitude);

            myLocationListener.onLocationChange(currentLcation);

        }
    };
    private Location newLocation;
    private Marker marker;
    private ArrayList<Marker> markerList;
    private List<LatLng> latLngList;
    private List<Step> stepList;
    private ArrayList<Route> routes;
    private LatLng myLatLngLocation;
    private RelativeLayout relativeLayoutOnMap;
    //private RelativeLayout relativeLayoutOnNavigation;
    private FloatingActionButton fabGetDirection;
    private FloatingActionButton fabStartNavigation;
    private PlaceAutocompleteFragment autocompleteFragment;
    private HashMap<Integer, Marker> visibleMarkers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myLatLngLocation = new LatLng(10.7773018, 06.6993529);
        newLocation = new Location("");
        polylineList = new ArrayList<>();
        routes = new ArrayList<>();
        markerList = new ArrayList<>();
        latLngList = new ArrayList<>();
        iconFactory = new IconGenerator(this);


        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        // register layout and view OnMap
        relativeLayoutOnMap = (RelativeLayout) findViewById(R.id.relativeLayoutOnMap);
        fabGetDirection = (FloatingActionButton) findViewById(R.id.fab_get_direction);
        fabStartNavigation = (FloatingActionButton) findViewById(R.id.fab_start_navigation);
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                Toast.makeText(MapsActivity.this, "End: " + place.getName(), Toast.LENGTH_SHORT).show();
                toPosition = place.getLatLng();

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        fabGetDirection.setOnClickListener(this);
        fabStartNavigation.setOnClickListener(this);
//        fabRecenter.setOnClickListener(this);

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
        mMap.setOnCameraMoveListener(this);

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
                    Toast.makeText(MapsActivity.this, "Permission denied to get location", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
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
                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                        .getCurrentPlace(mGoogleApiClient, null);
                result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                    @Override
                    public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                        if (!likelyPlaces.getStatus().isSuccess()) {
                            // Request did not complete successfully
                            Log.e(TAG, "Place query did not complete. Error: " +
                                    likelyPlaces.getStatus().toString());

                            likelyPlaces.release();
                            return;
                        }
                        // Get the Place object from the buffer.
                        final PlaceLikelihood place = likelyPlaces.get(0);
                        Toast.makeText(MapsActivity.this, "Start: " + place.getPlace().toString(),
                                Toast.LENGTH_SHORT).show();
                        fromPosition = place.getPlace().getLatLng();

                        Bundle bundle = new Bundle();
                        bundle.putParcelable(Key.FROM_POSITION, fromPosition);
                        bundle.putParcelable(Key.TO_POSITION, toPosition);

                        NavigationFragment navigationFragment = new NavigationFragment();
                        navigationFragment.setArguments(bundle);
                        navigationFragment.setListenLocation(myLocationListener);
                        getFragmentManager().beginTransaction()
                                .add(R.id.fragment_navigation, navigationFragment, TAG_FRAGMENT)
                                .addToBackStack(null)
                                .commit();
                    }
                });
            }
            break;
            case R.id.fab_start_navigation: {
            }
            break;
            case R.id.fab_recenter:
                re_center = true;
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {

        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public void onCameraMoveStarted(int i) {
        if (i == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            re_center = false;
        }
    }

    @Override
    public void onCameraMove() {
        showVisibleMarker();
    }

    private void showVisibleMarker() {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        //new ShowMarker().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bounds);
    }

    //Your "Item" class will need at least a unique id, latitude and longitude.

}

