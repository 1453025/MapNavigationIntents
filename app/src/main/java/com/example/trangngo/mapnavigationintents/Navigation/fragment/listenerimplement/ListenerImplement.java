package com.example.trangngo.mapnavigationintents.Navigation.fragment.listenerimplement;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.example.trangngo.mapnavigationintents.Navigation.Presenter.Presenter;
import com.example.trangngo.mapnavigationintents.R;
import com.google.android.gms.maps.GoogleMap;

import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;


/**
 * Created by trangngo on 7/18/17.
 */

public class ListenerImplement implements ViewPager.OnPageChangeListener, View.OnClickListener
        , GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraMoveListener {
    static String TAG = "Listener Implement";
    int state = -1;
    Presenter presenter;

    public ListenerImplement(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (state == SCROLL_STATE_DRAGGING) {
            presenter.setRecenter(false);
            presenter.changeCameraFollowStep(position);
            state = -1;
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == SCROLL_STATE_DRAGGING) {
            this.state = SCROLL_STATE_DRAGGING;
        }
    }

    @Override
    public void onCameraMoveStarted(int i) {
        if (i == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            presenter.setRecenter(false);
        }
    }

    @Override
    public void onCameraMove() {
        presenter.addMarkerVisibleToMap();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_recenter) {
            presenter.onRecenter();
        }
    }
}
