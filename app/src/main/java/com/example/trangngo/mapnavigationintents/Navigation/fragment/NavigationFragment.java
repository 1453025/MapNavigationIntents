package com.example.trangngo.mapnavigationintents.Navigation.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akexorcist.googledirection.model.Step;
import com.example.trangngo.mapnavigationintents.Navigation.Presenter.Presenter;
import com.example.trangngo.mapnavigationintents.Navigation.Presenter.PresenterToViewCallback;
import com.example.trangngo.mapnavigationintents.Navigation.adapter.InstructionsAdapter;
import com.example.trangngo.mapnavigationintents.Navigation.fragment.listenerimplement.ListenerImplement;
import com.example.trangngo.mapnavigationintents.Navigation.model.Instructions;
import com.example.trangngo.mapnavigationintents.Navigation.utils.Key;
import com.example.trangngo.mapnavigationintents.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by trangngo on 7/17/17.
 */

public class NavigationFragment extends Fragment implements OnMapReadyCallback,
        PresenterToViewCallback.OnRoute, PresenterToViewCallback {
    private static final int[] COLORS = new int[]{
            R.color.primary_dark,
            R.color.primary,
            R.color.primary_light,
            R.color.accent,
            R.color.primary_dark_material_light};
    private static final NavigationFragment ourInstance = new NavigationFragment();
    private static String TAG = "Navigation Fragment";
    Presenter presenter;
    ListenerImplement listenerImplement;
    GoogleMap mMap;
    IconGenerator iconGenerator;
    private List<Polyline> polylineList;
    private HashMap<Integer, Marker> arrowMarkerDirection;
    private HashMap<Integer, Marker> nameMarkerStreet;
    private HashMap<Integer, Marker> timeMarker;

    private ProgressDialog progressDialog;
    private LatLngBounds homePDD = new LatLngBounds(
            new LatLng(10.7651909, 106.6619211),
            new LatLng(10.7773018, 106.6999617));
    private FloatingActionButton fabRecenter;
    private ViewPager vpInstructions;

    public static NavigationFragment getInstance() {
        return ourInstance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        polylineList = new ArrayList<>();
        arrowMarkerDirection = new HashMap<>();
        nameMarkerStreet = new HashMap<>();
        timeMarker = new HashMap<>();

        iconGenerator = new IconGenerator(getActivity());

        presenter = new Presenter(this, this, getActivity());
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
        if (success) {

        } else {

        }
    }

    @Override
    public void drawNavigateDirection(List<LatLng> pointList, int i) {
        int colorIndex = i % COLORS.length;
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(getResources().getColor(COLORS[colorIndex]));
        polylineOptions.width(10 + i * 3);
        polylineOptions.addAll(pointList);
        Polyline polyline = mMap.addPolyline(polylineOptions);
        polylineList.add(polyline);
    }

    @Override
    public void setAdapterViewInstructions(List<Instructions> intructionsList) {
        InstructionsAdapter insAdapter = new InstructionsAdapter(getActivity(), intructionsList);
        vpInstructions.setAdapter(insAdapter);

    }


    @Override
    public void changeCameraFollowStep(List<Step> stepList, int index) {
        //addIcon(iconFactory, "Manh", stepList.get(position).getStartLocation().getCoordination());
        if (index > 0) {
            Step step = stepList.get(index - 1);
            //addIcon(iconFactory, "Manh2", stepList.get(position - 1).getStartLocation().getCoordination());
            Double heading = SphericalUtil.computeHeading(step.getStartLocation().getCoordination()
                    , step.getEndLocation().getCoordination());
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

    @Override
    public void addMarkerVisibleToMap(List<MarkerOptions> arrowMarkerDirectionList,
                                      List<MarkerOptions> nameMarkerStreetList,
                                      List<MarkerOptions> timeMarkerList) {
        //This is the current user-viewable region of the map
        LatLngBounds bounds = this.mMap.getProjection().getVisibleRegion().latLngBounds;


        addMarkerFollowBounds(bounds, arrowMarkerDirection, arrowMarkerDirectionList);
        addMarkerNameFollowBounds(bounds, nameMarkerStreet, nameMarkerStreetList);
        addMarkerFollowBounds(bounds, timeMarker, timeMarkerList);

    }

    private void addMarkerNameFollowBounds(LatLngBounds bounds
            , HashMap<Integer, Marker> nameMarkerStreet, List<MarkerOptions> nameMarkerStreetList) {

        for (int i = 0; i < nameMarkerStreetList.size(); i++) {
            //If the item is within the the bounds of the screen
            if (bounds.contains(nameMarkerStreetList.get(i).getPosition())) {
                //If the item isn't already being displayed
                if (!nameMarkerStreet.containsKey(i)) {
                    //Add the Marker to the Map and keep track of it with the HashMap
                    //getMarkerForItem just returns a MarkerOptions object
                    getNameAddressFromLatLng(getActivity()
                            , nameMarkerStreetList.get(i).getPosition().latitude
                            , nameMarkerStreetList.get(i).getPosition().longitude
                            , nameMarkerStreet, nameMarkerStreetList, i);
                }
            }
            //If the marker is off screen
            else {
                //If the course was previously on screen
                if (nameMarkerStreet.containsKey(i)) {
                    //1. Remove the Marker from the GoogleMap
                    nameMarkerStreet.get(i).remove();
                    //2. Remove the reference to the Marker from the HashMap
                    nameMarkerStreet.remove(i);
                }
            }
        }

    }

    void addMarkerFollowBounds(LatLngBounds bounds
            , HashMap<Integer, Marker> hmMarker, List<MarkerOptions> markerOptionsList) {

        for (int i = 0; i < markerOptionsList.size(); i++) {
            //If the item is within the the bounds of the screen
            if (bounds.contains(markerOptionsList.get(i).getPosition())) {
                //If the item isn't already being displayed
                if (!hmMarker.containsKey(i)) {
                    //Add the Marker to the Map and keep track of it with the HashMap
                    //getMarkerForItem just returns a MarkerOptions object
                    hmMarker.put(i, mMap.addMarker(markerOptionsList.get(i)));
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

    public void getNameAddressFromLatLng(final Context context, final double lat, final double lng
            , final HashMap<Integer, Marker> nameMarkerStreet, final List<MarkerOptions> nameMarkerStreetList, final int i) {
        LatLng latlng = new LatLng(lat, lng);
        final Geocoder geocoder;
        geocoder = new Geocoder(context, Locale.getDefault());
        AsyncTask<LatLng, Void, String> task = new AsyncTask<LatLng, Void, String>() {
            @Override
            protected void onPreExecute() {
                // Utils.showProgressDialog(context,"");
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(LatLng... latLngs) {
                try {
                    List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                    //DebugLog.loge("name"+addresses.get(0).getAddressLine(0).toString());
                    if (!addresses.isEmpty()) {
                        try {
                            return addresses.get(0).getAddressLine(0).toString();
                        } catch (Exception e) {
                            return "Unknow";
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return "Unknow";
            }

            @Override
            protected void onPostExecute(String s) {
                nameMarkerStreet.put(i, mMap.addMarker(nameMarkerStreetList.get(i)
                        .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(s)))));
            }
        };
        task.execute(latlng);
    }

}
