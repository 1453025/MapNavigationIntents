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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.Language;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.example.trangngo.mapnavigationintents.adapter.InstructionsAdapter;
import com.example.trangngo.mapnavigationintents.animatedmarker.LatLngInterpolator;
import com.example.trangngo.mapnavigationintents.animatedmarker.MarkerAnimation;
import com.example.trangngo.mapnavigationintents.model.Instructions;
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
import com.google.android.gms.maps.model.*;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, GoogleMap.OnCameraMoveStartedListener, DirectionCallback, GoogleMap.OnCameraMoveListener {

    private static final int[] COLORS = new int[]{R.color.primary_dark, R.color.primary, R.color.primary_light, R.color.accent, R.color.primary_dark_material_light};
    private static String TAG = "MapsActivity";
    protected LatLng start;
    protected LatLng end;
    List<LatLng> latLngs = new ArrayList<>();
    boolean re_center = false;
    boolean isGetMyLocation = false;
    LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Spherical();
    IconGenerator iconFactory;
    List<MarkerOptions> markerOptionsList = new ArrayList<>();
    private LatLngBounds homePDD = new LatLngBounds(new LatLng(10.7651909, 106.6619211), new LatLng(10.7773018, 106.6999617));
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private ProgressDialog progressDialog;
    private List<Polyline> polylineList;
    private LatLng myLatLng;
    private Location currentLcation;
    private Location newLocation;
    private Marker marker;
    private ArrayList<Marker> markerList;
    private List<LatLng> latLngList;
    private List<Step> stepList;
    private ArrayList<Route> routes;
    private LatLng myLatLngLocation;
    private RelativeLayout relativeLayoutOnMap;
    private RelativeLayout relativeLayoutOnNavigation;
    private FloatingActionButton fabGetDirection;
    private FloatingActionButton fabStartNavigation;
    private PlaceAutocompleteFragment autocompleteFragment;
    private FloatingActionButton fabRecenter;
    private ViewPager vpInstructions;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double latitude = Double.valueOf(intent.getStringExtra("latutide"));
            double longitude = Double.valueOf(intent.getStringExtra("longitude"));
            currentLcation = intent.getParcelableExtra("location");
            myLatLng = new LatLng(latitude, longitude);

            boolean isOnPath = false;
            MarkerAnimation.animateMarkerToGB(marker, myLatLng, latLngInterpolator);
            if (re_center) {
                updateCameraBearing(mMap, myLatLng, currentLcation.getBearing());
                for (int i = 0; i < polylineList.size(); i++) {
                    if (PolyUtil.isLocationOnPath(myLatLng, polylineList.get(i).getPoints(), false, 10)) {
                        if (vpInstructions.getCurrentItem() != i)
                            vpInstructions.setCurrentItem(i);
                        break;
                    }
                }
            }

            for (int i = 0; i < polylineList.size(); i++) {
                if (PolyUtil.isLocationOnPath(myLatLng, polylineList.get(i).getPoints(), false, 50)) {
                    //if(vpInstructions.getCurrentItem() != )
                    //vpInstructions.setCurrentItem(i);
                    isOnPath = true;
                    break;
                }
            }

            if (!isOnPath) {
                Log.d(TAG, "false");
            }


        }
    };
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

        // register layout and view OnNavtigation
        relativeLayoutOnNavigation = (RelativeLayout) findViewById(R.id.relativeLayoutOnNavigation);
        fabRecenter = (FloatingActionButton) findViewById(R.id.fab_recenter);
        vpInstructions = (ViewPager) findViewById(R.id.vpInstructions);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        relativeLayoutOnNavigation.setVisibility(View.GONE);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                Toast.makeText(MapsActivity.this, "End: " + place.getName(), Toast.LENGTH_SHORT).show();
                end = place.getLatLng();

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        fabGetDirection.setOnClickListener(this);
        fabStartNavigation.setOnClickListener(this);
        fabRecenter.setOnClickListener(this);
        vpInstructions.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Toast.makeText(MapsActivity.this, "Position: " + position, Toast.LENGTH_SHORT).show();
                changeCameraPreview(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void route(LatLng from, LatLng to) {

        Log.d(TAG, "route: ");

        if (start == null) {
            Toast.makeText(getApplicationContext(), "Can't get LatLng from start place!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (end == null) {
            Toast.makeText(getApplicationContext(), "Can't get LatLng from end place!", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Fetching route information.", true);

        GoogleDirection.withServerKey("AIzaSyA8FkLNAIyrX6xTkytf05cbKsnaOeOglso")
                .from(from)
                .to(to)
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

                        start = place.getPlace().getLatLng();
                        route(start, end);
                    }
                });
            }
            break;
            case R.id.fab_start_navigation: {
                startNavigation();
            }
            break;
            case R.id.fab_recenter:
                re_center = true;
                break;
            default:
                break;
        }
    }

    private void startNavigation() {
        List<Instructions> intructionsList = getInstructionsFromSteps(stepList);
        hideAllView();
        showNavigationView();
        setAdapterViewInstructions(intructionsList);
        updateCameraBearing(mMap, myLatLng, currentLcation.getBearing());
        // hide blue dot
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(false);
        }

        drawArrowMarkerOnDirection(mMap, markerList);
    }

    private void drawArrowMarkerOnDirection(GoogleMap googleMap, List<Marker> markers) {

        LatLng tailLatLng;
        LatLng headLatLng;
        Double heading;
        MarkerOptions markerOptions;
        for (Step step : stepList) {
            int size = step.getPolyline().getPointList().size();
            if (size > 1) {
                tailLatLng = step.getPolyline().getPointList().get(0);
                headLatLng = step.getPolyline().getPointList().get(1);
                heading = SphericalUtil.computeHeading(tailLatLng, headLatLng);
                markerOptions = new MarkerOptions()
                        .position(tailLatLng)
                        .flat(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_polyline))
                        .anchor(0.5f, 0.5f)
                        .rotation(heading.floatValue() - 90);
                markerOptionsList.add(markerOptions);
                if (size > 3) {
                    if (Integer.parseInt(step.getDistance().getValue()) > 100) {
                        int midle = size / 2;
                        if (midle > 1) {
                            tailLatLng = step.getPolyline().getPointList().get(midle);
                            headLatLng = step.getPolyline().getPointList().get(midle + 1);
                            heading = SphericalUtil.computeHeading(tailLatLng, headLatLng);
                            markerOptions = new MarkerOptions()
                                    .position(tailLatLng)
                                    .flat(true)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_polyline))
                                    .anchor(0.5f, 0.5f)
                                    .rotation(heading.floatValue() - 90);
                            markerOptionsList.add(markerOptions);
                        }
                    }
                }
            }
        }
    }

    private void setAdapterViewInstructions(List<Instructions> intructionsList) {
        InstructionsAdapter instructionsAdapter = new InstructionsAdapter(this, intructionsList);
        vpInstructions.setAdapter(instructionsAdapter);

    }

    private void showNavigationView() {
        relativeLayoutOnNavigation.setVisibility(View.VISIBLE);
    }

    private void hideAllView() {
        relativeLayoutOnMap.setVisibility(View.GONE);
    }

    private List<Instructions> getInstructionsFromSteps(List<Step> stepList) {
        List<Instructions> instructionsList = new ArrayList<>();
        for (Step step : stepList) {
            Instructions instructions = new Instructions.InstructionsBuilder()
                    .setInstructions(step.getHtmlInstruction())
                    .setDistance(step.getDistance().getText())
                    .setManeuver(step.getManeuver())
                    .setEndLatLng(step.getEndLocation().getCoordination())
                    .setStartLatLng(step.getStartLocation().getCoordination())
                    .build();
            instructionsList.add(instructions);
        }
        return instructionsList;
    }

    void drawNavigateDirection(List<Route> routes) {

        if (polylineList.size() > 0) {
            for (Polyline poly : polylineList) {
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
            polylineList.add(polyline);
        }
    }

    void drawNavigateDirection(Route routes, int index) {
        int colorIndex = index % COLORS.length;
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(getResources().getColor(COLORS[colorIndex]));
        polylineOptions.width(10 + index * 3);
        polylineOptions.addAll(routes.getOverviewPolyline().getPointList());
        Polyline polyline = mMap.addPolyline(polylineOptions);
        polylineList.add(polyline);

        //Toast.makeText(getApplicationContext(),"Route "+ (index+1) +": distance - "+ routes.get(index).getDistanceValue()+": duration - "+ routes.get(index).getDurationValue(),Toast.LENGTH_SHORT).show();

    }

    void drawNavigateDirection(List<LatLng> latLngs, int index) {
        int colorIndex = index % COLORS.length;
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(getResources().getColor(COLORS[colorIndex]));
        polylineOptions.width(10 + index * 3);
        polylineOptions.addAll(latLngs);
        Polyline polyline = mMap.addPolyline(polylineOptions);
        polylineList.add(polyline);

        //Toast.makeText(getApplicationContext(),"Route "+ (index+1) +": distance - "+ routes.get(index).getDistanceValue()+": duration - "+ routes.get(index).getDurationValue(),Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {
        relativeLayoutOnNavigation.setVisibility(View.GONE);
        relativeLayoutOnMap.setVisibility(View.VISIBLE);

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
            }
            Route route = direction.getRouteList().get(0);
            List<Leg> legs = route.getLegList();
            for (Leg leg : route.getLegList()) {
                stepList = leg.getStepList();
                for (int i = 0; i < leg.getStepList().size(); i++) {
                    drawNavigateDirection(leg.getStepList().get(i).getPolyline().getPointList(), 2);
                }
            }
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {

    }

    private void changeCameraPreview(int position) {
        addIcon(iconFactory, "Manh", stepList.get(position).getStartLocation().getCoordination());
        if (position > 0) {
            Step step = stepList.get(position - 1);
            addIcon(iconFactory, "Manh2", stepList.get(position - 1).getStartLocation().getCoordination());
            Double heading = SphericalUtil.computeHeading(step.getStartLocation().getCoordination(), step.getEndLocation().getCoordination());
            updateCameraBearing(mMap, step.getEndLocation().getCoordination(), heading.floatValue());
        }
    }

    private void updateCameraBearing(GoogleMap googleMap, LatLng latLng, float bearing) {
        if (googleMap == null) return;
        CameraPosition camPos = CameraPosition
                .builder(
                        googleMap.getCameraPosition() // current Camera
                )
                .target(latLng)
                .bearing(bearing)
                .tilt(45)
                .zoom(18)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }

    private void addIcon(IconGenerator iconFactory, CharSequence text, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text))).
                position(position).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
        mMap.addMarker(markerOptions);
    }

    @Override
    public void onCameraMove() {
        showVisibleMarker();
    }

    private void showVisibleMarker() {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        //new ShowMarker().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bounds);
        addItemsToMap(markerOptionsList);
    }

    //Your "Item" class will need at least a unique id, latitude and longitude.
    private void addItemsToMap(List<MarkerOptions> markerOptionsList) {
        if (this.mMap != null) {
            //This is the current user-viewable region of the map
            LatLngBounds bounds = this.mMap.getProjection().getVisibleRegion().latLngBounds;

            //Loop through all the items that are available to be placed on the map
            for (int i = 0; i < markerOptionsList.size(); i++) {
                //If the item is within the the bounds of the screen
                if (bounds.contains(markerOptionsList.get(i).getPosition())) {
                    //If the item isn't already being displayed
                    if (!visibleMarkers.containsKey(i)) {
                        //Add the Marker to the Map and keep track of it with the HashMap
                        //getMarkerForItem just returns a MarkerOptions object
                        this.visibleMarkers.put(i, this.mMap.addMarker(markerOptionsList.get(i)));
                    }
                }
                //If the marker is off screen
                else {
                    //If the course was previously on screen
                    if (visibleMarkers.containsKey(i)) {
                        //1. Remove the Marker from the GoogleMap
                        visibleMarkers.get(i).remove();
                        //2. Remove the reference to the Marker from the HashMap
                        visibleMarkers.remove(i);
                    }
                }
            }
        }
    }


    class ShowMarker extends AsyncTask<LatLngBounds, Integer, Void> {

        @Override
        protected void onPreExecute() {
            for (Marker marker : markerList) {
                marker.remove();
            }
            markerList.clear();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            markerList.add(mMap.addMarker(markerOptionsList.get(values[0])));
        }

        @Override
        protected Void doInBackground(LatLngBounds... latLngBounds) {

            for (int i = 0; i < markerOptionsList.size(); i++) {
                //If the item is within the the bounds of the screen
                if (latLngBounds[0].contains(markerOptionsList.get(i).getPosition())) {
                    //If the item isn't already being displayed
                    publishProgress(i);

                }
                //If the marker is off screen
            }


            return null;
        }
    }

}

