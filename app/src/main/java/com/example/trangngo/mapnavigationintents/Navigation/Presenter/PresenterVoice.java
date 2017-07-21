package com.example.trangngo.mapnavigationintents.Navigation.Presenter;

import android.content.Context;

import com.akexorcist.googledirection.model.Direction;

/**
 * Created by manhngo on 7/21/17.
 */

public class PresenterVoice {
    Context context;

    Direction direction;

    PresenterVoiceCallBack presenterVoiceCallBack;

    int distance;


    public PresenterVoice(Context context, Direction direction, PresenterVoiceCallBack presenterVoiceCallBack) {
        this.context = context;
        this.direction = direction;
        this.presenterVoiceCallBack = presenterVoiceCallBack;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    private void handleNotification(int distance) {
        if (distance <= 0) {

            return;
        }

        if (100 < distance && distance <= 200) {

            return;
        }

        if (10 < distance && distance <= 100) {

            return;
        }

        if (distance <= 10) {
            presenterVoiceCallBack.speech(distance);
        }

    }
}
