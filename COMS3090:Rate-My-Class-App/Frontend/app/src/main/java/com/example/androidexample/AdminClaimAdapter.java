package com.example.androidexample;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class AdminClaimAdapter extends ArrayAdapter<AdminClaim> {

    public AdminClaimAdapter(Activity context, List<AdminClaim> claims) {
        super(context, 0, claims);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AdminClaim claim = getItem(position);

//        if(convertView == null) {
//            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_admin_claim, parent, false);
//        }

//        TextView tvClaim1 = convertView.findViewById(R.id.tvClaim1);
//        TextView tvClaim2 = convertView.findViewById(R.id.tvClaim2);

//        if(claim != null) {
//            tvClaim1.setText("#" + claim.claim_id + " | " + claim.claim_type + " | " + claim.target_type + " | " + claim.status);
//            tvClaim2.setText("User: " + claim.username + " | Target: " + claim.target_type + " (" + claim.target_id + ")");
//        }

        return convertView;
    }
}
