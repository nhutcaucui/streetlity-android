package com.example.streetlity_android.Chat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.streetlity_android.R;

import java.util.ArrayList;

public class ChatObjectAdapter extends ArrayAdapter implements Filterable {

    Context context;
    private ArrayList<ChatObject> mDisplayedValues;

    public ChatObjectAdapter(@NonNull Context context, int resource, @NonNull ArrayList<ChatObject> objects) {
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
        TextView tvBody;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder = null;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        if (convertView == null) {

            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.lv_item_chat, null);

            holder.tvName = convertView.findViewById(R.id.tv_name);

            holder.tvBody = convertView.findViewById(R.id.tv_body);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvName.setText(this.mDisplayedValues.get(position).getName());
        holder.tvBody.setText(this.mDisplayedValues.get(position).getBody());

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