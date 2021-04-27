package com.logictreeit.android.fracturedphoto.adapters;

import java.util.ArrayList;

import com.dran.fracturedphoto.R;
import com.logictreeit.android.fracturedphoto.helpers.AppsInfo;
import com.logictreeit.android.fracturedphoto.utils.ApplicationConstants;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SharePopupAdapter extends BaseAdapter implements ApplicationConstants {

    private Activity activity;
    private String[] appNames;
    private LayoutInflater inflater;
    private ArrayList<AppsInfo> installedApps;

    public SharePopupAdapter(Activity activity, String[] appNames, ArrayList<AppsInfo> installedApps) {
        this.activity = activity;
        this.appNames = appNames;
        this.inflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.installedApps = installedApps;
    }

    @Override
    public int getCount() {
        return appNames.length;
    }

    @Override
    public Object getItem(int position) {
        return appNames[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        final ViewHolder holder;
        if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
            rowView = inflater.inflate(R.layout.share_list_row, null);
            holder = new ViewHolder();
            holder.iconImageView = (ImageView) rowView.findViewById(R.id.share_icon);
            holder.nameTextView = (TextView) rowView.findViewById(R.id.app_name);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        String appName = appNames[position];
        holder.nameTextView.setText(appName);
        Drawable icon = getAppIconOf(appName);
        if (icon != null) {
            holder.iconImageView.setImageDrawable(icon);
        } else {
            if (appName.equalsIgnoreCase("Facebook")) {
                holder.iconImageView.setImageResource(R.drawable.facebook);
            } else if (appName.equalsIgnoreCase(GALLERY_PAGE_NAME)) {
                holder.iconImageView.setImageResource(R.drawable.small_logo);
            } else if (appName.equalsIgnoreCase("Twitter")) {
                holder.iconImageView.setImageResource(R.drawable.twitter);
            } else if (appName.equalsIgnoreCase("Gmail")) {
                holder.iconImageView.setImageResource(R.drawable.gmail);
            }
        }
        return rowView;
    }

    private Drawable getAppIconOf(String appName) {
        final int max = installedApps.size();
        for (int i = 0; i < max; ++i) {
            AppsInfo appInfo = installedApps.get(i);
            if (appInfo.appName.equalsIgnoreCase(appName)) {
                return appInfo.appIcon;
            }
        }
        return null;
    }

    static class ViewHolder {
        public ImageView iconImageView;
        public TextView nameTextView;
    }
}
