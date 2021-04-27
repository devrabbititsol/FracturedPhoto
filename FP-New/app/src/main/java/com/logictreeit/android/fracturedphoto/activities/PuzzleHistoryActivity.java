package com.logictreeit.android.fracturedphoto.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cursoradapter.widget.SimpleCursorAdapter;

import com.android.vending.billing.IInAppBillingService;
//import com.crashlytics.android.Crashlytics;
import com.dran.fracturedphoto.R;
import com.dran.fracturedphoto.R.string;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.logictreeit.android.fracturedphoto.db.FracturePhotoDB;
import com.logictreeit.android.fracturedphoto.db.FracturePhotoDBModel;
import com.logictreeit.android.fracturedphoto.db.SampleCursorLoader;
import com.logictreeit.android.fracturedphoto.utils.ApplicationConstants;
import com.logictreeit.android.fracturedphoto.utils.ApplicationConstants.Extras_Keys;
import com.logictreeit.android.fracturedphoto.utils.PuzzleType;
import com.logictreeit.android.fracturedphoto.utils.Utils;

//import io.fabric.sdk.android.Fabric;

public class PuzzleHistoryActivity extends Activity implements OnItemClickListener, ApplicationConstants {

    private static final int ACTION_ID = 0;
    private static final int DELETE_ACTION_ID = ACTION_ID + 1;
    private static final int SOLVE_ACTION_ID = ACTION_ID + 2;
    private ListView listView;
    private SimpleCursorAdapter adapter;
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
        setContentView(R.layout.puzzle_history);
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        //registerForContextMenu(listView);
        FracturePhotoDB db = new FracturePhotoDB(getApplicationContext());
        db.openDB();
        Cursor cursor = new SampleCursorLoader(getApplicationContext(), db, "loadPuzzleHistory", "").loadInBackground();
        if (cursor != null && cursor.getCount() >= 1) {
            adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.history_list_row, cursor, new String[]{FracturePhotoDBModel.COL_PZLNAME, FracturePhotoDBModel.COL_DATE, FracturePhotoDBModel.COL_TIME}, new int[]{R.id.pzlName, R.id.dateTextViewListRow, R.id.timeTextViewListRow}, 1) {
                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    View row = super.getView(position, convertView, parent);
                    ImageView iv = (ImageView) row.findViewById(R.id.deletePuzzle);
                    iv.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Cursor cursor = (Cursor) getItem(position);
                            showDeleteAlert(cursor);
                        }
                    });
                    Utils.applyFontForWholeView(getApplicationContext(), row, "Bold");
                    return row;
                }
            };
            listView.setAdapter(adapter);
        } else {
            showToast("No Puzzles Found");
        }
        db.closeDB();

        ll = (LinearLayout) findViewById(R.id.ll);
        mView = getLayoutInflater().inflate(R.layout.ad_layout, ll, true);
        relativeLayout = (RelativeLayout) mView.findViewById(R.id.relative_layout);
        ad = (AdView) mView.findViewById(R.id.adView);
    }

    @Override
    public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
        Cursor c = ((SimpleCursorAdapter) av.getAdapter()).getCursor();
        launchNextActivity(c);
    }

    private void launchNextActivity(Cursor c) {
        String puzzleType = c.getString(c.getColumnIndexOrThrow(FracturePhotoDBModel.COL_TYPE_OF_PUZZLE));
        String filePath = c.getString(c.getColumnIndexOrThrow(FracturePhotoDBModel.COL_PHOTOPATH));
        String pzlName = c.getString(c.getColumnIndexOrThrow(FracturePhotoDBModel.COL_PZLNAME));
        String pzlRowId = c.getString(c.getColumnIndexOrThrow(FracturePhotoDBModel.COL_ID1));

        if (puzzleType.equals(PuzzleType.SQUARE_PUZZLE.getPuzzleType())) {
            int numOfPieces = Integer.parseInt(c.getString(c.getColumnIndexOrThrow(FracturePhotoDBModel.COL_NUM_OF_PIECES)));

            Intent intent = new Intent(this, PlaySquarePuzzleActivity.class);
            intent.putExtra(Extras_Keys.PHOTO_PATH, filePath);
            intent.putExtra(Extras_Keys.NUM_OF_PIECES, numOfPieces);
            intent.putExtra(Extras_Keys.PUZZLE_NAME, pzlName);
            intent.putExtra(Extras_Keys.PUZZLE_HISTORY_ROW_ID_IN_DB, pzlRowId);
            intent.putExtra(Extras_Keys.IS_CREATE_PUZZLE_MODE, false);
            intent.putExtra(Extras_Keys.PHOTO_ORIENTATION, Utils.getPhotoOrientation(filePath));
            startActivity(intent);

        } else if (puzzleType.equals(PuzzleType.SHATTERED_PUZZLE.getPuzzleType())) {

            Intent intent = new Intent(this, PlayShatteredPuzzleActivity.class);
            intent.putExtra(Extras_Keys.PHOTO_PATH, filePath);
            intent.putExtra(Extras_Keys.PUZZLE_NAME, pzlName);
            intent.putExtra(Extras_Keys.PUZZLE_HISTORY_ROW_ID_IN_DB, pzlRowId);
            intent.putExtra(Extras_Keys.IS_CREATE_PUZZLE_MODE, false);
            intent.putExtra(Extras_Keys.PHOTO_ORIENTATION, Utils.getPhotoOrientation(filePath));
            startActivity(intent);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select Action");
        menu.add(Menu.NONE, SOLVE_ACTION_ID, Menu.NONE, "Solve");
        menu.add(Menu.NONE, DELETE_ACTION_ID, Menu.NONE, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Cursor cursor = (Cursor) adapter.getItem(info.position);
        switch (item.getItemId()) {
            case DELETE_ACTION_ID:
                deletePuzzle(cursor);
                return super.onContextItemSelected(item);
            case SOLVE_ACTION_ID:
                launchNextActivity(cursor);
                break;
            default:
                break;
        }
        return true;
    }

    private void deletePuzzle(Cursor cursor) {
        final String pzlRowId = cursor.getString(cursor.getColumnIndexOrThrow(FracturePhotoDBModel.COL_ID1));
        final String puzzleType = cursor.getString(cursor.getColumnIndexOrThrow(FracturePhotoDBModel.COL_TYPE_OF_PUZZLE));
        final ProgressDialog deleteProgressWheel = ProgressDialog.show(PuzzleHistoryActivity.this, "Deleting Puzzle", "Please wait...");
        deleteProgressWheel.setCancelable(false);
        deleteProgressWheel.setCanceledOnTouchOutside(false);
        deleteProgressWheel.setIndeterminate(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                //delete the puzzle from DB
                final FracturePhotoDB db = new FracturePhotoDB(getApplicationContext());
                db.openDB();
                db.deletePuzzleHistoryWithId(pzlRowId);
                int affected = 0;
                if (puzzleType.equals(PuzzleType.SQUARE_PUZZLE.getPuzzleType())) {
                    affected = db.deleteSquareTilesAssociatedWithBaseRowId(pzlRowId);
                } else if (puzzleType.equals(PuzzleType.SHATTERED_PUZZLE.getPuzzleType())) {
                    affected = db.deleteShatteredTilesAssociatedWithBaseRowId(pzlRowId);
                }
                System.err.println("NUM of Rows DeLEted = " + affected);
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.swapCursor(new SampleCursorLoader(getApplicationContext(), db, "loadPuzzleHistory", "").loadInBackground());
                        db.closeDB();
                        if (deleteProgressWheel != null && deleteProgressWheel.isShowing()) {
                            deleteProgressWheel.dismiss();
                        }
                    }
                });
            }
        }).start();
    }

    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast t = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        FracturePhotoDB db = new FracturePhotoDB(getApplicationContext());
        db.openDB();
        if (adapter != null) {
            Cursor cursor = new SampleCursorLoader(getApplicationContext(), db, "loadPuzzleHistory", "").loadInBackground();
            if (cursor != null) {
                adapter.swapCursor(cursor);
                if (cursor.getCount() == 0) {
                    showToast("No Puzzles Found");
                }
            }
        }
        db.closeDB();
    }

    private void showDeleteAlert(final Cursor cursor) {
        Builder closeAlertBox = new AlertDialog.Builder(PuzzleHistoryActivity.this);
        closeAlertBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                deletePuzzle(cursor);
            }
        });

        closeAlertBox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        closeAlertBox.setTitle("Confirm");
        closeAlertBox.setMessage("Are you sure you want to delete this puzzle?");
        closeAlertBox.setCancelable(false);
        closeAlertBox.show();
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
        Toast.makeText(PuzzleHistoryActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
