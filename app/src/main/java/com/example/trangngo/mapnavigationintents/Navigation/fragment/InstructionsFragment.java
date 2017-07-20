package com.example.trangngo.mapnavigationintents.Navigation.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.trangngo.mapnavigationintents.R;


/**
 * Created by trangngo on 7/20/17.
 */

public class InstructionsFragment extends Fragment {

    // Store instance variables
    private String title;
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static InstructionsFragment newInstance(int page, String title) {
        InstructionsFragment instructionsFragment = new InstructionsFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        instructionsFragment.setArguments(args);
        return instructionsFragment;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_instructions, container, false);
        TextView tvDistance = (TextView) view.findViewById(R.id.tvDistance);
        TextView tvInstructions = (TextView) view.findViewById(R.id.tvInstruction);


        return view;
    }
}
