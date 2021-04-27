package com.logictreeit.android.fracturedphoto.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
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

public class ChoosePuzzleTypeActivity extends Activity implements ApplicationConstants {
    private String imageFilePath;
    private int rotation;
    private String imageUri;
    LinearLayout ll;
    View mView;
    public RelativeLayout relativeLayout;
    public AdView adView, ad;
    ImageView square_image, shattered_image;
    public final Handler handler = new Handler();
    private static final int SHATTERED_PUZZLES_RESPONSE_PURCHASE = 1002;
    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final int BILLING_RESPONSE_RESULT_OK = 0;
    private static final String RESPONSE_BUY_INTENT = "BUY_INTENT";
    private IInAppBillingService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // Fabric.with(this, new Crashlytics());
        setContentView(R.layout.choose_puzzle_type);

        Intent i = getIntent();
        imageFilePath = i.getStringExtra(Extras_Keys.PHOTO_PATH);
        rotation = i.getIntExtra(Extras_Keys.PHOTO_ORIENTATION, 0);
        imageUri = i.getStringExtra(Extras_Keys.PHOTO_URI);

        ll = (LinearLayout) findViewById(R.id.ll);
        mView = getLayoutInflater().inflate(R.layout.ad_layout, ll, true);
        relativeLayout = (RelativeLayout) mView.findViewById(R.id.relative_layout);
        ad = (AdView) mView.findViewById(R.id.adView);
        square_image = (ImageView) findViewById(R.id.square);
        shattered_image = (ImageView) findViewById(R.id.shattered);
        square_image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumOfPiecesAlert();
            }
        });
        shattered_image.setOnClickListener(v -> {
            if (isShatteredPuzzlePurchased()) {
                playShattered();
            } else {
                showPaymentAlert();
            }
        });
    }

    private void showNumOfPiecesAlert() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.pieces_alert);

        Button b30 = (Button) dialog.findViewById(R.id.p30);
        Button b20 = (Button) dialog.findViewById(R.id.p20);
        Button b12 = (Button) dialog.findViewById(R.id.p12);

        b30.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSquare(30);
                dialog.dismiss();
            }
        });

        b20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSquare(20);
                dialog.dismiss();
            }
        });

        b12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSquare(12);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void playSquare(int numberOfPieces) {
        Intent intent = new Intent(ChoosePuzzleTypeActivity.this, SquarePuzzleLandingActivity.class);
        intent.putExtra(Extras_Keys.PHOTO_PATH, imageFilePath);
        intent.putExtra(Extras_Keys.PHOTO_ORIENTATION, rotation);
        intent.putExtra(Extras_Keys.PHOTO_URI, imageUri);
        intent.putExtra(Extras_Keys.NUM_OF_PIECES, numberOfPieces);
        startActivity(intent);
    }

    public void playShattered() {
        Intent intent = new Intent(ChoosePuzzleTypeActivity.this, ShatteredPuzzleLandingActivity.class);
        intent.putExtra(Extras_Keys.PHOTO_PATH, imageFilePath);
        intent.putExtra(Extras_Keys.PHOTO_ORIENTATION, rotation);
        intent.putExtra(Extras_Keys.PHOTO_URI, imageUri);
        startActivity(intent);
    }

    public void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
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
            if (isAdsPurchased()) {
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

    public boolean isAdsPurchased() {
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

    public void showMessage(String message) {
        Toast.makeText(ChoosePuzzleTypeActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void hideAd() {
        runOnUiThread(() -> {
            ad.setEnabled(false);
            ad.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.GONE);
        });
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void subscribeForCreateShatteredPuzzle() {
        try {
            if (mService == null) {
                return;
            }
            Bundle bundle = mService.getBuyIntent(3, getPackageName(), SHATTERED_PUZZLE_SKU, "inapp", "");
            PendingIntent pendingIntent = bundle.getParcelable(RESPONSE_BUY_INTENT);
            if (bundle.getInt(RESPONSE_CODE) == BILLING_RESPONSE_RESULT_OK) {
                startIntentSenderForResult(pendingIntent.getIntentSender(), SHATTERED_PUZZLES_RESPONSE_PURCHASE, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    public void showPaymentAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(ChoosePuzzleTypeActivity.this).create();
        alertDialog.setTitle("Fractured Photo");
        alertDialog.setMessage("Enabling this feature requires a onetime charge of $0.99");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                subscribeForCreateShatteredPuzzle();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (IS_ADMIN_MODE) {
                    playShattered();
                }
            }
        });
        alertDialog.show();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SHATTERED_PUZZLES_RESPONSE_PURCHASE:
                int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
                String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
                String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        JSONObject jo = new JSONObject(purchaseData);
                        String sku = jo.getString("productId");
                        playShattered();
                    } catch (JSONException e) {
                        showMessage("Failed to parse purchase data.");
                        e.printStackTrace();
                    }
                } else {
                    //showToast("Purchase operation is not completed.");
                }
                break;
        }
    }

    protected void showToast(final String msg) {
        runOnUiThread(() -> Toast.makeText(ChoosePuzzleTypeActivity.this, msg, Toast.LENGTH_LONG).show());
    }
}
