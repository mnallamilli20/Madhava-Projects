package com.example.androidexample;

import android.app.Activity;
import android.graphics.Color;
import android.icu.text.Transliterator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/*
 * Adapter for displaying search filter results
 * Rows show color rating badge on left
 * Names to the right of color
 *
 * Green: 4.0+ rating
 * Yellow: 3.0-3.9 rating
 * Orange: 2.0-2.9 rating
 * Red: <2.0 rating
 */

public class FilterResultAdapter extends ArrayAdapter<FilterResult> {
    public FilterResultAdapter(Activity context, List<FilterResult> results) {
        super(context, 0, results);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FilterResult result = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_filter_result, parent, false);
        }

        TextView tvRating = convertView.findViewById(R.id.tvResultRating);
        TextView tvRatingOutOf = convertView.findViewById(R.id.tvResultRatingOutOf);
        TextView tvName = convertView.findViewById(R.id.ResultName);
        TextView tvSubtitle = convertView.findViewById(R.id.tvResultSubtitle);
        TextView tvRatingLabel = convertView.findViewById(R.id.tvResultRatingLabel);
        View ratingBadge = convertView.findViewById(R.id.ratingBadge);

        if(result != null) {
            //rating number
            if(result.rating > 0) {
                tvRating.setText(String.format("%.1f", result.rating));
                tvRatingOutOf.setText("/5");
            }
            else {
                tvRating.setText("N/A");
                tvRatingOutOf.setText("");
            }

            //badge color from rating
            int badgeColor = getRatingColor(result.rating);
            ratingBadge.getBackground().setTint(badgeColor);

            //name & subtitle
            tvName.setText(result.name);
            tvSubtitle.setText(result.subtitle);

            //what rating measures
            tvRatingLabel.setText(result.ratingLabel);
        }
        return convertView;
    }

    /*
     * Creates and returns color based on rating value
     */
    private int getRatingColor(double rating) {
        if(rating >= 4.0) {
            return Color.parseColor("#008000"); //green
        }
        if(rating >= 3.0 && rating <=3.9) {
            return Color.parseColor("#FFFF00"); //yellow
        }
        if(rating >= 2.0 && rating <= 2.9) {
            return Color.parseColor("#FFA500"); //orange
        }
        if(rating < 2.0) {
            return Color.parseColor("#FF0000"); //red
        }
        return Color.parseColor("#808080"); //gray for N/A
    }
}
