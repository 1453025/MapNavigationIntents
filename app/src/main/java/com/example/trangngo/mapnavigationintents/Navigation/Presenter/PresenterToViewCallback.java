package com.example.trangngo.mapnavigationintents.Navigation.Presenter;

import com.example.trangngo.mapnavigationintents.Navigation.model.Instructions;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by trangngo on 7/13/17.
 */

public interface PresenterToViewCallback {

    void drawNavigateDirection(List<LatLng> pointList, int i);

    void setAdapterViewInstructions(List<Instructions> intructionsList);

    interface OnRoute {
        void onInputFail();

        void onStartRoute();

        void onEndRoute(boolean success);


    }
}
