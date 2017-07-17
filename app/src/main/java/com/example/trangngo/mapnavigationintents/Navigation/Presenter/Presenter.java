package com.example.trangngo.mapnavigationintents.Navigation.Presenter;

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
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by trangngo on 7/13/17.
 */

public class Presenter implements DirectionCallback {
    private static String TAG = "Presenter";

    private LatLng fromPosition;
    private LatLng toPosition;

    private PresenterToViewCallback.OnRoute onRoute;
    private PresenterToViewCallback presenterCb;
    private List<Step> stepList;
    private List<Instructions> intructionsList;

    public Presenter(PresenterToViewCallback.OnRoute onRoute, PresenterToViewCallback presenterCb) {
        this.onRoute = onRoute;
        this.presenterCb = presenterCb;
    }

    public void setFromToPosition(LatLng fromPosition, LatLng toPosition) {
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
    }

    private void initList() {
        stepList = new ArrayList<>();
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

            for (Route route : direction.getRouteList()) {
                List<Leg> leg = route.getLegList();
            }
            Route route = direction.getRouteList().get(0);
            List<Leg> legs = route.getLegList();
            for (Leg leg : route.getLegList()) {
                stepList = leg.getStepList();
                intructionsList = MyUtils.getInstructionsFromSteps(stepList);
                presenterCb.setAdapterViewInstructions(intructionsList);
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

}
