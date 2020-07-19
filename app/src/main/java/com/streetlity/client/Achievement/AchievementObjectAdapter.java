package com.streetlity.client.Achievement;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streetlity.client.MainFragment.MapObject;
import com.streetlity.client.R;

import java.util.ArrayList;

public class AchievementObjectAdapter extends ArrayAdapter implements Filterable {

    Context context;
    private ArrayList<MapObject> mDisplayedValues;

    public AchievementObjectAdapter(@NonNull Context context, int resource, @NonNull ArrayList<MapObject> objects) {
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
        TextView tvAddress;
        ImageView imgIcon;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder = null;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        if (convertView == null) {

            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.lv_item_achievement, null);

            holder.tvName = convertView.findViewById(R.id.tv_name);

            holder.tvAddress = convertView.findViewById(R.id.tv_address);

            holder.imgIcon = convertView.findViewById(R.id.img_service_icon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvName.setText(this.mDisplayedValues.get(position).getName());
        holder.tvAddress.setText(this.mDisplayedValues.get(position).getAddress());

        if(this.mDisplayedValues.get(position).getType() == 1){
            holder.imgIcon.setImageResource(R.drawable.fuel_big_icon);
        }else if(this.mDisplayedValues.get(position).getType() == 2){
            holder.imgIcon.setImageResource(R.drawable.wc_big_icon);
        }else if(this.mDisplayedValues.get(position).getType() == 3){
            holder.imgIcon.setImageResource(R.drawable.fix_big_icon);
        }else if(this.mDisplayedValues.get(position).getType() == 4){
            holder.imgIcon.setImageResource(R.drawable.atm_big_icon);
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