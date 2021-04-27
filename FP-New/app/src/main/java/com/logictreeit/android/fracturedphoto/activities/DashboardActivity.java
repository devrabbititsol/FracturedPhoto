package com.logictreeit.android.fracturedphoto.activities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.android.vending.billing.IInAppBillingService;
//import com.crashlytics.android.Crashlytics;
import com.dran.fracturedphoto.R;
import com.dran.fracturedphoto.R.string;
import com.facebook.FacebookSdk;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.logictreeit.android.fracturedphoto.inapp_billing_utils.IabHelper;
import com.logictreeit.android.fracturedphoto.utils.ApplicationConstants;
import com.logictreeit.android.fracturedphoto.utils.Utils;
//import io.fabric.sdk.android.Fabric;

public class DashboardActivity extends Activity implements ApplicationConstants, View.OnClickListener {

    private static final int PERMISSIONS_REQUEST = 123;
    RelativeLayout ll;
    View mView;
    public RelativeLayout relativeLayout;
    public AdView adView, ad;
    public final Handler handler = new Handler();
    private static final int RESPONSE_PURCHASE = 1001;
    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final int BILLING_RESPONSE_RESULT_OK = 0;
    private static final String RESPONSE_BUY_INTENT = "BUY_INTENT";
    private IInAppBillingService mService;
    private boolean isAdsPurchased;
    private boolean isShatteredPuzzlesPurchased;
    private ImageView upgradeApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        setContentView(R.layout.dashboard);
        ll = findViewById(R.id.ll);
        mView = getLayoutInflater().inflate(R.layout.ad_layout, ll, true);
        relativeLayout = mView.findViewById(R.id.relative_layout);
        ad = mView.findViewById(R.id.adView);
        upgradeApp = findViewById(R.id.upgradeApp);
        findViewById(R.id.demoVideos).setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
        }

        FirebaseMessaging.getInstance().subscribeToTopic(PUBLIC_BROADCAST_TOPIC_NAME).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //showToast(PUBLIC_BROADCAST_TOPIC_NAME + " Topic is subscribed");
                    return;
                }
                //showToast("Not subscribed for topic "+PUBLIC_BROADCAST_TOPIC_NAME);
            }
        });

        if (IS_ADMIN_MODE) {
            FirebaseMessaging.getInstance().subscribeToTopic(SELF_BROADCAST_TOPIC_NAME).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        //showToast(SELF_BROADCAST_TOPIC_NAME + " Topic is subscribed");
                        return;
                    }
                    //showToast("Not subscribed for topic "+SELF_BROADCAST_TOPIC_NAME);
                }
            });
        }
    }

    /*public static void printHashKey(Context pContext) {
        try {
            PackageInfo info = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.d("AppLog", "printHashKey() Hash Key: " + hashKey);
            }
        } catch (Exception e) {
            Log.e("AppLog", "printHashKey()", e);
        }
    }*/

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

    public void upgradeAppClicked(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.upgrade_alert);

        Button removeAds = dialog.findViewById(R.id.removeAds);
        if (isAdsPurchased) {
            removeAds.setVisibility(View.GONE);
        } else {
            removeAds.setVisibility(View.VISIBLE);
        }
        Button enableShatteredPuzzle = dialog.findViewById(R.id.enableShatteredPuzzle);
        if (isShatteredPuzzlesPurchased) {
            enableShatteredPuzzle.setVisibility(View.GONE);
        } else {
            enableShatteredPuzzle.setVisibility(View.VISIBLE);
        }

        enableShatteredPuzzle.setOnClickListener(v -> {
            dialog.dismiss();
            purchaseShatteredPuzzle();
        });

        removeAds.setOnClickListener(v -> {
            dialog.dismiss();
            purchaseAds();
        });
        dialog.show();
    }

    public void createPuzzleClicked(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, CameraGalleryActivity.class));
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
            }
        } else {
            startActivity(new Intent(this, CameraGalleryActivity.class));
        }
    }

    public void sharePuzzleClicked(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, LoadPuzzleActivity.class));
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
            }
        } else {
            startActivity(new Intent(this, LoadPuzzleActivity.class));
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESPONSE_PURCHASE) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
            if (resultCode == Activity.RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    if (sku.equals(ADS_SKU)) {
                        isAdsPurchased = true;
                        hideAd();
                    } else if (sku.equals(SHATTERED_PUZZLE_SKU)) {
                        isShatteredPuzzlesPurchased = true;
                    }

                    if (isAdsPurchased && isShatteredPuzzlesPurchased) {
                        upgradeApp.setVisibility(View.GONE);
                    } else {
                        upgradeApp.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    showToast("Failed to parse purchase data.");
                    e.printStackTrace();
                }
            } else {
                //showToast("Purchase operation is not completed.");
            }
        }
    }

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
            if (isAdsPurchased()) {
                isAdsPurchased = true;
                hideAd();
            } else {
                isAdsPurchased = false;
                displayAds();
            }

            isShatteredPuzzlesPurchased = isShatteredPuzzlePurchased();

            if (isAdsPurchased && isShatteredPuzzlesPurchased) {
                upgradeApp.setVisibility(View.GONE);
            } else {
                upgradeApp.setVisibility(View.VISIBLE);
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

    public void purchaseAds() {
        try {
            if (mService == null) {
                return;
            }
            Bundle bundle = mService.getBuyIntent(3, getPackageName(), ADS_SKU, "inapp", "");
            PendingIntent pendingIntent = bundle.getParcelable(RESPONSE_BUY_INTENT);
            if (bundle.getInt(RESPONSE_CODE) == BILLING_RESPONSE_RESULT_OK) {
                startIntentSenderForResult(pendingIntent.getIntentSender(), RESPONSE_PURCHASE, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Crashlytics.logException(e);
        }
    }

    public boolean isAdsPurchased() {
        Bundle activeSubs;
        try {
            if (mService == null) {
                return false;
            }
            activeSubs = mService.getPurchases(3, getPackageName(), "inapp", null);
            int response = activeSubs.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> ownedSkus = activeSubs.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                if (ownedSkus != null && ownedSkus.contains(ADS_SKU)) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isShatteredPuzzlePurchased() {
        Bundle activeSubs;
        try {
            if (mService == null) {
                return false;
            }
            activeSubs = mService.getPurchases(3, getPackageName(), "inapp", null);
            int response = activeSubs.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> ownedSkus = activeSubs.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                if (ownedSkus != null && ownedSkus.contains(SHATTERED_PUZZLE_SKU)) {
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

    public void showToast(final String message) {
        runOnUiThread(() -> Toast.makeText(DashboardActivity.this, message, Toast.LENGTH_LONG).show());
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
        bindIAPService();
    }

    public void purchaseShatteredPuzzle() {
        try {
            if (mService == null) {
                return;
            }
            Bundle bundle = mService.getBuyIntent(3, getPackageName(), SHATTERED_PUZZLE_SKU, "inapp", "");
            PendingIntent pendingIntent = bundle.getParcelable(RESPONSE_BUY_INTENT);
            if (bundle.getInt(RESPONSE_CODE) == BILLING_RESPONSE_RESULT_OK) {
                startIntentSenderForResult(pendingIntent.getIntentSender(), RESPONSE_PURCHASE, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Crashlytics.logException(e);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                if (grantResults.length >= 3 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "Please grant all permissions to proceed.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void reEnableAdsAndShatteredPuzzlesForPurchase(Activity activity, IInAppBillingService mService) {
        try {
            Bundle ownedItems = mService.getPurchases(3, activity.getPackageName(), IabHelper.ITEM_TYPE_INAPP, null);
            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                for (String purchaseData : purchaseDataList) {
                    JSONObject jo = new JSONObject(purchaseData);
                    if (SHATTERED_PUZZLE_SKU.equals(jo.getString("productId"))) {
                        mService.consumePurchase(3, activity.getPackageName(), jo.getString("purchaseToken"));
                    } else if (ADS_SKU.equals(jo.getString("productId"))) {
                        mService.consumePurchase(3, activity.getPackageName(), jo.getString("purchaseToken"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Crashlytics.logException(e);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.demoVideos:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(FP_BASE_URL + "fracturedphoto/videos.php"));
                startActivity(intent);
                break;
        }
    }
}
