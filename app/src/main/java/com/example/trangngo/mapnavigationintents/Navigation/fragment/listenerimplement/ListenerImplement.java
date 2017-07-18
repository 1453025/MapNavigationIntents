package com.example.trangngo.mapnavigationintents.Navigation.fragment.listenerimplement;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.example.trangngo.mapnavigationintents.Navigation.Presenter.Presenter;
import com.example.trangngo.mapnavigationintents.R;
import com.google.android.gms.maps.GoogleMap;

/**
 * Created by trangngo on 7/18/17.
 */

public class ListenerImplement implements ViewPager.OnPageChangeListener, View.OnClickListener
        , GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraMoveListener {

    Presenter presenter;

    public ListenerImplement(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        presenter.changeCameraFollowStep(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onCameraMoveStarted(int i) {
    }

    @Override
    public void onCameraMove() {
        presenter.addMarkerVisibleToMap();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_recenter) {

        }
    }
}
