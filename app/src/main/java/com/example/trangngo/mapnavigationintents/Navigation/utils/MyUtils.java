package com.example.trangngo.mapnavigationintents.Navigation.utils;

import com.akexorcist.googledirection.model.Step;
import com.example.trangngo.mapnavigationintents.Navigation.model.Instructions;

import java.util.ArrayList;
import java.util.List;

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

    public static String decodeManeuver(String maneuver) {
        if (maneuver == null) {
            return null;
        }
        String strManuever;
        strManuever = maneuver.replace('-', ' ');
        return strManuever;
    }
}
