package com.streetlity.client.User.Maintainer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streetlity.client.R;

import java.util.ArrayList;
@Deprecated
public class NormalOrderApdater extends ArrayAdapter {

    Context context;
    private ArrayList<NormalOrderObject> mOriginalValues;

    public NormalOrderApdater(@NonNull Context context, int resource, @NonNull ArrayList<NormalOrderObject> objects) {
        super(context, resource, objects);
        this.context = context;
        this.mOriginalValues = objects;
    }

    @Override
    public int getCount() {
        return (mOriginalValues == null) ? 0 :mOriginalValues.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView reason,name;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        NormalOrderApdater.ViewHolder holder = null;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        if (convertView == null) {

            holder = new NormalOrderApdater.ViewHolder();
            convertView = inflater.inflate(R.layout.lv_item_order, null);
            holder.reason = (TextView) convertView.findViewById(R.id.tv_order_reason);
            holder.name = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(holder);
        } else {
            holder = (NormalOrderApdater.ViewHolder) convertView.getTag();
        }


        holder.reason.setText(this.mOriginalValues.get(position).getReason());
        holder.name.setText(this.mOriginalValues.get(position).getName());

        return convertView;
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        if(position < mOriginalValues.size()) {

        }else{
            position = position - mOriginalValues.size();
        }
        return this.mOriginalValues.get(position);
    }
}
