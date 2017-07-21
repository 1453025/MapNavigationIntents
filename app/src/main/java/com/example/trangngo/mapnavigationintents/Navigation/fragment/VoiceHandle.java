package com.example.trangngo.mapnavigationintents.Navigation.fragment;

import android.content.Context;
import android.widget.Toast;

import com.example.trangngo.mapnavigationintents.Navigation.Presenter.PresenterVoiceCallBack;

/**
 * Created by manhngo on 7/21/17.
 */

public class VoiceHandle implements PresenterVoiceCallBack {

    private Context context;

    public VoiceHandle(Context context) {
        this.context = context;
    }

    void sound() {
        Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void speech(int distance) {

    }
}
