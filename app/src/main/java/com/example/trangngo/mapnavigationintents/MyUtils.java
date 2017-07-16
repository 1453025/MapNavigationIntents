package com.example.trangngo.mapnavigationintents;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by NgoXuanManh on 7/16/2017.
 */

public class MyUtils {

    public static Location convertLatLngToLocation(LatLng latLng) {
        Location location = new Location("someLoc");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }

    public static float bearingBetweenLatLngs(LatLng beginLatLng, LatLng endLatLng) {
        Location beginLocation = convertLatLngToLocation(beginLatLng);
        Location endLocation = convertLatLngToLocation(endLatLng);
        return beginLocation.bearingTo(endLocation);
    }
}
