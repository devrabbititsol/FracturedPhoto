package com.logictreeit.android.fracturedphoto.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.View;
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
import com.logictreeit.android.fracturedphoto.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
//import io.fabric.sdk.android.Fabric;

public class CameraGalleryActivity extends Activity implements ApplicationConstants {

    private static final int ACTIVITY_CONSTANT = 0;
    private static final int PICK_PHOTO_REQUEST_CODE = ACTIVITY_CONSTANT + 1;
    private static final int CLICK_PHOTO_REQUEST_CODE = ACTIVITY_CONSTANT + 2;
    private String clickedPhotoPath;
    private String downloadedPhotoPath;
    private String downloadedPhotoUri;
    private boolean isDuplicateClick;
    private LinearLayout ll;
    private View mView;
    public RelativeLayout relativeLayout;
    public AdView adView, ad;
    public final Handler handler = new Handler();
    private static final int RESPONSE_PURCHASE = 1001;
    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final int BILLING_RESPONSE_RESULT_OK = 0;
    private static final String RESPONSE_BUY_INTENT = "BUY_INTENT";
    private IInAppBillingService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());

        setContentView(R.layout.camera_gallery);

        ll = (LinearLayout) findViewById(R.id.ll);
        mView = getLayoutInflater().inflate(R.layout.ad_layout, ll, true);
        relativeLayout = (RelativeLayout) mView.findViewById(R.id.relative_layout);
        ad = (AdView) mView.findViewById(R.id.adView);
    }

    public void openCamera(View v) {
        if (!isDuplicateClick) {
            isDuplicateClick = true;
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(clickedPhotoPath = Utils.getClickedPhotoPath(getApplicationContext()))));
            startActivityForResult(Intent.createChooser(intent, "Select App"), CLICK_PHOTO_REQUEST_CODE);
        }
    }

    public void openGallery(View v) {
        if (!isDuplicateClick) {
            isDuplicateClick = true;
            clickedPhotoPath = null;
            ///sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + android.os.Environment.getExternalStorageDirectory())));
            startActivityForResult(Intent.createChooser(Utils.openGalleryIntent(), "Select App"), PICK_PHOTO_REQUEST_CODE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isDuplicateClick = false;
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_PHOTO_REQUEST_CODE) {
                Uri imageUri = data.getData();
                String imageFilePath = Utils.getRealPathFromURI(getApplicationContext(), imageUri);
                System.err.println("imageUri : " + imageUri);
                System.err.println("imageFilePath : " + imageFilePath);
                if (imageUri != null && imageFilePath != null && !imageFilePath.startsWith("http://") && !imageFilePath.startsWith("https://")) {
                    //Picked one from purely local photos
                    launchNextActivity(imageUri, imageFilePath);
                } else if (imageUri != null) { //picked from EXTERNAL PHOTOS
                    String imageUrl = Utils.getPathOfExternalPhotoFromUri(getApplicationContext(), imageUri);
                    if (imageUrl != null && imageUrl.startsWith("http")) {
                        if (Utils.isConnectionAvailable(this)) {
                            downloadedPhotoUri = imageUri.toString();
                            Picasso.with(this).load(imageUrl).into(imageDownloader);
                        } else {
                            showToast("Network is not available to download selected photo");
                        }
                    } else {
                        showToast("Unable to pick this external photo");
                    }
                } else {
                    showToast("Unable to pick this external photo");
                }
            } else if (requestCode == CLICK_PHOTO_REQUEST_CODE) {
                String imageFilePath = clickedPhotoPath;
                Uri imageUri = Utils.getImageUriFromPath(getApplicationContext(), imageFilePath);
                System.err.println("imageUri : " + imageUri);
                System.err.println("imageFilePath : " + imageFilePath);
                //Utils.deleteLastPhotoTaken(getApplicationContext());
                launchNextActivity(imageUri, imageFilePath);
            }
        }
    }

    private Target imageDownloader = new Target() {
        private ProgressDialog donwloadingProgressWheel;

        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            File file = new File(downloadedPhotoPath = Utils.getClickedPhotoPath(getApplicationContext()));
            try {
                FileOutputStream ostream = new FileOutputStream(file);
                bitmap.compress(CompressFormat.JPEG, 100, ostream);
                ostream.close();

                new Handler().postDelayed(() -> {
                    if (donwloadingProgressWheel != null && donwloadingProgressWheel.isShowing()) {
                        donwloadingProgressWheel.dismiss();
                    }
                    launchNextActivity(Uri.parse(downloadedPhotoUri), downloadedPhotoPath);
                }, 3000);

            } catch (Exception e) {
                e.printStackTrace();
                showToast("Not able to download external photo");
                if (donwloadingProgressWheel != null && donwloadingProgressWheel.isShowing()) {
                    donwloadingProgressWheel.dismiss();
                }
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            if (donwloadingProgressWheel != null && donwloadingProgressWheel.isShowing()) {
                donwloadingProgressWheel.dismiss();
            }
            showToast("Not able to download external photo");
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            donwloadingProgressWheel = ProgressDialog.show(CameraGalleryActivity.this, "Downloading", "Please wait...");
            donwloadingProgressWheel.setCancelable(false);
            donwloadingProgressWheel.setCanceledOnTouchOutside(false);
            donwloadingProgressWheel.setIndeterminate(true);
        }
    };

    private void launchNextActivity(Uri imageUri, String imageFilePath) {
        if (imageFilePath == null || imageUri == null) {
            showToast("Something went wrong. Please try again later.");
            return;
        }

        int rotation = Utils.getPhotoOrientation(imageFilePath);
        ///showToast("Orientation : " + rotation);
        System.err.println("Orientation : " + rotation);

        Intent intent = new Intent(this, ChoosePuzzleTypeActivity.class);
        intent.putExtra(Extras_Keys.PHOTO_PATH, imageFilePath);
        intent.putExtra(Extras_Keys.PHOTO_ORIENTATION, rotation);
        intent.putExtra(Extras_Keys.PHOTO_URI, imageUri.toString());
        this.startActivity(intent);
    }

    protected void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CameraGalleryActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (clickedPhotoPath != null) {
            outState.putString(Extras_Keys.PHOTO_PATH, clickedPhotoPath);
        }
        outState.putString("downloadedPhotoPath", downloadedPhotoPath);
        outState.putString("downloadedPhotoUri", downloadedPhotoUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        clickedPhotoPath = savedInstanceState.getString(Extras_Keys.PHOTO_PATH);
        downloadedPhotoPath = savedInstanceState.getString("downloadedPhotoPath");
        downloadedPhotoUri = savedInstanceState.getString("downloadedPhotoUri");
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

        //	AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();

        // Start loading the ad in the background.
        ad.loadAd(adRequest);
        ad.setVisibility(View.VISIBLE);
    }

    public void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
    }

    private void hideAd() {
        runOnUiThread(() -> {
            ad.setEnabled(false);
            ad.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.GONE);
        });
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
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void showMessage(String message) {
        Toast.makeText(CameraGalleryActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
