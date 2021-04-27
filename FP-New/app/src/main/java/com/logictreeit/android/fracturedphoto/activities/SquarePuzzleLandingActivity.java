package com.logictreeit.android.fracturedphoto.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
//import com.crashlytics.android.Crashlytics;
import com.dran.fracturedphoto.R;
import com.dran.fracturedphoto.R.string;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.logictreeit.android.fracturedphoto.helpers.BitmapHelper;
import com.logictreeit.android.fracturedphoto.utils.ApplicationConstants;
import com.logictreeit.android.fracturedphoto.utils.ApplicationConstants.Extras_Keys;
import com.logictreeit.android.fracturedphoto.utils.Utils;

//import io.fabric.sdk.android.Fabric;

public class SquarePuzzleLandingActivity extends Activity implements ApplicationConstants {
    private String filePath;
    private boolean isCreatePuzzleMode;
    private Bitmap photoBitmap;
    private int numOfPieces;
    private EditText editText;
    private int rotation;
    LinearLayout ll;
    View mView;
    public RelativeLayout relativeLayout;
    public AdView adView, ad;
    public final Handler handler = new Handler();
    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final int BILLING_RESPONSE_RESULT_OK = 0;
    private static final String RESPONSE_BUY_INTENT = "BUY_INTENT";
    private IInAppBillingService mService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        setContentView(R.layout.square_puzzle_landing_screen);
        
        editText = (EditText) findViewById(R.id.pzlNameInput);
        
        filePath = getIntent().getStringExtra(Extras_Keys.PHOTO_PATH);
        rotation = getIntent().getIntExtra(Extras_Keys.PHOTO_ORIENTATION, 0);
        isCreatePuzzleMode = getIntent().getBooleanExtra(Extras_Keys.IS_CREATE_PUZZLE_MODE, true);
        
        photoBitmap = BitmapHelper.decodeBitmapFromPath(filePath, 640, 480, rotation);
        
        if (photoBitmap != null) {
            numOfPieces = getIntent().getIntExtra(Extras_Keys.NUM_OF_PIECES, 12);
        } else {
            Toast.makeText(getApplicationContext(), "Something went wrong. Please try again later", Toast.LENGTH_LONG).show();
            finish();
        }
        Utils.applyFontForWholeView(getApplicationContext(), (LinearLayout) findViewById(R.id.squarePuzzleLandingScreen), "Bold");
        
        ll = (LinearLayout) findViewById(R.id.squarePuzzleLandingScreen);
        mView = getLayoutInflater().inflate(R.layout.ad_layout, ll, true);
        relativeLayout = (RelativeLayout) mView.findViewById(R.id.relative_layout);
        ad = (AdView) mView.findViewById(R.id.adView);
    }
    
    public void createClicked(View v) {
        String pzlName = editText.getText().toString().trim();
        if (pzlName.length() < 1) {
            editText.setError("Missing");
        } else {
            Intent intent = new Intent(this, PlaySquarePuzzleActivity.class);
            intent.putExtra(Extras_Keys.PHOTO_PATH, filePath);
            intent.putExtra(Extras_Keys.NUM_OF_PIECES, numOfPieces);
            intent.putExtra(Extras_Keys.PUZZLE_NAME, pzlName);
            intent.putExtra(Extras_Keys.IS_CREATE_PUZZLE_MODE, isCreatePuzzleMode);
            intent.putExtra(Extras_Keys.PHOTO_ORIENTATION, getIntent().getIntExtra(Extras_Keys.PHOTO_ORIENTATION, 0));
            
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            
            startActivity(intent);
        }
    }
    
    @Override
    protected void onDestroy() {
        if (photoBitmap != null && !photoBitmap.isRecycled()) {
            photoBitmap.recycle();
            photoBitmap = null;
        }
        
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        bindIAPService();
    }
    
    public void displayAds() {
        // Create an ad.
        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        
        adView.setAdUnitId(getString(string.banner_ad_unit_id));
        
        // Create an ad request. Check logcat output for the hashed device ID to get test ads on a
        // physical device.
        //	AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        
        // Start loading the ad in the background.
        ad.loadAd(adRequest);
        ad.setVisibility(View.VISIBLE);
    }

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
            if (checkforSubscription()) {
                hideAd();
            } else {
                displayAds();
            }
        }
    };
    
    private void bindIAPService() {
        getApplicationContext().bindService(getExplicitIapIntent(),
                mServiceConn, BIND_AUTO_CREATE);
    }
    
    private Intent getExplicitIapIntent() {
        PackageManager pm = getPackageManager();
        Intent implicitIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        implicitIntent.setPackage("com.android.vending");
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(implicitIntent, 0);
        
        // Is somebody else trying to intercept our IAP call?
        if (resolveInfos == null || resolveInfos.size() != 1) {
            return null;
        }
        
        ResolveInfo serviceInfo = resolveInfos.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        Intent iapIntent = new Intent();
        iapIntent.setComponent(component);
        return iapIntent;
    }

    public boolean checkforSubscription() {
        Bundle activeSubs;
        try {
            activeSubs = mService.getPurchases(3, getPackageName(), "inapp", null);
            int response = activeSubs.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> ownedSkus = activeSubs.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                if (ownedSkus.contains(ADS_SKU)) {
                    return true;
                } else {
                    return false;
                }
                
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void showMessage(String message) {
        Toast.makeText(SquarePuzzleLandingActivity.this, message, Toast.LENGTH_LONG).show();
    }
    
    private void hideAd() {
        
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ad.setEnabled(false);
                ad.setVisibility(View.GONE);
                relativeLayout.setVisibility(View.GONE);
            }
        });
    }

    private void showToast(final String msg) {
        runOnUiThread(() -> Toast.makeText(SquarePuzzleLandingActivity.this, msg, Toast.LENGTH_LONG).show());
    }
}
