package com.example.trangngo.mapnavigationintents.Navigation.Presenter;

import android.content.Context;
import android.util.Log;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.Language;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.example.trangngo.mapnavigationintents.Navigation.model.Instructions;
import com.example.trangngo.mapnavigationintents.Navigation.utils.MyUtils;
import com.example.trangngo.mapnavigationintents.R;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by trangngo on 7/13/17.
 */

public class Presenter implements DirectionCallback {
    private static String TAG = "Presenter";
    IconGenerator iconFactory;
    private LatLng fromPosition;
    private LatLng toPosition;
    private PresenterToViewCallback.OnRoute onRoute;
    private PresenterToViewCallback presenterCb;
    private Context context;

    private List<Step> stepList;
    private List<Instructions> intructionsList;
    private List<MarkerOptions> arrowMarkerDirectionList;
    private List<MarkerOptions> nameMarkerStreetList;
    private List<MarkerOptions> timeMarkerList;

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
        iconFactory = new IconGenerator(context);
        stepList = new ArrayList<>();
        arrowMarkerDirectionList = new ArrayList<>();
        nameMarkerStreetList = new ArrayList<>();
        timeMarkerList = new ArrayList<>();
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
            onRoute.onEndRoute(true);
            //drawNavigateDirection(direction.getRouteList());

            init();
            for (Route route : direction.getRouteList()) {
                List<Leg> leg = route.getLegList();
            }
            Route route = direction.getRouteList().get(0);
            List<Leg> legs = route.getLegList();
            for (Leg leg : route.getLegList()) {
                stepList = leg.getStepList();
                intructionsList = MyUtils.getInstructionsFromSteps(stepList);
                presenterCb.setAdapterViewInstructions(intructionsList);
                makeArrowMakerDirection();
                for (int i = 0; i < leg.getStepList().size(); i++) {
                    presenterCb.drawNavigateDirection(leg.getStepList().get(i).getPolyline().getPointList(), 0);
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
        presenterCb.changeCameraFollowStep(stepList, position);

    }

    public void addMarkerVisibleToMap() {
        presenterCb.addMarkerVisibleToMap(arrowMarkerDirectionList, nameMarkerStreetList, timeMarkerList);
    }

    public void makeArrowMakerDirection() {

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

                            nameMarkerStreetList.add(new MarkerOptions().position(headLatLng).
                                    anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV()));
                        }
                    }
                }

                timeMarkerList.add(new MarkerOptions().position(tailLatLng)
                        .icon(BitmapDescriptorFactory
                                .fromBitmap(iconFactory.makeIcon(step.getDuration().getText()))).
                                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV()));
            }

        }
    }
}
