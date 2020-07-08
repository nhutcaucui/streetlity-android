package com.example.streetlity_android.MainFragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.streetlity_android.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class BankObjectAdapter extends ArrayAdapter implements Filterable {

    Context context;
    private ArrayList<BankObject> mDisplayedValues;
    private ArrayList<BankObject> mOriginalValues;

    public BankObjectAdapter(@NonNull Context context, int resource, @NonNull ArrayList<BankObject> objects) {
        super(context, resource, objects);
        this.context = context;
        this.mDisplayedValues = objects;
        this.mOriginalValues = objects;
    }

    @Override
    public int getCount() {
        return (mDisplayedValues == null) ? 0 :mDisplayedValues.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView tvName;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder = null;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        if (convertView == null) {

            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.spinner_item_broadcast, null);

            holder.tvName = convertView.findViewById(R.id.tv_name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvName.setText(this.mDisplayedValues.get(position).getName());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.spinner_item_broadcast,parent, false);
        }
        TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
        tvName.setText(mDisplayedValues.get(position).getName());

        return convertView;
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        if(position < mDisplayedValues.size()) {

        }else{
            position = position - mDisplayedValues.size();
        }
        return this.mDisplayedValues.get(position);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                mDisplayedValues = (ArrayList<BankObject>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
                if(constraint.toString().equals("") || constraint.toString().equals("")){
                    mDisplayedValues = mOriginalValues;
                }
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<BankObject> FilteredArrList = new ArrayList<BankObject>();
                Log.e("", "performFiltering: " + constraint );
                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<BankObject>(mDisplayedValues); // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0 || constraint == "") {

                    // set the Original result to return
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {

                    for (int i = 0; i < mOriginalValues.size(); i++) {
                        if (mOriginalValues.get(i).getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            FilteredArrList.add(mOriginalValues.get(i));
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }
}