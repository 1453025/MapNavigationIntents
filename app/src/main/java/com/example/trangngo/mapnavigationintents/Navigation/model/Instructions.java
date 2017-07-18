package com.example.trangngo.mapnavigationintents.Navigation.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by NgoXuanManh on 7/15/2017.
 */

public class Instructions {

    private String distance;
    private String instructions;
    private String maneuver;
    private LatLng startLatLng;
    private LatLng endLatLng;

    Instructions(InstructionsBuilder builder) {
        this.distance = builder.distance;
        this.instructions = builder.instructions;
        this.maneuver = builder.maneuver;
        this.startLatLng = builder.startLatLng;
        this.endLatLng = builder.endLatLng;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getManeuver() {
        return maneuver;
    }

    public void setManeuver(String maneuver) {
        this.maneuver = maneuver;
    }

    public static class InstructionsBuilder {
        String distance;
        String instructions;
        String maneuver;
        LatLng startLatLng;
        LatLng endLatLng;

        public InstructionsBuilder setDistance(String distance) {
            this.distance = distance;
            return this;
        }

        public InstructionsBuilder setInstructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        public InstructionsBuilder setManeuver(String maneuver) {
            this.maneuver = maneuver;
            return this;
        }

        public InstructionsBuilder setStartLatLng(LatLng startLatLng) {
            this.startLatLng = startLatLng;
            return this;
        }

        public InstructionsBuilder setEndLatLng(LatLng endLatLng) {
            this.endLatLng = endLatLng;
            return this;
        }

        public Instructions build() {
            return new Instructions(this);
        }
    }
}
