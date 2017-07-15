package com.example.trangngo.mapnavigationintents.model;

/**
 * Created by NgoXuanManh on 7/15/2017.
 */

public class Instructions {

    String distance;
    String instructions;
    String maneuver;

    public Instructions(String distance, String instructions) {
        this.distance = distance;
        this.instructions = instructions;
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
}
