package com.example.streetlity_android.Achievement;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.streetlity_android.Chat.ChatObject;
import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

public class AchievementObjectAdapter extends ArrayAdapter implements Filterable {

    Context context;
    private ArrayList<AchievementObject> mDisplayedValues;

    public AchievementObjectAdapter(@NonNull Context context, int resource, @NonNull ArrayList<AchievementObject> objects) {
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
        TextView tvName;
        TextView tvPoint;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder = null;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        if (convertView == null) {

            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.lv_item_achievement, null);

            holder.tvName = convertView.findViewById(R.id.tv_achievement_name);

            holder.tvPoint = convertView.findViewById(R.id.tv_point);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvName.setText(this.mDisplayedValues.get(position).getName());
        holder.tvPoint.setText(Integer.toString(this.mDisplayedValues.get(position).getPoint()));

        if(this.mDisplayedValues.get(position).isEarned()){
            holder.tvName.setTextColor(context.getResources().getColor(R.color.black));
            holder.tvName.setTypeface(null, Typeface.BOLD);
            holder.tvPoint.setTextColor(context.getResources().getColor(R.color.black));
        }else{
            holder.tvName.setTextColor(context.getResources().getColor(android.R.color.tab_indicator_text));
            holder.tvName.setTypeface(null, Typeface.NORMAL);
            holder.tvPoint.setTextColor(context.getResources().getColor(android.R.color.tab_indicator_text));
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