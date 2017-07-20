package com.example.trangngo.mapnavigationintents.Navigation.Presenter;

import android.location.Location;

import com.akexorcist.googledirection.model.Step;
import com.example.trangngo.mapnavigationintents.Navigation.model.Instructions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Created by trangngo on 7/13/17.
 */

public interface PresenterToViewCallback {

    void drawNavigateDirection(List<LatLng> pointList, int i);

    void moveCameraFollowStep(List<Step> stepList, int position);

    void addMarkerVisibleToMap(List<MarkerOptions> arrowMarkerDirectionList,
                               List<MarkerOptions> nameMarkerStreetList,
                               List<MarkerOptions> timeMarkerList);

    void setAdapterViewInstructions(List<Instructions> intructionsList);

    void moveMarkerFollowMyLocation(Location location);

    void moveCameraFollowMyLocation(Location myLocation);

    void setViewPagerFollowMyLocation(int i);

    void changeColorViewPager(int position);

    void notifySetChangeAdapter(int data, int index);

    interface OnRoute {
        void onInputFail();

        void onStartRoute();

        void onEndRoute(boolean success);


    }
}
