package com.logictreeit.android.fracturedphoto.helpers;

import java.util.ArrayList; 
import java.util.List;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;

public class AppsInfo {
	public String appName;
 	public Drawable appIcon;
  
	public static ArrayList<AppsInfo> getInstalledApps(Activity activity, boolean getSysPackages) {
		ArrayList<AppsInfo> res = new ArrayList<AppsInfo>();        
		List<PackageInfo> packs = activity.getPackageManager().getInstalledPackages(0);
		int size = packs.size();
		for(int i = 0; i < size; ++i) {
			PackageInfo pkgInfo = packs.get(i);
			if ((!getSysPackages) && (pkgInfo.versionName == null)) {
				continue;
			}
			AppsInfo newInfo = new AppsInfo();
			newInfo.appName = pkgInfo.applicationInfo.loadLabel(activity.getPackageManager()).toString();
		 	newInfo.appIcon = pkgInfo.applicationInfo.loadIcon(activity.getPackageManager());
			res.add(newInfo);
		}
		return res; 
	}
}