package com.example.trangngo.mapnavigationintents.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.trangngo.mapnavigationintents.R;
import com.example.trangngo.mapnavigationintents.model.Instructions;

import java.util.List;

/**
 * Created by NgoXuanManh on 7/15/2017.
 */

public class InstructionsAdapter extends PagerAdapter {

    List<Instructions> instructionses;
    LayoutInflater layoutInflater;

    public InstructionsAdapter(Context context, List<Instructions> instructionses) {
        this.instructionses = instructionses;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return instructionses.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = layoutInflater.inflate(R.layout.item_instructions, container, false);

        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.lnContainer);
        TextView tvDistance = (TextView) view.findViewById(R.id.tvDistance);
        TextView tvInstructions = (TextView) view.findViewById(R.id.tvInstruction);

        Instructions instructions = instructionses.get(position);
        tvDistance.setText(instructions.getDistance());
        tvInstructions.setText(instructions.getInstructions());

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
