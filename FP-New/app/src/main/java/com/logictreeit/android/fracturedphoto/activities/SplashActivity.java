package com.logictreeit.android.fracturedphoto.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
//import com.crashlytics.android.Crashlytics;
import com.dran.fracturedphoto.R;
import com.logictreeit.android.fracturedphoto.utils.ApplicationConstants;

//import io.fabric.sdk.android.Fabric;

public class SplashActivity extends Activity implements ApplicationConstants{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Fabric.with(this, new Crashlytics());
		setContentView(R.layout.splash_screen);

		new Handler().postDelayed(() -> {
			if (getIntent() != null && getIntent().hasExtra(Extras_Keys.PUZZLE_URL)){
				Intent intent = new Intent(this, PuzzlesGalleryActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				intent.putExtra(ApplicationConstants.Extras_Keys.FROM_PUSH, true);
				intent.putExtra(ApplicationConstants.Extras_Keys.PUZZLE_URL, getIntent().getStringExtra(Extras_Keys.PUZZLE_URL));
				startActivity(intent);
			} else {
				startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
			}
			finish();
		}, SPLASH_SCREEN_DURATION);
	}
}
