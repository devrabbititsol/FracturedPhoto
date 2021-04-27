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
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.logictreeit.android.fracturedphoto.utils.ApplicationConstants;

//import io.fabric.sdk.android.Fabric;

public class LoadPuzzleActivity extends Activity implements OnClickListener, ApplicationConstants {

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
        setContentView(R.layout.activity_loadpuzzles);

        ll = (LinearLayout) findViewById(R.id.ll);
        mView = getLayoutInflater().inflate(R.layout.ad_layout, ll, true);
        relativeLayout = (RelativeLayout) mView.findViewById(R.id.relative_layout);
        ad = (AdView) mView.findViewById(R.id.adView);

        findViewById(R.id.loadSavedPuzzle).setOnClickListener(this);
        findViewById(R.id.loadFromFB).setOnClickListener(this);
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
        getApplicationContext().bindService(getExplicitIapIntent(), mServiceConn, BIND_AUTO_CREATE);
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
        } catch (Exception e) {
            e.printStackTrace();
            //Crashlytics.logException(e);
        }
        return false;
    }

    private void hideAd() {
        runOnUiThread(() -> {
            ad.setEnabled(false);
            ad.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.GONE);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        bindIAPService();// binding the in_app_purchase service in this fragment
    }

    public void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loadSavedPuzzle:
                startActivity(new Intent(this, PuzzleHistoryActivity.class));
                break;
            case R.id.loadFromFB:
                navigateToFacebookPage();
                break;
        }
    }

    private void navigateToFacebookPage() {
        //Intent intent = new Intent(Intent.ACTION_VIEW);
        //intent.setData(Uri.parse("https://www.facebook.com/pages/Fractured-Photo/295372787327265?fref=nf"));
        //intent.setData(Uri.parse("https://fracturedphotoapp.com/fracturedphoto/gallery.php"));

        Intent intent = new Intent(this, PuzzlesGalleryActivity.class);
        startActivity(intent);
    }

    public void showToast(final String message) {
        runOnUiThread(() -> Toast.makeText(LoadPuzzleActivity.this, message, Toast.LENGTH_LONG).show());
    }
}
