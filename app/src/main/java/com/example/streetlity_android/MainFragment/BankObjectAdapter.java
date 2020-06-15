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
}