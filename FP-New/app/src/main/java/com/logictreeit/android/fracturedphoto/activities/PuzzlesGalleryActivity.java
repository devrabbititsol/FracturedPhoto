package com.logictreeit.android.fracturedphoto.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
/*
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
 */
//import com.crashlytics.android.Crashlytics;
import com.android.vending.billing.IInAppBillingService;
import com.dran.fracturedphoto.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.logictreeit.android.fracturedphoto.adapters.PuzzlesGridAdapter;
import com.logictreeit.android.fracturedphoto.fcm.PuzzleCreditMetrics;
import com.logictreeit.android.fracturedphoto.inapp_billing_utils.IabHelper;
import com.logictreeit.android.fracturedphoto.models.GalleryPuzzle;
import com.logictreeit.android.fracturedphoto.utils.ApplicationConstants;
import com.logictreeit.android.fracturedphoto.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class PuzzlesGalleryActivity extends Activity implements AdapterView.OnItemClickListener, ApplicationConstants {

    private static final int PUZZLES_ALLOWED_PER_PURCHASE = 4;
    private RelativeLayout ll;
    private View mView;
    private RelativeLayout relativeLayout;
    private AdView adView, ad;
    private GridView gridview;
    private ArrayList<GalleryPuzzle> dataset = new ArrayList<GalleryPuzzle>();
    private PuzzlesGridAdapter adapter;
    private IInAppBillingService inAppBillingService;
    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final int BILLING_RESPONSE_RESULT_OK = 0;
    private static final String RESPONSE_BUY_INTENT = "BUY_INTENT";
    private static final int RESPONSE_PURCHASE = 1001;
    private GalleryPuzzle purchasingPuzzle;
    private final String LOAD_GALLERY_PUZZLES_API = FP_BASE_URL + "mobile_gallery.php";
    private PuzzleCreditMetrics metrics;
    public static final String ADMIN_PUZZLE_DOWNLOAD_URL = FP_BASE_URL + "puzzles/admin/";
    private String cachedDir;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_puzzles);

        ll = (RelativeLayout) findViewById(R.id.ll);
        mView = getLayoutInflater().inflate(R.layout.ad_layout, ll, true);
        relativeLayout = (RelativeLayout) mView.findViewById(R.id.relative_layout);
        ad = (AdView) mView.findViewById(R.id.adView);

        gridview = findViewById(R.id.gallery);
        adapter = new PuzzlesGridAdapter(this, dataset);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(this);

        new DownloadGalleryPuzzles().execute();

        if (Utils.isConnectionAvailable(PuzzlesGalleryActivity.this)) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signInAnonymously()
                    .addOnCompleteListener(PuzzlesGalleryActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                //showToast("Authentication Success - " + user.getUid());
                                new DownloadGalleryPuzzles().execute();
                            } else {
                                if (task.getException() != null) {
                                    task.getException().printStackTrace();
                                    showToast("Firebase Authentication Failed - " + task.getException().getMessage());
                                } else {
                                    showToast("Firebase Authentication Failed");
                                }
                            }
                        }
                    });
        } else {
            String message = "You are offline. Please connect to working internet.";
            Utils.NetworkMessage(PuzzlesGalleryActivity.this, message);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bindIAPService();
        //initializeBillingClient();
    }

    public void displayAds() {
        // Create an ad.
        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);

        adView.setAdUnitId(getString(R.string.banner_ad_unit_id));

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
            inAppBillingService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            inAppBillingService = IInAppBillingService.Stub.asInterface(service);
            if (isAdsPurchased()) {
                hideAd();
            } else {
                displayAds();
            }
        }
    };

    private void bindIAPService() {
        getApplicationContext().bindService(getExplicitIAPIntent(), mServiceConn, BIND_AUTO_CREATE);
    }

    private Intent getExplicitIAPIntent() {
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

    public boolean isAdsPurchased() {
        Bundle activeSubs;
        try {
            activeSubs = inAppBillingService.getPurchases(3, getPackageName(), "inapp", null);
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

    protected void showToast(final String msg) {
        runOnUiThread(() -> Toast.makeText(PuzzlesGalleryActivity.this, msg, Toast.LENGTH_LONG).show());
    }

    private void hideAd() {
        runOnUiThread(() -> {
            ad.setEnabled(false);
            ad.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.GONE);
        });
    }

    private void showBroadcastAlert(final GalleryPuzzle galleryPuzzle) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.broadcast_alert);

        Button selfBroadcast = dialog.findViewById(R.id.selfBroadcast);
        Button publicBroadcast = dialog.findViewById(R.id.publicBroadcast);

        selfBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isConnectionAvailable(PuzzlesGalleryActivity.this)){
                    Utils.initiatePushNotification(PuzzlesGalleryActivity.this, FP_BASE_URL + "mobile.php?filename="+galleryPuzzle.getPuzzle_name_with_extension(), galleryPuzzle.getPuzzle_image(), SELF_BROADCAST_TOPIC_NAME);
                } else {
                    showToast(getString(R.string.no_network));
                }
                dialog.dismiss();
            }
        });

        publicBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isConnectionAvailable(PuzzlesGalleryActivity.this)){
                    Utils.initiatePushNotification(PuzzlesGalleryActivity.this, FP_BASE_URL + "mobile.php?filename="+galleryPuzzle.getPuzzle_name_with_extension(), galleryPuzzle.getPuzzle_image(), PUBLIC_BROADCAST_TOPIC_NAME);
                } else {
                    showToast(getString(R.string.no_network));
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GalleryPuzzle galleryPuzzle = (GalleryPuzzle) adapter.getItem(position);
        if (galleryPuzzle != null) {
            if (IS_ADMIN_MODE) {
                showBroadcastAlert(galleryPuzzle);
            } else {
                goForDownloadPuzzle(galleryPuzzle);
            }
        }
    }

    public void goForDownloadPuzzle(GalleryPuzzle galleryPuzzle) {
        this.purchasingPuzzle = galleryPuzzle;
        if (galleryPuzzle.isPurchased()) {
            initiateDownload(galleryPuzzle);
        } else if (metrics != null) {
            int puzzles = metrics.getPuzzles_balance();
            if (puzzles > 0) {
                initiateDownload(purchasingPuzzle);
            } else {
                showLimitExceededAlert(PuzzlesGalleryActivity.this, "Get up to " + PUZZLES_ALLOWED_PER_PURCHASE + " puzzles for $0.99");
            }
        } else {
            showLimitExceededAlert(PuzzlesGalleryActivity.this, "Get up to " + PUZZLES_ALLOWED_PER_PURCHASE + " puzzles for $0.99");
        }
    }

    private void initiateDownload(GalleryPuzzle puzzle) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cachedDir = getExternalCacheDir().getAbsolutePath() + File.separator + getString(R.string.app_name) + File.separator;
        } else {
            cachedDir = getCacheDir().getAbsolutePath() + File.separator + getString(R.string.app_name) + File.separator;
        }
        if (!new File(cachedDir).exists()) {
            new File(cachedDir).mkdir();
        }

        if (Utils.isConnectionAvailable(PuzzlesGalleryActivity.this)) {
            new DownloadPuzzleTask(puzzle).execute(ADMIN_PUZZLE_DOWNLOAD_URL + puzzle.getPuzzle_name_with_extension());
        } else {
            String message = "You are offline. Please check your connectivity.";
            showToast(message);
        }
    }

    private class DownloadGalleryPuzzles extends AsyncTask<String, Void, String> {

        ProgressDialog progressBar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = ProgressDialog.show(PuzzlesGalleryActivity.this, "Loading Gallery", "Please wait...");
            progressBar.setCancelable(false);
            progressBar.setCanceledOnTouchOutside(false);
            progressBar.setIndeterminate(true);
            progressBar.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL downloadFileUrl = new URL(LOAD_GALLERY_PUZZLES_API);
                final URLConnection urlConnection = downloadFileUrl.openConnection();
                urlConnection.setConnectTimeout(15000);
                urlConnection.setReadTimeout(15000);
                final InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader isReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(isReader);
                StringBuilder sBuilder = new StringBuilder();
                String str;
                while ((str = reader.readLine()) != null) {
                    sBuilder.append(str);
                }
                return sBuilder.toString();
            } catch (Exception e) {
                showToast("Couldn't load puzzles. Something went wrong. " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (!isFinishing()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if (isDestroyed()) {
                        return;
                    }
                }

                if (progressBar != null && progressBar.isShowing()) {
                    progressBar.dismiss();
                }

                if (response != null) {
                    try {
                        dataset.clear();
                        Log.v("RESPOSNE", response);
                        if (response.trim().endsWith("script>")){
                            response = response.trim().replaceAll("<script" + ".*" + "script>", "");
                        }
                        JSONObject responseJSONObj = new JSONObject(response);
                        String preview_path = responseJSONObj.getString("preview_image_url");
                        String puzzle_url = responseJSONObj.getString("puzzle_url");
                        JSONArray galleryArray = responseJSONObj.getJSONArray("gallery");
                        for (int i = 0; i < galleryArray.length(); ++i) {
                            GalleryPuzzle puzzle = new GalleryPuzzle();

                            puzzle.setPuzzle_name(galleryArray.getString(i));
                            puzzle.setPuzzle_image(preview_path + puzzle.getPuzzle_name() + ".jpg");
                            puzzle.setPuzzle_url(puzzle_url + puzzle.getPuzzle_name_with_extension());
                            puzzle.setPurchased(false);
                            puzzle.setNew(false);
                            dataset.add(puzzle);
                        }

                        final String unqDeviceId = Utils.getUniqueDeviceId(PuzzlesGalleryActivity.this);
                        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                        databaseReference.child(NODE_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(unqDeviceId)) {
                                    PuzzleCreditMetrics tracker = dataSnapshot.child(unqDeviceId).getValue(PuzzleCreditMetrics.class);
                                    metrics = tracker;
                                    if (tracker != null) {
                                        ArrayList<String> purchasedPuzzles = tracker.getPurchased_puzzles();
                                        if (purchasedPuzzles != null) {
                                            for (GalleryPuzzle puzzle : dataset) {
                                                try {
                                                    puzzle.setPurchased(purchasedPuzzles.contains(puzzle.getPuzzle_url().split("filename=")[1]));
                                                } catch (Exception e){
                                                    e.printStackTrace();
                                                }

                                                if (getIntent().getBooleanExtra(Extras_Keys.FROM_PUSH, false)) {
                                                    try {
                                                        puzzle.setNew(getIntent().getStringExtra(Extras_Keys.PUZZLE_URL).split("filename=")[1].equals(puzzle.getPuzzle_url().split("filename=")[1]));
                                                    } catch (Exception e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError e) {
                                e.toException().printStackTrace();
                            }
                        });
                    } catch (JSONException e) {
                        showToast("Unexpected response received.. Please try again later.");
                        e.printStackTrace();
                        finish();
                    } catch (Exception e) {
                        showToast("Something went wrong.. Please try again later.");
                        e.printStackTrace();
                        finish();
                    }
                }
            }
        }
    }

    public void showLimitExceededAlert(Activity act, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(act);
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Purchase", (dialog, id) -> {
                    dialog.dismiss();
                    purchasePuzzle();
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void purchasePuzzle() {
        try {
            if (inAppBillingService == null) {
                return;
            }
            Bundle bundle = inAppBillingService.getBuyIntent(3, getPackageName(), PUZZLE_DOWNLOADS_SKU, "inapp", "");
            PendingIntent pendingIntent = bundle.getParcelable(RESPONSE_BUY_INTENT);
            if (bundle.getInt(RESPONSE_CODE) == BILLING_RESPONSE_RESULT_OK) {
                startIntentSenderForResult(pendingIntent.getIntentSender(), RESPONSE_PURCHASE, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESPONSE_PURCHASE) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
            if (resultCode == Activity.RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    //Once product is purchased successfully, then we have to make it consumable to make it purchase again and again. Otherwise we can't purchase it again.
                    consumePurchase(PuzzlesGalleryActivity.this, inAppBillingService);

                    final String unqDeviceId = Utils.getUniqueDeviceId(PuzzlesGalleryActivity.this);
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    databaseReference.child(NODE_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (purchasingPuzzle == null) {
                                startActivity(getIntent());
                                finish();
                                return;
                            }
                            PuzzleCreditMetrics tracker;
                            if (dataSnapshot.hasChild(unqDeviceId)) {
                                tracker = dataSnapshot.child(unqDeviceId).getValue(PuzzleCreditMetrics.class);
                            } else {
                                tracker = new PuzzleCreditMetrics();
                            }
                            databaseReference.child(NODE_NAME).child(unqDeviceId).setValue(tracker).addOnSuccessListener(aVoid -> {
                                tracker.setPuzzles_balance(PUZZLES_ALLOWED_PER_PURCHASE);
                                metrics = tracker;
                                initiateDownload(purchasingPuzzle);
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                    showToast("Something went wrong - " + e.getMessage());
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError e) {
                            e.toException().printStackTrace();
                            //Crashlytics.logException(e.toException());
                        }
                    });
                } catch (JSONException e) {
                    showToast("Failed to purchase.");
                    e.printStackTrace();
                    finish();
                }
            } else {
                //showToast("Purchase operation is not completed.");
            }
        } else {
            //showToast("Cancelled purchase operation.");
        }
    }

    public void consumePurchase(Activity activity, IInAppBillingService mService) {
        try {
            Bundle ownedItems = mService.getPurchases(3, activity.getPackageName(), IabHelper.ITEM_TYPE_INAPP, null);
            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                for (String purchaseData : purchaseDataList) {
                    JSONObject jo = new JSONObject(purchaseData);
                    if (PUZZLE_DOWNLOADS_SKU.equals(jo.getString("productId"))) {
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
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("metrics", metrics);
        outState.putSerializable("gallery_puzzle", purchasingPuzzle);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        purchasingPuzzle = (GalleryPuzzle) savedInstanceState.getSerializable("gallery_puzzle");
        metrics = (PuzzleCreditMetrics) savedInstanceState.getSerializable("metrics");
        super.onRestoreInstanceState(savedInstanceState);
    }

    private class DownloadPuzzleTask extends AsyncTask<String, Void, String> {
        GalleryPuzzle puzzle;
        ProgressDialog pBar;

        DownloadPuzzleTask(GalleryPuzzle puzzle){
            this.puzzle = puzzle;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pBar = ProgressDialog.show(PuzzlesGalleryActivity.this, "Downloading", "Please wait...");
            pBar.setCancelable(true);
            pBar.setCanceledOnTouchOutside(false);
            pBar.setIndeterminate(true);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String downloadedFilePath = cachedDir + SharedPuzzleReceiverActivity.ATTACHMENT_NAME;
                File downloadedFile = new File(downloadedFilePath);

                URL downloadFileUrl = new URL(params[0]);
                final URLConnection urlConnection = downloadFileUrl.openConnection();

                final FileOutputStream fileOutputStream = new FileOutputStream(downloadedFile);
                final byte buffer[] = new byte[1024];

                final InputStream inputStream = urlConnection.getInputStream();
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, len);
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                return downloadedFilePath;
            } catch (Exception e) {
                showToast("Couldn't download. Something went wrong. " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String downloadedAttachmentPath) {
            super.onPostExecute(downloadedAttachmentPath);
            if (downloadedAttachmentPath != null) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                final String unqDeviceId = Utils.getUniqueDeviceId(PuzzlesGalleryActivity.this);
                databaseReference.child(NODE_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (pBar != null && pBar.isShowing()) {
                            pBar.dismiss();
                        }

                        if (dataSnapshot.hasChild(unqDeviceId)) {
                            PuzzleCreditMetrics tracker = dataSnapshot.child(unqDeviceId).getValue(PuzzleCreditMetrics.class);
                            if (tracker != null) {
                                int puzzles = tracker.getPuzzles_balance();

                                ArrayList<String> purchasedPuzzles = tracker.getPurchased_puzzles();
                                if (purchasedPuzzles == null) {
                                    purchasedPuzzles = new ArrayList<>();
                                }

                                if (!purchasedPuzzles.contains(puzzle.getPuzzle_name_with_extension())) {
                                    if (puzzles <= 0) {
                                        showToast("You have exceeded limit. Please purchase again");
                                        return;
                                    }
                                    purchasedPuzzles.add(puzzle.getPuzzle_name_with_extension());
                                    tracker.setPuzzles_balance(puzzles - 1);
                                    tracker.setPurchased_puzzles(purchasedPuzzles);

                                    //Crashlytics.logException(e);
                                    databaseReference.child(NODE_NAME).child(unqDeviceId).setValue(tracker).addOnSuccessListener(aVoid -> {
                                        SharedPuzzleReceiverActivity.readAndExtractAttachment(PuzzlesGalleryActivity.this, downloadedAttachmentPath);
                                    }).addOnFailureListener(Throwable::printStackTrace);
                                } else {
                                    SharedPuzzleReceiverActivity.readAndExtractAttachment(PuzzlesGalleryActivity.this, downloadedAttachmentPath);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        e.toException().printStackTrace();
                        if (pBar != null && pBar.isShowing()) {
                            pBar.dismiss();
                        }
                    }
                });
            } else {
                if (pBar != null && pBar.isShowing()) {
                    pBar.dismiss();
                }
                showToast("Something went wrong.\nPlease try again later");
            }
        }
    }

    /*private BillingClient billingClient;
    public void initializeBillingClient() {
        billingClient = BillingClient
                .newBuilder(this)
                .enablePendingPurchases()
                .setListener(this)
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    handleAdsVisibility(billingClient);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                showToast("Something went wrong, Please try again later.");
                // Logic from ServiceConnection.onServiceDisconnected should be moved here.
            }
        });
    }

    private void handleAdsVisibility(BillingClient billingClient) {
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, new PurchaseHistoryResponseListener() {
            @Override
            public void onPurchaseHistoryResponse(@NonNull BillingResult billingResult, @Nullable List<PurchaseHistoryRecord> purchasesHistoryResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (purchasesHistoryResult != null && !purchasesHistoryResult.isEmpty()) {
                        for (PurchaseHistoryRecord purchaseHistory : purchasesHistoryResult) {
                            if (purchaseHistory.getSku().equals(ADS_SKU)) {
                                hideAd();
                                break;
                            } else {
                                displayAds();
                            }
                        }
                    } else {
                        displayAds();
                    }
                }
            }
        });*/

        /*
        queryPurchases will use cache of Playstore app
        queryPurchaseHistoryAsync will make an API call to get latest info

        Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        if (purchasesResult != null) {
            List<Purchase> purchasesList = purchasesResult.getPurchasesList();
            if (purchasesList == null) {
                return;
            }
            if (!purchasesList.isEmpty()) {
                for (Purchase purchase : purchasesList) {
                    if (purchase.getSku().equals(ADS_SKU)) {
                        hideAd();
                        break;
                    } else {
                        displayAds();
                    }
                }
            } else {
                displayAds();
            }
        }*/
    /*}

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {

    }*/
}