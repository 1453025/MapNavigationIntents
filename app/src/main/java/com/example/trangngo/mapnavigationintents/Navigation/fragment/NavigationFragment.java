package com.example.trangngo.mapnavigationintents.Navigation.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.akexorcist.googledirection.model.Step;
import com.example.trangngo.mapnavigationintents.Navigation.Presenter.Presenter;
import com.example.trangngo.mapnavigationintents.Navigation.Presenter.PresenterToViewCallback;
import com.example.trangngo.mapnavigationintents.Navigation.adapter.InstructionsAdapter;
import com.example.trangngo.mapnavigationintents.Navigation.animatedmarker.LatLngInterpolator;
import com.example.trangngo.mapnavigationintents.Navigation.animatedmarker.MarkerAnimation;
import com.example.trangngo.mapnavigationintents.Navigation.fragment.listenerimplement.ListenerImplement;
import com.example.trangngo.mapnavigationintents.Navigation.fragment.listenerimplement.MyLocationListener;
import com.example.trangngo.mapnavigationintents.Navigation.model.Instructions;
import com.example.trangngo.mapnavigationintents.Navigation.utils.Key;
import com.example.trangngo.mapnavigationintents.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.ButtCap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by trangngo on 7/17/17.
 */

public class NavigationFragment extends Fragment implements OnMapReadyCallback,
        PresenterToViewCallback.OnRoute, PresenterToViewCallback {
    private static final int[] COLORS = new int[]{
            R.color.primary_dark,
            R.color.primary,
            R.color.primary_light,
            R.color.accent};
    private static String TAG = "Navigation Fragment";
    Presenter presenter;
    ListenerImplement listenerImplement;
    GoogleMap mMap;
    IconGenerator iconGenerator;
    MyLocationListener myLocationListener;
    LatLngInterpolator latLngInterpolator;
    int index = 0;
    private Marker myLocationMarker;
    private HashMap<Integer, Marker> arrowMarkerDirection;
    private HashMap<Integer, Marker> nameMarkerStreet;
    private HashMap<Integer, Marker> timeMarker;
    private ProgressDialog progressDialog;
    private LatLngBounds homePDD = new LatLngBounds(
            new LatLng(10.7651909, 106.6619211),
            new LatLng(10.7773018, 106.6999617));

    private FloatingActionButton fabRecenter;
    private ViewPager vpInstructions;
    private InstructionsAdapter insAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        arrowMarkerDirection = new HashMap<>();
        nameMarkerStreet = new HashMap<>();
        timeMarker = new HashMap<>();

        iconGenerator = new IconGenerator(getActivity());
        latLngInterpolator = new LatLngInterpolator.Spherical();

        presenter = new Presenter(this, this, getActivity());

        if (myLocationListener != null) {
            presenter.setMyLocationListener(myLocationListener);
        }
        listenerImplement = new ListenerImplement(presenter);
        LatLng fromPosition = getArguments().getParcelable(Key.FROM_POSITION);
        //LatLng toPosition = getArguments().getParcelable(Key.TO_POSITION);
        LatLng toPosition = new LatLng(10.7773018, 106.6999617);
        presenter.setFromToPosition(fromPosition, toPosition);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);

        fabRecenter = (FloatingActionButton) view.findViewById(R.id.fab_recenter);
        vpInstructions = (ViewPager) view.findViewById(R.id.vpInstructions);

        MapFragment mapFragment = (MapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(200, 500, 200, 200);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homePDD.getCenter(), 15));
        presenter.route();

        vpInstructions.addOnPageChangeListener(listenerImplement);
        fabRecenter.setOnClickListener(listenerImplement);
        mMap.setOnCameraMoveStartedListener(listenerImplement);
        mMap.setOnCameraMoveListener(listenerImplement);
    }

    @Override
    public void onInputFail() {

    }

    @Override
    public void onStartRoute() {
        progressDialog = ProgressDialog.show(getActivity(), "Please wait.",
                "Fetching route information.", true);
    }

    @Override
    public void onEndRoute(boolean success) {
        progressDialog.dismiss();
    }

    @Override
    public void drawNavigateDirection(List<LatLng> pointList, int i) {
        int colorIndex = i % COLORS.length;
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(getResources().getColor(COLORS[colorIndex]));
        polylineOptions.width(25);
        polylineOptions.addAll(pointList);
        Polyline polyline = mMap.addPolyline(polylineOptions);
        List<PatternItem> pattern = Arrays.asList(
                new Dot(), new Gap(20), new Dash(30), new Gap(20));
        //polyline.setPattern(pattern);
//        polyline.setStartCap(new CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_down),
//                20));
        polyline.setEndCap(new ButtCap());
        polyline.setJointType(JointType.ROUND);
    }

    @Override
    public void setAdapterViewInstructions(List<Instructions> intructionsList) {

        insAdapter = new InstructionsAdapter(getActivity(), intructionsList);
        vpInstructions.setAdapter(insAdapter);

    }

    @Override
    public void moveMarkerFollowMyLocation(Location location) {

        if (location == null) {
            return;
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (myLocationMarker == null) {
            myLocationMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .flat(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car_black))
                    .anchor(0.5f, 0.5f));
            myLocationMarker.setZIndex(99);
        }
        myLocationMarker.setRotation(location.getBearing());
        MarkerAnimation.animateMarkerToGB(myLocationMarker, latLng, latLngInterpolator);
    }

    @Override
    public void moveCameraFollowMyLocation(Location myLocation) {
        if (myLocation == null)
            return;
        LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        updateCameraBearing(mMap, latLng, myLocation.getBearing());
    }

    @Override
    public void setViewPagerFollowMyLocation(int i) {
        index = i;
        if (vpInstructions.getCurrentItem() != i) {
            vpInstructions.setCurrentItem(i);

        }
    }

    @Override
    public void changeColorViewPager(int position) {
        if (this.index == position) {
            vpInstructions.setBackgroundColor(getResources().getColor(R.color.primary));
        } else {
            vpInstructions.setBackgroundColor(getResources().getColor(R.color.secondary_text));
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void notifySetChangeAdapter(int data, int index) {
        TextView view = (TextView) vpInstructions.findViewWithTag(Key.DISTANCETAG + index);
        if (view == null) {
            return;
        }
        view.setText(String.valueOf(data) + "m");
    }


    @Override
    public void moveCameraFollowStep(List<Step> stepList, int position) {
        if (position >= 0) {
            Step step = stepList.get(position);
            Double heading = SphericalUtil.computeHeading(step.getStartLocation().getCoordination()
                    , step.getEndLocation().getCoordination());
            updateCameraBearing(mMap, step.getStartLocation().getCoordination(), heading.floatValue());
        }
    }

    @Override
    public void addMarkerVisibleToMap(List<MarkerOptions> arrowMarkerDirectionList,
                                      List<MarkerOptions> nameMarkerStreetList,
                                      List<MarkerOptions> timeMarkerList) {
        //This is the current user-viewable region of the map
        LatLngBounds bounds = this.mMap.getProjection().getVisibleRegion().latLngBounds;


        //addMarkerFollowBounds(bounds, arrowMarkerDirection, arrowMarkerDirectionList);
        addMarkerFollowBounds(bounds, nameMarkerStreet, nameMarkerStreetList);
        addMarkerFollowBounds(bounds, timeMarker, timeMarkerList);

    }

    void addMarkerFollowBounds(LatLngBounds bounds
            , HashMap<Integer, Marker> hmMarker, List<MarkerOptions> markerOptionsList) {
        for (int i = 0; i < markerOptionsList.size(); i++) {
            //If the item is within the the bounds of the screen
            if (bounds.contains(markerOptionsList.get(i).getPosition())) {
                //If the item isn't already being displayed
                if (!hmMarker.containsKey(i)) {
                    Marker marker = mMap.addMarker(markerOptionsList.get(i));
                    marker.setZIndex(1);
                    hmMarker.put(i, marker);
                }
            }
            //If the marker is off screen
            else {
                //If the course was previously on screen
                if (hmMarker.containsKey(i)) {
                    //1. Remove the Marker from the GoogleMap
                    hmMarker.get(i).remove();
                    //2. Remove the reference to the Marker from the HashMap
                    hmMarker.remove(i);
                }
            }
        }
    }

    public void setListenLocation(MyLocationListener listenLocation) {
        this.myLocationListener = listenLocation;
    }

    private void updateCameraBearing(GoogleMap googleMap, LatLng latLng, float bearing) {
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
}
