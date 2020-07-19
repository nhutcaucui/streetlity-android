package com.streetlity.client;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ReviewAdapter extends ArrayAdapter implements Filterable {

    Context context;
    private ArrayList<Review> mDisplayedValues;

    public ReviewAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Review> objects) {
        super(context, resource, objects);
        this.context = context;
        this.mDisplayedValues = objects;

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
        TextView username, body;
        RatingBar rating;
        ImageView editable;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder = null;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        if (convertView == null) {

            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.review_item, null);
            holder.username = (TextView) convertView.findViewById(R.id.tv_username);
            holder.body = (TextView) convertView.findViewById(R.id.tv_body_review);
            holder.rating = (RatingBar) convertView.findViewById(R.id.ratingBar2);
            holder.editable = convertView.findViewById(R.id.img_editable);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.username.setText(this.mDisplayedValues.get(position).username);
        holder.body.setText(this.mDisplayedValues.get(position).reviewBody);
        holder.rating.setRating(this.mDisplayedValues.get(position).rating);

        LayerDrawable stars = (LayerDrawable) holder.rating.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        if(context instanceof AllServiceMap){

        }else {
            if (MyApplication.getInstance().getUsername().equals(holder.username.getText().toString())) {
                holder.editable.setVisibility(View.VISIBLE);
            } else {
                holder.editable.setVisibility(View.GONE);
            }
        }

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
