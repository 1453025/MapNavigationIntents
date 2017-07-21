package com.example.trangngo.mapnavigationintents.Navigation.Presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.Language;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.example.trangngo.mapnavigationintents.Navigation.fragment.listenerimplement.MyLocationListener;
import com.example.trangngo.mapnavigationintents.Navigation.model.Instructions;
import com.example.trangngo.mapnavigationintents.Navigation.utils.MyUtils;
import com.example.trangngo.mapnavigationintents.R;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by trangngo on 7/13/17.
 */

public class Presenter implements DirectionCallback {
    private String TAG = "Presenter";
    private IconGenerator iconFactory;

    private LatLng fromPosition;
    private LatLng toPosition;
    private Location myLocation;
    private boolean routeSuccess = false;

    private PresenterToViewCallback.OnRoute onRoute;
    private PresenterToViewCallback presenterCb;
    private MyLocationListener myLocationListener;
    private Context context;

    private List<Step> stepList;
    private List<PolylineOptions> polylineOptionsList;
    private List<Instructions> intructionsList;
    private List<MarkerOptions> arrowMarkerDirectionList;
    private List<MarkerOptions> nameMarkerStreetList;
    private List<MarkerOptions> timeMarkerList;
    private List<MarkerOptions> instructionMarkerList; // put in tail of the polyline


    private boolean reCenter = true;

    public Presenter(PresenterToViewCallback.OnRoute onRoute, PresenterToViewCallback presenterCb,
                     Context context) {
        this.onRoute = onRoute;
        this.presenterCb = presenterCb;
        this.context = context;
    }

    public void setFromToPosition(LatLng fromPosition, LatLng toPosition) {
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
    }

    private void init() {
        stepList = new ArrayList<>();
        polylineOptionsList = new ArrayList<>();
        arrowMarkerDirectionList = new ArrayList<>();
        nameMarkerStreetList = new ArrayList<>();
        timeMarkerList = new ArrayList<>();
        instructionMarkerList = new ArrayList<>();
        iconFactory = new IconGenerator(context);
    }

    public void route() {

        Log.d(TAG, "route: ");


        if (fromPosition == null || toPosition == null) {
            onRoute.onInputFail();
            return;
        }

        onRoute.onStartRoute();


        GoogleDirection.withServerKey("AIzaSyA8FkLNAIyrX6xTkytf05cbKsnaOeOglso")
                .from(fromPosition)
                .to(toPosition)
                .language(Language.VIETNAMESE)
                .alternativeRoute(true)
                .execute(this);
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {

        if (direction.isOK()) {
            routeSuccess = true;
            onRoute.onEndRoute(true);

            init();
            Route route = direction.getRouteList().get(0);
            for (Leg leg : route.getLegList()) {
                stepList = leg.getStepList();
                intructionsList = MyUtils.getInstructionsFromSteps(stepList);
                presenterCb.setAdapterViewInstructions(intructionsList);
                makeArrowMakerDirection();
                for (int i = 0; i < leg.getStepList().size(); i++) {
                    presenterCb.drawNavigateDirection(
                            leg.getStepList().get(i).getPolyline().getPointList(), 0);
                    polylineOptionsList.add(new PolylineOptions().addAll(
                            leg.getStepList().get(i).getPolyline().getPointList()));

                }
            }
        } else {
            onRoute.onEndRoute(false);
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {

    }

    public void changeCameraFollowStep(int position) {
        if (routeSuccess) {
            presenterCb.moveCameraFollowStep(stepList, position);
        }
    }

    public void addMarkerVisibleToMap() {
        if (routeSuccess) {
            presenterCb.addMarkerVisibleToMap(
                    arrowMarkerDirectionList, nameMarkerStreetList, instructionMarkerList);
        }
    }

    public void onRecenter() {
        reCenter = true;
        presenterCb.moveCameraFollowMyLocation(myLocation);
    }

    public void onLocationChange(Location location) {
        if (location == null) {
            return;
        }
        this.myLocation = location;
        presenterCb.moveMarkerFollowMyLocation(myLocation);

        LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

        int position = myLocationIsOnPolyline(latLng);

        updateDistanceASinglePolyline(position);
        if (reCenter) {
            presenterCb.moveCameraFollowMyLocation(myLocation);

            if (position > -1) {
                presenterCb.setViewPagerFollowMyLocation(position);
            }
        }

    }

    public void setMyLocationListener(MyLocationListener myLocationListener) {
        this.myLocationListener = myLocationListener;
        this.myLocationListener.setPresenter(this);
    }

    public void setRecenterFalse() {
        this.reCenter = false;
    }

    public void changeColorViewPager(int position) {
        presenterCb.changeColorViewPager(position);
    }

    private int myLocationIsOnPolyline(LatLng latLng) {
        if (polylineOptionsList == null) {
            return -1;
        }
        for (int i = 0; i < polylineOptionsList.size(); i++) {
            if (PolyUtil.isLocationOnPath(latLng, polylineOptionsList.get(i).getPoints(), false, 10)) {
                return i;
            }
        }
        return -1;
    }

    //get marker to show on polyline
    private void makeArrowMakerDirection() {

        LatLng tailLatLng;
        LatLng headLatLng;
        Double heading;
        MarkerOptions markerOptions;
        for (Step step : stepList) {
            int size = step.getPolyline().getPointList().size();
            tailLatLng = step.getPolyline().getPointList().get(0);
            instructionMarkerList.add(new MarkerOptions()
                    .position(tailLatLng)
                    .icon(BitmapDescriptorFactory
                            .fromBitmap(iconFactory.makeIcon(step.getManeuver())))
                    .anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV()));

            if (size > 1) {
                tailLatLng = step.getPolyline().getPointList().get(0);
                headLatLng = step.getPolyline().getPointList().get(1);
                heading = SphericalUtil.computeHeading(tailLatLng, headLatLng);
                markerOptions = new MarkerOptions()
                        .position(headLatLng)
                        .flat(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_polyline))
                        .anchor(0.5f, 0.5f)
                        .rotation(heading.floatValue() - 90);
                arrowMarkerDirectionList.add(markerOptions);
                if (size > 3) {
                    if (Integer.parseInt(step.getDistance().getValue()) > 300) {
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
                            arrowMarkerDirectionList.add(markerOptions);

                            nameMarkerStreetList.add(new MarkerOptions().position(headLatLng)
                                    .anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV()));
                        }
                    }
                }

                timeMarkerList.add(new MarkerOptions().position(tailLatLng)
                        .icon(BitmapDescriptorFactory
                                .fromBitmap(iconFactory.makeIcon(step.getDuration().getText())))
                        .anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV()));
            }
        }
        getNameAddressFromLatLng();
    }

    // get name for marker in polyline
    private void getNameAddressFromLatLng() {
        MarkerOptions[] markerOptionsArr = nameMarkerStreetList.toArray(
                new MarkerOptions[nameMarkerStreetList.size()]);

        final Geocoder geocoder;
        geocoder = new Geocoder(context, Locale.getDefault());

        @SuppressLint("StaticFieldLeak") AsyncTask<MarkerOptions, Void, Void> task =
                new AsyncTask<MarkerOptions, Void, Void>() {

                    @NonNull
                    @Override
                    protected Void doInBackground(MarkerOptions... markerOptions) {
                        try {
                            for (int i = 0; i < markerOptions.length; i++) {
                                List<Address> addresses = geocoder.getFromLocation(
                                        markerOptions[i].getPosition().latitude,
                                        markerOptions[i].getPosition().longitude, 1);
                                if (!addresses.isEmpty()) {
                                    try {
                                        nameMarkerStreetList.get(i)
                                                .icon(BitmapDescriptorFactory
                                                        .fromBitmap(iconFactory.makeIcon(
                                                                addresses.get(0).getAddressLine(0))));
                                    } catch (Exception e) {
                                        nameMarkerStreetList.get(i)
                                                .icon(BitmapDescriptorFactory
                                                        .fromBitmap(iconFactory.makeIcon("Unknown")));
                                    }
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                };
        task.execute(markerOptionsArr);
    }

    // update distance from my location to end polyline
    private void updateDistanceASinglePolyline(int position) {

        if (position < 0 || myLocation == null || polylineOptionsList == null || intructionsList == null) {
            return;
        }
        LatLng latLngFrom = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        LatLng latLngTo = polylineOptionsList.get(position).getPoints()
                .get(polylineOptionsList.get(position).getPoints().size() - 1);
        int distance = (int) SphericalUtil.computeDistanceBetween(latLngFrom, latLngTo);

        presenterCb.notifySetChangeAdapter(distance, position);
    }

    private int isRoundaboutPolyline(Polyline polyline) {
        int limitLenght = 50;
        List<LatLng> latLngList = polyline.getPoints();
        if (latLngList == null) {
            return -1;
        }
        for (int i = 0; i < latLngList.size(); i++) {

        }

        return 0;
    }

}
