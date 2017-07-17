package com.example.trangngo.mapnavigationintents.Navigation.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.akexorcist.googledirection.model.Step;
import com.example.trangngo.mapnavigationintents.Navigation.Presenter.Presenter;
import com.example.trangngo.mapnavigationintents.Navigation.Presenter.PresenterToViewCallback;
import com.example.trangngo.mapnavigationintents.Navigation.adapter.InstructionsAdapter;
import com.example.trangngo.mapnavigationintents.Navigation.model.Instructions;
import com.example.trangngo.mapnavigationintents.Navigation.utils.Key;
import com.example.trangngo.mapnavigationintents.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by trangngo on 7/17/17.
 */

public class NavigationFragment extends Fragment implements OnMapReadyCallback,
        PresenterToViewCallback.OnRoute, PresenterToViewCallback {
    private static final int[] COLORS = new int[]{
            R.color.primary_dark,
            R.color.primary,
            R.color.primary_light,
            R.color.accent,
            R.color.primary_dark_material_light};
    private static final NavigationFragment ourInstance = new NavigationFragment();
    Presenter presenter;
    GoogleMap mMap;
    List<Polyline> polylineList;
    private ProgressDialog progressDialog;
    private LatLngBounds homePDD = new LatLngBounds(
            new LatLng(10.7651909, 106.6619211),
            new LatLng(10.7773018, 106.6999617));
    private FloatingActionButton fabRecenter;
    private ViewPager vpInstructions;

    public static NavigationFragment getInstance() {
        return ourInstance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        polylineList = new ArrayList<>();

        presenter = new Presenter(this, this);
        LatLng fromPosition = getArguments().getParcelable(Key.FROM_POSITION);
        LatLng toPosition = getArguments().getParcelable(Key.TO_POSITION);
        presenter.setFromToPosition(fromPosition, toPosition);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);

        fabRecenter = (FloatingActionButton) view.findViewById(R.id.fab_recenter);
        vpInstructions = (ViewPager) view.findViewById(R.id.vpInstructions);

        MapFragment mapFragment = (MapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        vpInstructions.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Toast.makeText(getActivity(), "Position: " + position, Toast.LENGTH_SHORT).show();
                changeCameraPreview(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homePDD.getCenter(), 15));

        presenter.route();
    }


    @Override
    public void onInputFail() {

    }

    @Override
    public void onStartRoute() {
        progressDialog = ProgressDialog.show(getActivity(), "Please wait.",
                "Fetching route information.", true);
    }

    @Override
    public void onEndRoute(boolean success) {
        progressDialog.dismiss();
        if (success) {

        } else {

        }
    }

    @Override
    public void drawNavigateDirection(List<LatLng> pointList, int i) {
        int colorIndex = i % COLORS.length;
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(getResources().getColor(COLORS[colorIndex]));
        polylineOptions.width(10 + i * 3);
        polylineOptions.addAll(pointList);
        Polyline polyline = mMap.addPolyline(polylineOptions);
        polylineList.add(polyline);
    }

    @Override
    public void setAdapterViewInstructions(List<Instructions> intructionsList) {
        InstructionsAdapter insAdapter = new InstructionsAdapter(getActivity(), intructionsList);
        vpInstructions.setAdapter(insAdapter);

    }


    private void changeCameraPreview(int position) {
        //addIcon(iconFactory, "Manh", stepList.get(position).getStartLocation().getCoordination());
        if (position > 0) {
            Step step = stepList.get(position - 1);
            //addIcon(iconFactory, "Manh2", stepList.get(position - 1).getStartLocation().getCoordination());
            Double heading = SphericalUtil.computeHeading(step.getStartLocation().getCoordination(), step.getEndLocation().getCoordination());
            updateCameraBearing(mMap, step.getEndLocation().getCoordination(), heading.floatValue());
        }
    }

    private void updateCameraBearing(GoogleMap googleMap, LatLng latLng, float bearing) {
        if (googleMap == null) return;
        CameraPosition camPos = CameraPosition
                .builder(
                        googleMap.getCameraPosition() // current Camera
                )
                .target(latLng)
                .bearing(bearing)
                .tilt(45)
                .zoom(18)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }
}
