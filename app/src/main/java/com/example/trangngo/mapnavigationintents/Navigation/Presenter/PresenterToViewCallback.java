package com.example.trangngo.mapnavigationintents.Navigation.Presenter;

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

    void changeCameraFollowStep(List<Step> stepList, int index);

    void addMarkerVisibleToMap(List<MarkerOptions> arrowMarkerDirectionList,
                               List<MarkerOptions> nameMarkerStreetList,
                               List<MarkerOptions> timeMarkerList);

    void setAdapterViewInstructions(List<Instructions> intructionsList);

    interface OnRoute {
        void onInputFail();

        void onStartRoute();

        void onEndRoute(boolean success);


    }
}
