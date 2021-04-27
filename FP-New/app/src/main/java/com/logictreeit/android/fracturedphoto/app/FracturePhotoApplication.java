package com.logictreeit.android.fracturedphoto.app;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import com.dran.fracturedphoto.R;
import com.google.android.gms.ads.MobileAds;
import com.logictreeit.android.fracturedphoto.helpers.AppsInfo;
import com.logictreeit.android.fracturedphoto.models.ShatteredTile;
import com.logictreeit.android.fracturedphoto.models.SquareTile;
import com.logictreeit.android.fracturedphoto.utils.Utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

public class FracturePhotoApplication extends MultiDexApplication {

	private static ArrayList<ShatteredTile> originalTilesList;
	private static ArrayList<ShatteredTile> droppedTilesList;
	private static ArrayList<ShatteredTile> galleryTilesList;
	private static ArrayList<SquareTile> originalTilesListSquare;
	private static ArrayList<AppsInfo> installedAppsList;

	@Override
	public void onCreate() {
		super.onCreate();
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());

		createAppDirectories();

		MobileAds.initialize(this, getString(R.string.adMobAppId));
		//MobileAds.initialize(this, initializationStatus -> { });

        //printKeyHash(this);
	}

	private void createAppDirectories() {
		File dir = new File(Utils.getAppDirPath(getApplicationContext()));
		if (!dir.exists()) {
			dir.mkdir();
		}
	}

	public static ArrayList<ShatteredTile> getDroppedTilesList() {
		return droppedTilesList;
	}

	public static void setDroppedTilesList(ArrayList<ShatteredTile> droppedTilesList) {
		FracturePhotoApplication.droppedTilesList = droppedTilesList;
	}

	public static ArrayList<ShatteredTile> getOriginalTilesList() {
		return originalTilesList;
	}

	public static void setOriginalTilesList(ArrayList<ShatteredTile> originalTilesList) {
		FracturePhotoApplication.originalTilesList = originalTilesList;
	}

	public static ArrayList<ShatteredTile> getGalleryTilesList() {
		return galleryTilesList;
	}

	public static void setGalleryTilesList(ArrayList<ShatteredTile> galleryTilesList) {
		FracturePhotoApplication.galleryTilesList = galleryTilesList;
	}

	public static ArrayList<SquareTile> getOriginalTilesListSquare() {
		return originalTilesListSquare;
	}

	public static void setOriginalTilesListSquare(ArrayList<SquareTile> originalTilesListSquare) {
		FracturePhotoApplication.originalTilesListSquare = originalTilesListSquare;
	}

	public static ArrayList<AppsInfo> getInstalledAppsList() {
		return installedAppsList;
	}

	public static void setInstalledAppsList(ArrayList<AppsInfo> installedAppsList) {
		FracturePhotoApplication.installedAppsList = installedAppsList;
	}

	public static void printKeyHash(Context context) {
		PackageInfo info;
		try {
			info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.v("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		}catch (Exception e) {
			Log.e("exception", e.toString());
		}
	}
}
