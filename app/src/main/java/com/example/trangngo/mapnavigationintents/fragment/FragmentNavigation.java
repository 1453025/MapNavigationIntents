package com.example.trangngo.mapnavigationintents.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.trangngo.mapnavigationintents.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by trangngo on 7/17/17.
 */

public class FragmentNavigation extends Fragment {

    GoogleMap mMap;
    private FloatingActionButton fabRecenter;
    private ViewPager vpInstructions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);

        fabRecenter = (FloatingActionButton) view.findViewById(R.id.fab_recenter);
        vpInstructions = (ViewPager) view.findViewById(R.id.vpInstructions);
        SupportMapFragment mapFragment = getFragmentManager().findFragmentById(R.id.mapFragment);

        return view;
    }
}
