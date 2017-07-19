package com.example.trangngo.mapnavigationintents.Navigation.fragment.listenerimplement;

import android.location.Location;

import com.example.trangngo.mapnavigationintents.Navigation.Presenter.Presenter;

/**
 * Created by trangngo on 7/18/17.
 */

public class MyLocationListener implements ListenLocationChange {


    private Presenter presenter;

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;

    }

    @Override
    public void onLocationChange(Location location) {
        if (presenter == null) {
            return;
        }
        if (location != null) {
            presenter.onLocationChange(location);
        }
    }
}
