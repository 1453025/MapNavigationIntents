package com.example.trangngo.mapnavigationintents.Navigation.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.akexorcist.googledirection.model.Step;
import com.example.trangngo.mapnavigationintents.Navigation.model.Instructions;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by NgoXuanManh on 7/17/2017.
 */

public class MyUtils {
    public static List<Instructions> getInstructionsFromSteps(List<Step> stepList) {
        List<Instructions> instructionsList = new ArrayList<>();
        for (Step step : stepList) {
            Instructions instructions = new Instructions.InstructionsBuilder()
                    .setInstructions(step.getHtmlInstruction())
                    .setDistance(step.getDistance().getText())
                    .setManeuver(step.getManeuver())
                    .setEndLatLng(step.getEndLocation().getCoordination())
                    .setStartLatLng(step.getStartLocation().getCoordination())
                    .build();
            instructionsList.add(instructions);
        }
        return instructionsList;
    }

    public static String getNameAddressFromLatLng(final Context context, final double lat, final double lng) {
        final String[] address = {"Unknown"};
        LatLng latlng = new LatLng(lat, lng);
        final Geocoder geocoder;
        geocoder = new Geocoder(context, Locale.getDefault());
        @SuppressLint("StaticFieldLeak") AsyncTask<LatLng, Void, String> task = new AsyncTask<LatLng, Void, String>() {
            @Override
            protected void onPreExecute() {
                // Utils.showProgressDialog(context,"");
                super.onPreExecute();
            }

            @NonNull
            @Override
            protected String doInBackground(LatLng... latLngs) {
                try {
                    List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                    if (!addresses.isEmpty()) {
                        try {
                            return addresses.get(0).getAddressLine(0);
                        } catch (Exception e) {
                            return "Unknow";
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return "Unknow";
            }

            @Override
            protected void onPostExecute(String s) {
                address[0] = s;
            }
        };
        task.execute(latlng);
        return address[0];
    }
}
