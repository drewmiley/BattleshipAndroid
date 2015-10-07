package com.scottlogic.dmiley.battleship.activities.twoplayer.spinner;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.scottlogic.dmiley.battleship.R;

// Custom adaptor for use in our Spinner strip selector
public class StripArrayAdaptor extends ArrayAdapter<CharSequence> {

    private Context context;
    private int support_simple_spinner_dropdown_item;

    private String[] textValues;
    private int[] colorValues;

    public StripArrayAdaptor(Context context, int support_simple_spinner_dropdown_item, String[] textValues, int[] colorValues) {
        super(context, R.layout.support_simple_spinner_dropdown_item, textValues);
        this.context = context;
        this.support_simple_spinner_dropdown_item = support_simple_spinner_dropdown_item;
        this.textValues = textValues;
        this.colorValues = colorValues;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();

        View row = layoutInflater.inflate(support_simple_spinner_dropdown_item, parent, false);
        TextView label = (TextView) row.findViewById(android.R.id.text1);

        label.setText(textValues[position]);
        if (position != 0) {
            label.setTextColor(Color.WHITE);
            row.setBackgroundColor(colorValues[position - 1]);
        }
        return row;
    }
}
