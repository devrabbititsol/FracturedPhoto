package com.logictreeit.android.fracturedphoto.activities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Telephony;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
//import com.crashlytics.android.Crashlytics;
import com.dran.fracturedphoto.R;
import com.dran.fracturedphoto.R.string;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.logictreeit.android.fracturedphoto.adapters.SharePopupAdapter;
import com.logictreeit.android.fracturedphoto.adapters.SquarePuzzleGridAdapter;
import com.logictreeit.android.fracturedphoto.app.FracturePhotoApplication;
import com.logictreeit.android.fracturedphoto.db.FracturePhotoDB;
import com.logictreeit.android.fracturedphoto.db.FracturePhotoDBModel;
import com.logictreeit.android.fracturedphoto.db.SampleCursorLoader;
import com.logictreeit.android.fracturedphoto.helpers.AppsInfo;
import com.logictreeit.android.fracturedphoto.helpers.BitmapHelper;
import com.logictreeit.android.fracturedphoto.helpers.ZipHelper;
import com.logictreeit.android.fracturedphoto.listeners.OrientationManager;
import com.logictreeit.android.fracturedphoto.models.SquareTile;
import com.logictreeit.android.fracturedphoto.utils.ApplicationConstants;
import com.logictreeit.android.fracturedphoto.utils.BaseDialogFragment;
import com.logictreeit.android.fracturedphoto.utils.OkDialog;
import com.logictreeit.android.fracturedphoto.utils.FP_PrefsManager;
import com.logictreeit.android.fracturedphoto.utils.PuzzleType;
import com.logictreeit.android.fracturedphoto.utils.Utils;
import com.logictreeit.android.fracturedphoto.network.NetworkCallBack;
import com.logictreeit.android.fracturedphoto.network.RequestHandler;
//import io.fabric.sdk.android.Fabric;

public class PlaySquarePuzzleActivity extends Activity implements
        ApplicationConstants, OnClickListener, NetworkCallBack, OrientationManager.OrientationListener {

    private ImageView saveButton, previewButton, sharePuzzleButton, turnLeftImage, turnRightImage, fpLogo;
    private GridView gridView;
    private String photoPath, rootPath, pathOfDuplicateSharedPhoto;
    private int numOfPieces;
    private Bitmap photoBitmap;
    private int numOfRows, numOfColsInGrid, scale;
    private ArrayList<Bitmap> bitmapPiecesList = new ArrayList<Bitmap>();
    private ArrayList<SquareTile> tilesList = new ArrayList<SquareTile>();
    private SquarePuzzleGridAdapter adapter;
    private boolean isGridTouched;
    private int touchedItemPos;
    private int screenWidth;
    private int bmpWidth;
    private int bmpHeight;
    private String imagePathToShare;
    private File cachedPicturesFolder;
    private File cacheFolder;
    private String metaDataFilePath;
    private String puzzleName;
    private boolean isCreatePuzzleMode;
    private boolean isSharedPuzzle;
    private ProgressDialog progressBar;
    private boolean isActivityStopped;
    private boolean isDuplicateShareClick;
    private static final Random RAN = new Random();
    private static final int SHARE_PUZZLE_REQUEST_CODE = 1234;
    private String uploadPuzzleUrl = FP_BASE_URL + "upload.php";
    private String adminPuzzleUploadUrl = FP_BASE_URL + "admin_upload.php";
    private String fileLink, uploadFileName, deviceid, responseStr, uploadFbFileName, fbPhotoPath;
    private File myDir;
    RelativeLayout ll;
    View mView;
    public RelativeLayout relativeLayout;
    public AdView adView, ad;
    public final Handler handler = new Handler();
    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final int BILLING_RESPONSE_RESULT_OK = 0;
    private static final String RESPONSE_BUY_INTENT = "BUY_INTENT";
    private static final int SHARE_PUZZLE_REQUEST_CODE_INSTAGRAM = 9999;
    private IInAppBillingService mService;
    protected long savedSharedPuzzleRowId;
    private ProgressDialog dialog;
    private CallbackManager mCallbackManager;
    private OrientationManager orientationManager;
    private LinearLayout gridBGLayout;
    private int screenHeight;
    private com.logictreeit.android.fracturedphoto.custom_ui.RotateLayout congratsParentLayout;
    public static int rotateTo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        setContentView(R.layout.play_square_puzzle);
        ll = (RelativeLayout) findViewById(R.id.ll);
        mView = getLayoutInflater().inflate(R.layout.ad_layout, ll, true);
        relativeLayout = (RelativeLayout) mView.findViewById(R.id.relative_layout);
        ad = (AdView) mView.findViewById(R.id.adView);
        saveButton = (ImageView) findViewById(R.id.saveButton);
        sharePuzzleButton = (ImageView) findViewById(R.id.sharePuzzleButton);
        gridView = (GridView) findViewById(R.id.grid);
        gridBGLayout = (LinearLayout) findViewById(R.id.gridBGLayout);
        gridView.setDrawingCacheEnabled(true);
        turnRightImage = (ImageView) findViewById(R.id.turnRight);
        turnLeftImage = (ImageView) findViewById(R.id.turnLeft);
        rootPath = SDCARD_PATH + File.separator + getString(string.app_name) + "_SharedPuzzles" + File.separator;
        turnLeftImage.setOnClickListener(this);
        turnRightImage.setOnClickListener(this);

        previewButton = (ImageView) findViewById(R.id.preview);
        previewButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        sharePuzzleButton.setOnClickListener(this);
        fpLogo = (ImageView) findViewById(R.id.fpLogo);

        deviceid = Utils.getUniqueDeviceId(this);
        System.out.print("Device id------- " + deviceid);

        doInitialSetup(savedInstanceState);
    }

    private void doInitialSetup(Bundle savedInstanceState) {

        Intent intent = getIntent();
        isSharedPuzzle = intent.getBooleanExtra(Extras_Keys.IS_SHARED_PUZZLE, false);
        puzzleName = intent.getStringExtra(Extras_Keys.PUZZLE_NAME).replace("\\.", "");

        if (!isSharedPuzzle) {
            photoPath = intent.getStringExtra(Extras_Keys.PHOTO_PATH);
            numOfPieces = intent.getIntExtra(Extras_Keys.NUM_OF_PIECES, 12);
            isCreatePuzzleMode = intent.getBooleanExtra(Extras_Keys.IS_CREATE_PUZZLE_MODE, true);

            photoBitmap = BitmapHelper.decodeBitmapFromPath(photoPath, 640, 480, getIntent().getIntExtra(Extras_Keys.PHOTO_ORIENTATION, 0));

            if (photoBitmap != null) {
                if (isCreatePuzzleMode) {
                    // we are in CREATE PUZZLE MODE
                    bmpWidth = photoBitmap.getWidth();
                    bmpHeight = photoBitmap.getHeight();
                    screenWidth = getResources().getDisplayMetrics().widthPixels - (int) Utils.convertDpToPixels(34, getApplicationContext());
                    screenHeight = getResources().getDisplayMetrics().heightPixels - (int) Utils.convertDpToPixels(114, getApplicationContext());

                    if ((screenWidth * photoBitmap.getHeight()) / photoBitmap.getWidth() < screenHeight) {
                        photoBitmap = BitmapHelper.getResizedBitmap(photoBitmap, screenWidth, (screenWidth * photoBitmap.getHeight()) / photoBitmap.getWidth());
                    } else {
                        photoBitmap = BitmapHelper.getResizedBitmap(photoBitmap, screenWidth, screenHeight);
                    }

                    if (bmpWidth > bmpHeight) {
                        photoBitmap = BitmapHelper.getRotatedBitmap(photoBitmap, 90);
                    }
                    if (numOfPieces == 20) {
                        numOfColsInGrid = 4;
                        numOfRows = 5;
                    } else if (numOfPieces == 24) {
                        numOfColsInGrid = 4;
                        numOfRows = 6;
                    } else if (numOfPieces == 30) {
                        numOfColsInGrid = 5;
                        numOfRows = 6;
                    } else {
                        numOfColsInGrid = 3;
                        numOfRows = 4;
                    }

                    bitmapPiecesList = doBreakPhotoBitmap(photoBitmap);
                    for (int i = 0; i < bitmapPiecesList.size(); i++) {
                        SquareTile tile = new SquareTile(i, bitmapPiecesList.get(i).getWidth(), bitmapPiecesList.get(i).getHeight(), bitmapPiecesList.get(i), i, 0);
                        tilesList.add(tile);
                    }
                    Collections.shuffle(tilesList);
                    for (int i = 0; i < tilesList.size(); i++) {
                        tilesList.get(i).setCurrentPosition(i);
                        tilesList.get(i).setRotation(RAN.nextInt(4));
                    }
                    if (savedInstanceState != null && FracturePhotoApplication.getOriginalTilesListSquare() != null) {
                        tilesList = FracturePhotoApplication.getOriginalTilesListSquare();
                    }
                    adapter = new SquarePuzzleGridAdapter(this, tilesList, isCreatePuzzleMode, null, congratsParentLayout);
                    gridView.setAdapter(adapter);

                    gridView.setNumColumns(numOfColsInGrid);
                    gridView.setColumnWidth(tilesList.get(0).getWidth());
                    gridView.setStretchMode(GridView.NO_STRETCH);
                } else {
                    // we are in SOLVE PUZZLE MODE

                    String pzlRowId = intent.getStringExtra(Extras_Keys.PUZZLE_HISTORY_ROW_ID_IN_DB);

                    // Added the below block to make the long side of photo
                    // match with long side of puzzle assembly area
                    bmpWidth = photoBitmap.getWidth();
                    bmpHeight = photoBitmap.getHeight();
                    if (bmpWidth > bmpHeight) {
                        photoBitmap = BitmapHelper.getRotatedBitmap(photoBitmap, 90);
                    }

                    // load from db and create tile list and set adapter
                    FracturePhotoDB db = new FracturePhotoDB(getApplicationContext());
                    db.openDB();
                    Cursor c = new SampleCursorLoader(getApplicationContext(), db, "loadSquareTilesDetailsOfPuzzleId", pzlRowId).loadInBackground();
                    c.moveToFirst();
                    int gridColWidth = 1;

                    numOfPieces = c.getCount();
                    if (numOfPieces == 20) {
                        numOfColsInGrid = 4;
                        numOfRows = 5;
                    } else if (numOfPieces == 24) {
                        numOfColsInGrid = 4;
                        numOfRows = 6;
                    } else if (numOfPieces == 30) {
                        numOfColsInGrid = 5;
                        numOfRows = 6;
                    } else {
                        numOfColsInGrid = 3;
                        numOfRows = 4;
                    }

                    for (int i = 0; i < c.getCount(); i++) {
                        byte[] imageArray = c.getBlob(c.getColumnIndexOrThrow(FracturePhotoDBModel.COL_SQUARE_TILE_BITMAP));

                        SquareTile tile = new SquareTile(
                                Integer.parseInt(c.getString(c.getColumnIndexOrThrow(FracturePhotoDBModel.COL_SQUARE_TILE_ID))),
                                Integer.parseInt(c.getString(c.getColumnIndexOrThrow(FracturePhotoDBModel.COL_SQUARE_TILE_WIDTH))),
                                Integer.parseInt(c.getString(c.getColumnIndexOrThrow(FracturePhotoDBModel.COL_SQUARE_TILE_HEIGHT))),
                                BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length),
                                Integer.parseInt(c.getString(c.getColumnIndexOrThrow(FracturePhotoDBModel.COL_SQUARE_TILE_ORIG_POS))),
                                Integer.parseInt(c.getString(c.getColumnIndexOrThrow(FracturePhotoDBModel.COL_SQUARE_TILE_ROTATION))));

                        int currentPosition = Integer.parseInt(c.getString(c.getColumnIndexOrThrow(FracturePhotoDBModel.COL_SQUARE_TILE_CUR_POS)));
                        tile.setCurrentPosition(currentPosition);
                        tilesList.add(currentPosition, tile);

                        numOfColsInGrid = Integer.parseInt(c.getString(c.getColumnIndexOrThrow(FracturePhotoDBModel.COL_SQUARE_TILE_NUM_OF_GRID_COLUMNS)));
                        gridColWidth = Integer.parseInt(c.getString(c.getColumnIndexOrThrow(FracturePhotoDBModel.COL_SQUARE_TILE_GRID_COLUMNWIDTH)));

                        c.moveToNext();
                    }

                    adapter = new SquarePuzzleGridAdapter(this, tilesList, isCreatePuzzleMode, pzlRowId, congratsParentLayout);
                    gridView.setAdapter(adapter);

                    db.closeDB();
                    gridView.setNumColumns(numOfColsInGrid);
                    gridView.setColumnWidth(gridColWidth);
                    gridView.setStretchMode(GridView.NO_STRETCH);
                }
            } else {
                showToast("Something went wrong. Please try again later");
                finish();
            }
        } else {
            // SHARED PUZZLE
            System.err.println("Shared puzzle");
            photoPath = intent.getStringExtra(Extras_Keys.PHOTO_PATH);
            numOfPieces = intent.getIntExtra(Extras_Keys.NUM_OF_PIECES, 12);
            metaDataFilePath = intent.getStringExtra(Extras_Keys.META_DATA_FILE_PATH);
            if (new File(photoPath).exists()) {
                if (new File(metaDataFilePath).exists()) {
                    ArrayList<String[]> list = Utils.readMetaDataFile(metaDataFilePath);
                    photoBitmap = BitmapHelper.decodeBitmapFromPath(photoPath, 640, 480, Utils.getPhotoOrientation(photoPath));
                    if (photoBitmap != null && list.size() > 1) {
                        bmpWidth = photoBitmap.getWidth();
                        bmpHeight = photoBitmap.getHeight();
                        if (bmpWidth > bmpHeight) {
                            photoBitmap = BitmapHelper.getRotatedBitmap(photoBitmap, 90);
                        }
                        screenWidth = getResources().getDisplayMetrics().widthPixels - (int) Utils.convertDpToPixels(34, getApplicationContext());
                        // need to subtract paddings and margins of layouts from
                        // screenwidth to calculate accurate screenWidth
                        if (list.size() == 20) {
                            numOfColsInGrid = 4;
                            numOfRows = 5;
                        } else if (list.size() == 24) {
                            numOfColsInGrid = 4;
                            numOfRows = 6;
                        } else if (list.size() == 30) {
                            numOfColsInGrid = 5;
                            numOfRows = 6;
                        } else {
                            numOfColsInGrid = 3;
                            numOfRows = 4;
                        }
                        numOfPieces = numOfColsInGrid * numOfRows;

                        bitmapPiecesList = doBreakPhotoBitmap(photoBitmap);
                        for (int i = 0; i < bitmapPiecesList.size(); i++) {
                            SquareTile tile = new SquareTile(i,
                                    bitmapPiecesList.get(i).getWidth(),
                                    bitmapPiecesList.get(i).getHeight(),
                                    bitmapPiecesList.get(i), i, 0);

                            tilesList.add(tile);
                        }

                        ArrayList<SquareTile> duplicateTilesListToReArrangeTheOrder = new ArrayList<>(numOfPieces);

                        for (int i = 0; i < tilesList.size(); i++) {
                            String dataTokens[] = getMetaDataRowWithCorrespondingTileId(list, tilesList.get(i).getTileId());
                            tilesList.get(i).setCurrentPosition(Integer.parseInt(dataTokens[1]));
                            tilesList.get(i).setRotation(Integer.parseInt(dataTokens[3]));
                            duplicateTilesListToReArrangeTheOrder.add(tilesList.get(i));
                        }
                        for (int i = 0; i < numOfPieces; i++) {
                            SquareTile tile = tilesList.get(i);
                            duplicateTilesListToReArrangeTheOrder.set(tile.getCurrentPosition(), tile);
                        }
                        tilesList = duplicateTilesListToReArrangeTheOrder;
                        adapter = new SquarePuzzleGridAdapter(this, tilesList, isCreatePuzzleMode, null, congratsParentLayout);
                        gridView.setAdapter(adapter);

                        gridView.setNumColumns(numOfColsInGrid);
                        gridView.setColumnWidth(tilesList.get(0).getWidth());
                        gridView.setStretchMode(GridView.NO_STRETCH);
                        saveSharedPath(photoPath);
                        saveSharedPuzzle();
                        try {
                            Thread.sleep(3000);
                            if (rootPath != null && !rootPath.isEmpty()) {
                                File f = new File(rootPath);
                                DeleteRecursive(f);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                } else {
                    showToast("Meta data file has been deleted. You can't play this puzzle");
                    finish();
                    return;
                }
            } else {
                showToast("Puzzle Photo has been deleted. You can't play this puzzle");
                finish();
                return;
            }
        }
    }

    private void saveSharedPath(String sharedDownloadPhotoPath) {
        // temp store
        Log.v("sharedDownloadPh====", sharedDownloadPhotoPath);
        Bitmap sharedBitmap = BitmapFactory.decodeFile(sharedDownloadPhotoPath);
        File sharedFile = null;
        String root = SDCARD_PATH + File.separator + getString(string.app_name) + File.separator;
        myDir = new File(root + "/SharedPhotos");
        myDir.mkdirs();

        String fname = new SimpleDateFormat("yyyyMMddhhmmss'_SharedSquarePhto.jpg'").format(new Date());
        sharedFile = new File(myDir, fname);
        if (sharedFile.exists())
            sharedFile.delete();
        try {
            FileOutputStream fo = new FileOutputStream(sharedFile);
            sharedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fo);
            sharedBitmap = null;
            pathOfDuplicateSharedPhoto = sharedFile.getAbsolutePath();
            photoPath = pathOfDuplicateSharedPhoto;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveSharedPuzzle() {
        new Thread(() -> {
            FracturePhotoDB db = new FracturePhotoDB(getApplicationContext());
            db.openDB();
            if (isSharedPuzzle) {
                savedSharedPuzzleRowId = db.savePuzzleHistory(puzzleName, pathOfDuplicateSharedPhoto, numOfPieces, PuzzleType.SQUARE_PUZZLE.getPuzzleType());
                if (savedSharedPuzzleRowId != 0) {
                    for (int i = 0; i < tilesList.size(); ++i) {
                        SquareTile tile = tilesList.get(i);
                        if (db.saveSquareTilesDetails(
                                savedSharedPuzzleRowId, numOfPieces, tile,
                                String.valueOf(numOfColsInGrid),
                                String.valueOf(tilesList.get(0).getWidth())) == -1) {
                            db.deletePuzzleHistoryWithId(String.valueOf(savedSharedPuzzleRowId));
                            showToast("Something went wrong\nPlease try again later");
                            db.closeDB();
                            return;
                        }
                    }
                    db.closeDB();
                }
            } else {
                db.closeDB();
            }
        }).start();
    }

    private String[] getMetaDataRowWithCorrespondingTileId(ArrayList<String[]> list, int tileId) {
        for (int i = 0; i < list.size(); i++) {
            if (Integer.parseInt(list.get(i)[0]) == tileId) {
                return list.get(i);
            }
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveButton:
                if (tilesList != null && tilesList.size() >= 1) {
                    doSavePuzzle();
                }
                break;
            case R.id.preview:
                if (photoBitmap != null) {
                    final Dialog d = new Dialog(this, R.style.preview_dialog);
                    d.setContentView(R.layout.preview_dialog);
                    ImageView img = (ImageView) d.findViewById(R.id.previewImage);

                    ImageView closebtn = (ImageView) d.findViewById(R.id.Closebtn);
                    closebtn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            d.dismiss();
                        }
                    });
                    img.setImageBitmap(photoBitmap);
                    d.show();
                }
                break;
            case R.id.sharePuzzleButton:
                /*
                 * if (tilesList != null && tilesList.size() >= 1 && adapter != null
                 * && !adapter.isPuzzleSolved()) { final ProgressDialog progressBar
                 * = ProgressDialog.show(PlaySquarePuzzleActivity.this, "Preparing",
                 * "Please wait..."); progressBar.setCancelable(false);
                 * progressBar.setCanceledOnTouchOutside(false);
                 * progressBar.setIndeterminate(true); new Thread(new Runnable() {
                 *
                 * @Override public void run() { if
                 * (makeDuplicateOfPuzzlePhoto(puzzleName, filePath, numOfPieces,
                 * PuzzleType.SQUARE_PUZZLE.getPuzzleType())) {
                 * doPrepareSquareTilesMetaInfo(); doPrepareUploadFileInfo();
                 * uploadFile(); //doSendEmail(); } if(progressBar != null &&
                 * progressBar.isShowing()){ progressBar.dismiss(); } } }).start();
                 * }else if(tilesList != null && tilesList.size() >= 1 && adapter !=
                 * null && adapter.isPuzzleSolved()){ showToast(
                 * "You are not allowed to share this puzzle as it has been solved"
                 * ); }
                 */

                if (FracturePhotoApplication.getInstalledAppsList() == null) {
                    final ProgressDialog progressBar = ProgressDialog.show(PlaySquarePuzzleActivity.this, "Loading", "Please wait...");
                    progressBar.setCancelable(false);
                    progressBar.setCanceledOnTouchOutside(false);
                    progressBar.setIndeterminate(true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            FracturePhotoApplication.setInstalledAppsList(AppsInfo.getInstalledApps(PlaySquarePuzzleActivity.this, true));
                            if (progressBar != null && progressBar.isShowing()) {
                                progressBar.dismiss();
                            }
                            if (!isDuplicateShareClick) {
                                isDuplicateShareClick = true;
                                PlaySquarePuzzleActivity.this
                                        .runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (Utils.isConnectionAvailable(PlaySquarePuzzleActivity.this))
                                                    showSharePopup();
                                                else {
                                                    String message = "Your Device is not connected to Intetnet. Please connect to working Internet.";
                                                    Utils.NetworkMessage(PlaySquarePuzzleActivity.this, message);
                                                    isDuplicateShareClick = false;
                                                }
                                            }
                                        });
                            }
                        }
                    }).start();
                } else {
                    if (!isDuplicateShareClick) {
                        isDuplicateShareClick = true;
                        if (Utils.isConnectionAvailable(PlaySquarePuzzleActivity.this)) {
                            showSharePopup();
                        } else {
                            String message = "Your Device is not connected to Intetnet. Please connect to working Internet.";
                            Utils.NetworkMessage(PlaySquarePuzzleActivity.this, message);
                            isDuplicateShareClick = false;
                        }
                    }
                }
                break;
            case R.id.turnLeft:
                rotateSquareTile(-1);
                break;
            case R.id.turnRight:
                rotateSquareTile(1);
                break;
            default:
                break;
        }
    }

    protected void doPrepareUploadFileInfo() {
        // String zipName = cacheFolder + File.separator +
        // getString(R.string.app_name).replace(" ", "") +
        // SHARING_ZIP_FOLDER_NAME;
        String zipName = cacheFolder + File.separator + /*
         * getString(R.string.
         * app_name
         * ).replace(" ", "") +
         * SEPARATOR_SYMBOL +
         */puzzleName
                + ZIP_EXTENSION;
        File zipfile = new File(zipName);
        String[] files = new String[]{imagePathToShare, metaDataFilePath};
        ZipHelper cmprs = new ZipHelper(files, zipfile.getAbsolutePath());
        cmprs.zip();

        String newZipStrName = cacheFolder + File.separator + deviceid + ZIP_EXTENSION;
        final File newZipFileName = new File(newZipStrName);
        if (zipfile.exists()) {
            zipfile.renameTo(newZipFileName);
        }

        ZipHelper cmprs_new = new ZipHelper(files, newZipFileName.getAbsolutePath());
        cmprs_new.zip();
        uploadFileName = newZipFileName.getAbsolutePath();
    }

    private void showSharePopup() {
        final Dialog dialog = new Dialog(PlaySquarePuzzleActivity.this);
        dialog.setContentView(R.layout.share_screen_layout);
        dialog.setTitle("Share via");
        GridView gridView = (GridView) dialog.findViewById(R.id.gridView);
        String[] appNames = null;
        if (IS_ADMIN_MODE) {
            appNames = new String[]{"Facebook", GALLERY_PAGE_NAME, "Twitter", "Messages", "Gmail"};
        } else {
            appNames = new String[]{"Facebook", "Twitter", "Messages", "Gmail"};
        }
        if (FracturePhotoApplication.getInstalledAppsList() == null) {
            FracturePhotoApplication.setInstalledAppsList(AppsInfo.getInstalledApps(PlaySquarePuzzleActivity.this, true));
        }
        SharePopupAdapter shareAdapter = new SharePopupAdapter(this, appNames, FracturePhotoApplication.getInstalledAppsList());
        gridView.setAdapter(shareAdapter);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isDuplicateShareClick = false;
            }
        });
        dialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                isDuplicateShareClick = false;
            }
        });
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (adapterView.getItemAtPosition(position).toString().equals("Gmail")) {
                    if (tilesList != null && tilesList.size() >= 1 && adapter != null && !adapter.isPuzzleSolved()) {
                        final ProgressDialog progressBar = ProgressDialog.show(PlaySquarePuzzleActivity.this, "Preparing", "Please wait...");
                        progressBar.setCancelable(false);
                        progressBar.setCanceledOnTouchOutside(false);
                        progressBar.setIndeterminate(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (makeDuplicateOfPuzzlePhoto(puzzleName, photoPath, numOfPieces, PuzzleType.SQUARE_PUZZLE.getPuzzleType())) {
                                    doPrepareSquareTilesMetaInfo();
                                    doPrepareUploadFileInfo();
                                    doPrepareFbCaptureImage();
                                    uploadFile("Gmail", false);
                                    try {
                                        if (progressBar != null && progressBar.isShowing()) {
                                            progressBar.dismiss();
                                        }
                                        Thread.sleep(sleepTime);
                                        if (rootPath != null && !rootPath.isEmpty()) {
                                            File f = new File(rootPath);
                                            DeleteRecursive(f);
                                        }
                                    } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                    }

                                }
                                if (progressBar != null && progressBar.isShowing()) {
                                    progressBar.dismiss();
                                }
                            }
                        }).start();
                    } else if (tilesList != null && tilesList.size() >= 1 && adapter != null && adapter.isPuzzleSolved()) {
                        showToast("You are not allowed to share this puzzle as it has been solved");
                    }
                }
                if (adapterView.getItemAtPosition(position).toString().equals("Facebook")) {
                    if (tilesList != null && tilesList.size() >= 1 && adapter != null && !adapter.isPuzzleSolved()) {
                        final ProgressDialog progressBar = ProgressDialog.show(PlaySquarePuzzleActivity.this, "Preparing", "Please wait...");
                        progressBar.setCancelable(false);
                        progressBar.setCanceledOnTouchOutside(false);
                        progressBar.setIndeterminate(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (makeDuplicateOfPuzzlePhoto(puzzleName, photoPath, numOfPieces, PuzzleType.SQUARE_PUZZLE.getPuzzleType())) {
                                    doPrepareSquareTilesMetaInfo();
                                    doPrepareUploadFileInfo();
                                    doPrepareFbCaptureImage();
                                    uploadFile("Facebook", false);
                                    try {
                                        if (progressBar != null && progressBar.isShowing()) {
                                            progressBar.dismiss();
                                        }
                                        Thread.sleep(sleepTime);
                                        if (rootPath != null && !rootPath.isEmpty()) {
                                            File f = new File(rootPath);
                                            DeleteRecursive(f);

                                        }
                                    } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                if (progressBar != null && progressBar.isShowing()) {
                                    progressBar.dismiss();
                                }
                            }
                        }).start();
                    } else if (tilesList != null && tilesList.size() >= 1 && adapter != null && adapter.isPuzzleSolved()) {
                        showToast("You are not allowed to share this puzzle as it has been solved");
                    }
                }
                /*if (adapterView.getItemAtPosition(position).toString()
                        .equals("Instagram")) {
					if (tilesList != null && tilesList.size() >= 1
							&& adapter != null && !adapter.isPuzzleSolved()) {
						final ProgressDialog progressBar = ProgressDialog.show(
								PlaySquarePuzzleActivity.this, "Preparing",
								"Please wait...");
						progressBar.setCancelable(false);
						progressBar.setCanceledOnTouchOutside(false);
						progressBar.setIndeterminate(true);
						new Thread(new Runnable() {
							@Override
							public void run() {
								if (makeDuplicateOfPuzzlePhoto(puzzleName,
										photoPath, numOfPieces,
										PuzzleType.SQUARE_PUZZLE
										.getPuzzleType())) {
									doPrepareSquareTilesMetaInfo();
									doPrepareUploadFileInfo();
									doPrepareFbCaptureImage();
									uploadFile("Instagram");
									try {
										if (progressBar != null
												&& progressBar.isShowing()) {
											progressBar.dismiss();
										}
										Thread.sleep(300000);
										if (rootPath != null
												&& !rootPath.isEmpty()) {
											File f = new File(rootPath);
											DeleteRecursive(f);

										}
									} catch (InterruptedException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}

								}
								if (progressBar != null
										&& progressBar.isShowing()) {
									progressBar.dismiss();
								}
							}
						}).start();
					} else if (tilesList != null && tilesList.size() >= 1
							&& adapter != null && adapter.isPuzzleSolved()) {
						showToast("You are not allowed to share this puzzle as it has been solved");
					}
				}*/
                if (adapterView.getItemAtPosition(position).toString().equals(GALLERY_PAGE_NAME)) {
                    if (tilesList != null && tilesList.size() >= 1 && adapter != null && !adapter.isPuzzleSolved()) {
                        final ProgressDialog progressBar = ProgressDialog.show(PlaySquarePuzzleActivity.this, "Preparing", "Please wait...");
                        progressBar.setCancelable(false);
                        progressBar.setCanceledOnTouchOutside(false);
                        progressBar.setIndeterminate(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (makeDuplicateOfPuzzlePhoto(puzzleName, photoPath, numOfPieces, PuzzleType.SQUARE_PUZZLE.getPuzzleType())) {
                                    doPrepareSquareTilesMetaInfo();
                                    doPrepareUploadFileInfo();
                                    doPrepareFbCaptureImage();
                                    uploadFile(GALLERY_PAGE_NAME, true);
                                    try {
                                        if (progressBar != null && progressBar.isShowing()) {
                                            progressBar.dismiss();
                                        }
                                        Thread.sleep(sleepTime);
                                        if (rootPath != null && !rootPath.isEmpty()) {
                                            File f = new File(rootPath);
                                            DeleteRecursive(f);

                                        }
                                    } catch (InterruptedException e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }

                                }
                                if (progressBar != null && progressBar.isShowing()) {
                                    progressBar.dismiss();
                                }
                            }
                        }).start();
                    } else if (tilesList != null && tilesList.size() >= 1 && adapter != null && adapter.isPuzzleSolved()) {
                        showToast("You are not allowed to share this puzzle as it has been solved");
                    }
                }
                if (adapterView.getItemAtPosition(position).toString().equals("Twitter")) {
                    if (tilesList != null && tilesList.size() >= 1 && adapter != null && !adapter.isPuzzleSolved()) {
                        final ProgressDialog progressBar = ProgressDialog.show(PlaySquarePuzzleActivity.this, "Preparing", "Please wait...");
                        progressBar.setCancelable(false);
                        progressBar.setCanceledOnTouchOutside(false);
                        progressBar.setIndeterminate(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (makeDuplicateOfPuzzlePhoto(puzzleName, photoPath, numOfPieces, PuzzleType.SQUARE_PUZZLE.getPuzzleType())) {
                                    doPrepareSquareTilesMetaInfo();
                                    doPrepareUploadFileInfo();
                                    doPrepareFbCaptureImage();
                                    uploadFile("Twitter", false);
                                    try {
                                        if (progressBar != null && progressBar.isShowing()) {
                                            progressBar.dismiss();
                                        }
                                        Thread.sleep(sleepTime);
                                        if (rootPath != null && !rootPath.isEmpty()) {
                                            File f = new File(rootPath);
                                            DeleteRecursive(f);
                                        }
                                    } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                    }

                                }
                                if (progressBar != null && progressBar.isShowing()) {
                                    progressBar.dismiss();
                                }
                            }
                        }).start();
                    } else if (tilesList != null && tilesList.size() >= 1 && adapter != null && adapter.isPuzzleSolved()) {
                        showToast("You are not allowed to share this puzzle as it has been solved");
                    }
                }
                if (adapterView.getItemAtPosition(position).toString().equals("Messages")) {
                    if (tilesList != null && tilesList.size() >= 1 && adapter != null && !adapter.isPuzzleSolved()) {
                        final ProgressDialog progressBar = ProgressDialog.show(PlaySquarePuzzleActivity.this, "Preparing", "Please wait...");
                        progressBar.setCancelable(false);
                        progressBar.setCanceledOnTouchOutside(false);
                        progressBar.setIndeterminate(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (makeDuplicateOfPuzzlePhoto(puzzleName, photoPath, numOfPieces, PuzzleType.SQUARE_PUZZLE.getPuzzleType())) {
                                    doPrepareSquareTilesMetaInfo();
                                    doPrepareUploadFileInfo();
                                    doPrepareFbCaptureImage();
                                    uploadFile("Messages", false);
                                    try {
                                        if (progressBar != null && progressBar.isShowing()) {
                                            progressBar.dismiss();
                                        }
                                        Thread.sleep(sleepTime);
                                        if (rootPath != null && !rootPath.isEmpty()) {
                                            File f = new File(rootPath);
                                            DeleteRecursive(f);
                                        }
                                    } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                if (progressBar != null && progressBar.isShowing()) {
                                    progressBar.dismiss();
                                }
                            }
                        }).start();
                    } else if (tilesList != null && tilesList.size() >= 1 && adapter != null && adapter.isPuzzleSolved()) {
                        showToast("You are not allowed to share this puzzle as it has been solved");
                    }
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static boolean DeleteRecursive(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = DeleteRecursive(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    protected void doPrepareFbCaptureImage() {

        gridView.setDrawingCacheEnabled(true);
        Bitmap b = Bitmap.createBitmap(gridView.getDrawingCache());
        gridView.setDrawingCacheEnabled(false);

        /*
         * //Save bitmap // String extr =
         * Environment.getExternalStorageDirectory().toString() + File.separator
         * + "Folderabc"; String extr = SDCARD_PATH + File.separator +
         * getString(R.string.app_name) + "_SharedPuzzles" + File.separator +
         * "Puzzle_"+ Utils.getCurrentTime() + File.separator; if(!new
         * File(extr).exists()){ new File(extr).mkdir(); } String fileName = new
         * SimpleDateFormat("yyyyMMddhhmmss'_report.jpg'").format(new Date());
         * File myPath = new File(extr, fileName); FileOutputStream fos = null;
         * try { fos = new FileOutputStream(myPath);
         * b.compress(Bitmap.CompressFormat.JPEG, 100, fos); fos.flush();
         * fos.close();
         * //MediaStore.Images.Media.insertImage(getContentResolver(), b,
         * "Screen", "screen");
         *
         * uploadFbFileName= myPath.getAbsolutePath(); }catch
         * (FileNotFoundException e) { // TODO Auto-generated catch block
         * e.printStackTrace(); } catch (Exception e) { // TODO Auto-generated
         * catch block e.printStackTrace(); }
         */

        // temp file
        File imageFile = null;
        String root = SDCARD_PATH + File.separator
                + getString(string.app_name) + "_SharedPuzzles"
                + File.separator + "Puzzle_" + Utils.getCurrentTime()
                + File.separator;
        myDir = new File(root + "/snappit");
        myDir.mkdirs();

        String fname = new SimpleDateFormat("yyyyMMddhhmmss'_report.jpg'").format(new Date());
        imageFile = new File(myDir, fname);
        if (imageFile.exists())
            imageFile.delete();
        try {
            FileOutputStream fo = new FileOutputStream(imageFile);
            b.compress(Bitmap.CompressFormat.JPEG, 100, fo);
            b = null;
            uploadFbFileName = imageFile.getAbsolutePath();
            Log.v("saved_path", "" + uploadFbFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void doSavePuzzle() {
        progressBar = ProgressDialog.show(PlaySquarePuzzleActivity.this, "Saving Puzzle", "Please wait...");
        progressBar.setCancelable(false);
        progressBar.setCanceledOnTouchOutside(false);
        progressBar.setIndeterminate(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                FracturePhotoDB db = new FracturePhotoDB(getApplicationContext());
                db.openDB();
                if (isCreatePuzzleMode) {
                    long newRowId = db.savePuzzleHistory(puzzleName, photoPath, numOfPieces, PuzzleType.SQUARE_PUZZLE.getPuzzleType());
                    if (newRowId != 0) {
                        for (int i = 0; i < tilesList.size(); i++) {
                            SquareTile tile = tilesList.get(i);
                            if (db.saveSquareTilesDetails(newRowId,
                                    numOfPieces, tile, gridView.getNumColumns() + "", "" + gridView.getColumnWidth()) == -1) {
                                db.deletePuzzleHistoryWithId(String.valueOf(newRowId));
                                showToast("Something went wrong\nPlease try again later");
                                db.closeDB();
                                if (progressBar != null && progressBar.isShowing()) {
                                    progressBar.dismiss();
                                }
                                PlaySquarePuzzleActivity.this.finish();
                                return;
                            }
                        }
                        showToast("Puzzle has been saved succesfully");
                        db.closeDB();
                        if (progressBar != null && progressBar.isShowing()) {
                            progressBar.dismiss();
                        }
                        Intent i = new Intent(PlaySquarePuzzleActivity.this, DashboardActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        if (!isActivityStopped()) {
                            startActivity(i);
                        }
                        PlaySquarePuzzleActivity.this.finish();
                    }
                } else {// WE ARE IN SAVE PUZZLE MODE or Shared puzzle
                    String oldRowId;
                    if (isSharedPuzzle) {
                        oldRowId = String.valueOf(savedSharedPuzzleRowId);
                    } else {
                        oldRowId = getIntent().getStringExtra(Extras_Keys.PUZZLE_HISTORY_ROW_ID_IN_DB);
                    }
                    long newRowId = db.updatePuzzle(oldRowId, puzzleName, photoPath, numOfPieces);
                    if (newRowId != -1) {
                        int numOfRowsDeleted = db.deleteSquareTilesAssociatedWithBaseRowId(oldRowId);
                        System.err.println("NumOfRowsDeleted == " + numOfRowsDeleted);
                        if (numOfRowsDeleted == numOfPieces) {
                            for (int i = 0; i < tilesList.size(); i++) {
                                /*
                                 * SquareTile tile = tilesList.get(i); if
                                 * (db.updateSquareTilesDetails(oldRowId,
                                 * newRowId, numOfPieces, tile,
                                 * gridView.getNumColumns() + "", "" +
                                 * gridView.getColumnWidth()) == 0) {
                                 * Toast.makeText(getApplicationContext(),
                                 * "Something went wrong",
                                 * Toast.LENGTH_LONG).show(); break; }
                                 */
                                SquareTile tile = tilesList.get(i);
                                if (db.saveSquareTilesDetails(newRowId, numOfPieces, tile, gridView.getNumColumns() + "", "" + gridView.getColumnWidth()) == -1) {
                                    showToast("Something went wrong\nPlease try again later");
                                    db.closeDB();
                                    if (progressBar != null && progressBar.isShowing()) {
                                        progressBar.dismiss();
                                    }
                                    PlaySquarePuzzleActivity.this.finish();
                                    return;
                                }
                            }
                            showToast("Puzzle has been updated succesfully");
                            db.closeDB();
                            if (progressBar != null && progressBar.isShowing()) {
                                progressBar.dismiss();
                            }
                            Intent i = new Intent(PlaySquarePuzzleActivity.this, DashboardActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            if (!isActivityStopped()) {
                                startActivity(i);
                            }
                            PlaySquarePuzzleActivity.this.finish();
                        }
                    }
                }
            }
        }).start();
    }

    private ArrayList<Bitmap> doBreakPhotoBitmap(Bitmap photoBitmap) {

        ArrayList<Bitmap> bitmapPiecesList = new ArrayList<>(numOfPieces);
        int pieceHeight, pieceWidth;
        // for perfect square tile
        pieceHeight = photoBitmap.getHeight() / numOfRows;
        pieceWidth = photoBitmap.getWidth() / numOfColsInGrid;

        int newWidth = screenWidth / numOfColsInGrid;

        //showToast("" + (getResources().getDisplayMetrics().heightPixels - (int)Utils.convertDpToPixels(100, this)));

        Matrix matrix = new Matrix();
        matrix.postScale(((float) newWidth) / pieceWidth, ((float) newWidth) / pieceHeight);

        int yCoord = 0;
        for (int x = 0; x < numOfRows; ++x) {
            int xCoord = 0;
            for (int y = 0; y < numOfColsInGrid; ++y) {
                Bitmap resizedBitmap = Bitmap.createBitmap(photoBitmap, xCoord, yCoord, pieceWidth, pieceHeight, matrix, true);
                bitmapPiecesList.add(resizedBitmap);
                xCoord += pieceWidth;
            }
            yCoord += pieceHeight;
        }
        return bitmapPiecesList;
    }

    public boolean isGridTouched() {
        return isGridTouched;
    }

    public void setGridTouched(boolean isGridTouched) {
        this.isGridTouched = isGridTouched;
    }

    public int getTouchedItemPos() {
        return touchedItemPos;
    }

    public void setTouchedItemPos(int touchedItemPos) {
        this.touchedItemPos = touchedItemPos;
    }

    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast t = Toast.makeText(PlaySquarePuzzleActivity.this, msg, Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            }
        });
    }

    private void rotateSquareTile(int angle) {
        if (isGridTouched()) {
            SquareTile tile = tilesList.get(getTouchedItemPos());
            tile.setRotation((tile.getRotation() + angle) % 4);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                if (adapter.isPuzzleSolved()) {
                    adapter.showPuzzleSolvedDialogBox();
                    Utils.doPlaySound(this, R.raw.congratulations_audio);
                }
            }
        }
    }

    private boolean makeDuplicateOfPuzzlePhoto(String puzzleName, String filePath, int numOfPieces, String typeOfPuzzle) {
        String extDir;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            extDir = getExternalCacheDir().getAbsolutePath() + File.separator + getString(string.app_name);
        } else {
            extDir = getCacheDir().getAbsolutePath() + File.separator + getString(string.app_name);
        }
        cacheFolder = new File(extDir);
        if (!cacheFolder.exists()) {
            cacheFolder.mkdir();
        }
        cachedPicturesFolder = new File(cacheFolder.getAbsolutePath() + File.separator + "Pictures");
        if (!cachedPicturesFolder.exists()) {
            cachedPicturesFolder.mkdir();
        }
        try {
            File picsDir = new File(cachedPicturesFolder.getAbsoluteFile().toString());
            for (File f : picsDir.listFiles()) {
                if (f.length() > 0) {
                    f.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        String puzzlePhotoFileName = puzzleName + SEPARATOR_SYMBOL + typeOfPuzzle + SEPARATOR_SYMBOL + numOfPieces + SEPARATOR_SYMBOL + "PuzzlePhoto" + Utils.getCurrentTime() + IMAGE_FILE_EXTENSION;
        File photoFile = new File(cachedPicturesFolder.getAbsolutePath(), puzzlePhotoFileName);
        imagePathToShare = photoFile.getAbsolutePath();
        Log.v("imagePathToShare", imagePathToShare);
        try {
            if (photoBitmap != null) {
                BitmapHelper.writeBitmapIntoFile(getApplicationContext(), photoBitmap, imagePathToShare);
                return true;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void doPrepareSquareTilesMetaInfo() {
        ArrayList<String> detailsList = new ArrayList<>();
        File file = new File(cachedPicturesFolder.getAbsolutePath(), puzzleName + META_FILE_EXTENSION);
        if (file.exists()) {
            file.delete();
        }

        File newFile = new File(cachedPicturesFolder.getAbsolutePath(),puzzleName + META_FILE_EXTENSION);
        metaDataFilePath = newFile.getAbsolutePath();

        for (int i = 0; i < tilesList.size(); i++) {
            SquareTile tile = tilesList.get(i);
            detailsList.add(tile.getTileId() + SEPARATOR_SYMBOL + tile.getCurrentPosition() + SEPARATOR_SYMBOL + tile.getTileOriginalPosition() + SEPARATOR_SYMBOL + tile.getRotation());
        }
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(newFile, true));
            for (String line : detailsList) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doSendEmail() {
        if (uploadFbFileName != null) { // we need to send this photo as an attachment (New Requirement on Feb 14, 2015)
            File attachmentFile = new File(uploadFbFileName);
            if (!attachmentFile.exists() || !attachmentFile.canRead()) {
                showToast("Something went wrong");
                return;
            }

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/html");
            final PackageManager pm = this.getPackageManager();
            final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent, 0);
            String className = null;
            for (final ResolveInfo info : matches) {
                if (info.activityInfo.packageName.equals("com.google.android.gm")) {
                    className = info.activityInfo.name;
                    if (className != null && !className.isEmpty()) {
                        break;
                    }
                }
            }
            emailIntent.setClassName("com.google.android.gm", className);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "FracturedPhoto Puzzle");
            emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml("<br>" + fileLink + "<br>" + "[" + hyperLinkText + "<a href=\"" + FP_BASE_URL + "mobile.php\"></a>" + "]" + "<br>"));
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(attachmentFile));
            try {
                startActivityForResult(emailIntent, SHARE_PUZZLE_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                showToast("No application found on this device to perform share action");
            } catch (Exception e) {
                showToast(e.getMessage());
                e.printStackTrace();
            }
        } else {
            showToast("Something went wrong");
        }
    }

    public boolean uploadFile(final String var, boolean isAdminPuzzle) {
        try {
            Log.v("TAG", "uploading file.....");
            Log.v("uploadFileName", uploadFileName);
            Log.v("uploadFbFileName", uploadFbFileName);
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(isAdminPuzzle ? adminPuzzleUploadUrl : uploadPuzzleUrl);
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);
            nameValuePairs.add(new BasicNameValuePair("device_id", deviceid));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            if (uploadFileName == null || uploadFileName.isEmpty() || uploadFbFileName == null || uploadFbFileName.isEmpty()){
                showToast("Oops, Something went wrong.. Please try sharing again.");
                showToast("Oops, Something went wrong.. Please try sharing again.");
                return false;
            }
            File file = new File(uploadFileName);
            File shared_image = new File(uploadFbFileName);
            if (file.length() <= 0 || shared_image.length() <= 0){
                showToast("Oops, Something went wrong. Please try sharing again.");
                showToast("Oops, Something went wrong. Please try sharing again.");
                return false;
            }

            FileBody fileBody = new FileBody(file);
            FileBody fileBodyFb = new FileBody(shared_image);

            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("file", fileBody);
            reqEntity.addPart("shared_image", fileBodyFb);
            httpPost.setEntity(reqEntity);

            // execute HTTP post request
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                responseStr = EntityUtils.toString(resEntity).trim();
                Log.v("upload", "Response: " + responseStr);
                try {
                    JSONObject fileRespose = new JSONObject(responseStr);
                    String status = fileRespose.optString("upload_status");
                    String message = fileRespose.optString("success");
                    if (status.equalsIgnoreCase("success")) {
                        fileLink = fileRespose.getString("file");
                        Log.v("FileLink---", fileLink);
                        JSONArray fileFbResponse = fileRespose.getJSONArray("path");
                        for (int i = 0; i < fileFbResponse.length(); i++) {
                            fbPhotoPath = fileFbResponse.getString(1);
                            Log.v("fbPhotoPath", fbPhotoPath);
                        }

                        if (var.equalsIgnoreCase(GALLERY_PAGE_NAME)) {
                            showToast("Puzzle is uploaded to Gallery page.");
                            //Utils.initiatePushNotifications(PlaySquarePuzzleActivity.this, fileLink, fbPhotoPath, PUBLIC_BROADCAST_TOPIC_NAME);
                        } else if (var.equals("Gmail")) {
                            doSendEmail();
                        } else if (var.equals("Facebook")) {
                            doInitiateFBAuthentication();
                        } else if (var.equals("Messages")) {
                            doSendSMS();
                        } else if (var.equals("Twitter")) {
                            makeTwitterBrowser(PlaySquarePuzzleActivity.this);
                        }
                        return true;
                    } else {
                        showToast(message);
                        showToast(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Something went wrong : " + e.getMessage());
            return false;
        }
        return false;
    }

	/*private void makeInstagramBrowser(final Activity ac) {
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (isInstagramInstalled())
					doShareInstagram();
				else {
					String message = "Instagram app not installed in your device";
					String InstagramId = "market://details?id=com.instagram.android";
					alertMessage(ac, message, InstagramId);
				}
			}
		});
	}*/

	/*protected void doShareInstagram() {
		Intent instagramIntent = new Intent(android.content.Intent.ACTION_SEND);

		if (uploadFbFileName != null && !uploadFbFileName.isEmpty()) {
			instagramIntent.setType("image/*");
			instagramIntent.putExtra(Intent.EXTRA_TEXT, CARRIAGE_RETURN + fileLink
					+ CARRIAGE_RETURN + textMsg + CARRIAGE_RETURN);
			instagramIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + uploadFbFileName));
		}
		instagramIntent.setPackage("com.instagram.android");
		try {
			startActivityForResult(instagramIntent, SHARE_PUZZLE_REQUEST_CODE_INSTAGRAM);

		} catch (ActivityNotFoundException e) {
			showToast("No application found on this device to perform this action");
		}
	}

	protected boolean isInstagramInstalled() {
		boolean check_info = false;
		final PackageManager pm = this.getPackageManager();

		List<ApplicationInfo> packages = pm
				.getInstalledApplications(PackageManager.GET_META_DATA);
		for (ApplicationInfo packageInfo : packages) {
			if (packageInfo.packageName.toString()
					.equals("com.instagram.android")) {
				return check_info = true;
			}
		}
		return check_info;	}*/

    Handler handler2 = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //shareToFacebookPage_Admin(100, PlaySquarePuzzleActivity.this);
        }
    };

    private void makeTwitterBrowser(final Activity ac) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isTwitterInstalled())
                    doShareTwitter();
                else {
                    String message = "Twitter app is not installed on this device.";
                    String twitterId = "market://details?id=com.twitter.android";
                    alertMessage(ac, message, twitterId);
                }
            }
        });
    }

    public static void alertMessage(final Context ctxt, String message, final String playStoreId) {
        Builder alertDialogBuilder = new Builder(ctxt);

        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Install",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                /* ((Activity) ctxt).finish(); */
                                ctxt.startActivity(new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(playStoreId)));
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private void doShareTwitter() {
        Intent twitterIntent = new Intent(Intent.ACTION_SEND);
        twitterIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        twitterIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        twitterIntent.setType("text/plain");
        twitterIntent.putExtra(Intent.EXTRA_TEXT, CARRIAGE_RETURN + fileLink);

        if (uploadFbFileName != null && !uploadFbFileName.isEmpty()) {
            twitterIntent.setType("image/jpeg");
            twitterIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(uploadFbFileName)));
        }
        twitterIntent.setPackage("com.twitter.android");
        startActivity(twitterIntent);
    }

    private boolean isTwitterInstalled() {
        boolean check_info = false;
        final PackageManager pm = this.getPackageManager();

        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals("com.twitter.android")) {
                return true;
            }
        }
        return check_info;
    }

    private void doSendSMS() {
        Intent smsIntent = new Intent();
        smsIntent.setAction(Intent.ACTION_SEND);
        smsIntent.setType("text/plain");
        smsIntent.putExtra(Intent.EXTRA_TEXT, CARRIAGE_RETURN + fileLink);

        if (uploadFbFileName != null && !uploadFbFileName.isEmpty()) {
            smsIntent.setType("image/jpeg");
            smsIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(uploadFbFileName)));
        }
        //
        // smsIntent.setPackage("com.android.mms");
        String defalutSmspackage = getDefalutSmsPackageName();
        if (defalutSmspackage != null || !defalutSmspackage.isEmpty()) {
            smsIntent.setPackage(defalutSmspackage);
        }

        try {
            startActivity(smsIntent);
        } catch (ActivityNotFoundException e) {
            showToast("No application found on this device to perform this action");
        }
    }

    private String getDefalutSmsPackageName() {
        String defalutSmsPackage = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            defalutSmsPackage = Telephony.Sms.getDefaultSmsPackage(this);

        } else {
            String defApp = Settings.Secure.getString(getContentResolver(), "sms_default_application");
            PackageManager pm = getApplicationContext().getPackageManager();
            Intent iIntent = pm.getLaunchIntentForPackage(defApp);
            ResolveInfo mInfo = pm.resolveActivity(iIntent, 0);
            defalutSmsPackage = mInfo.activityInfo.packageName;
        }
        return defalutSmsPackage;
    }

	/*public void makeFBBrowser(final Activity ac) {
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!mFacebook.isSessionValid())
					mFacebook.authorize(ac, FacebookSessionStore.PERMISSIONS,
							new FacebookLoginDialogListener());
				else {
					doShareFaceBook();

				}
			}
		});
	}

	public final class FacebookLoginDialogListener implements DialogListener {
		public void onComplete(Bundle values) {
			FacebookSessionStore.saveFBSession(mFacebook,
					PlaySquarePuzzleActivity.this);
			doShareFaceBook();
			// shareToFacebookPage(100,PlaySquarePuzzleActivity.this);
		}

		public void onFacebookError(FacebookError error) {
			error.printStackTrace();

		}

		public void onError(DialogError error) {
			error.printStackTrace();

		}

		public void onCancel() {

		}
	}*/

    protected void doSharePuzzleOnFaceBook() {
        /*Bundle parameters = new Bundle();
        parameters.putString("name", "FracturedPhoto Puzzle");
        parameters.putString("caption", "");
        parameters.putString("message", "");
        parameters.putString("description", textMsg);
        parameters.putString("link", fileLink);
        parameters.putString("picture", fbPhotoPath);

		mFacebook.dialog(this, "stream.publish", parameters, new DialogListener() {
			public void onFacebookError(FacebookError error) {
				error.printStackTrace();
			}

			public void onError(DialogError error) {
				error.printStackTrace();
			}

			public void onCancel() {
			}

			public void onComplete(Bundle values) {
			}
		});*/

        ShareDialog shareDialog = new ShareDialog(PlaySquarePuzzleActivity.this);
        if (shareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("fracturedphoto puzzle")
                    .setContentDescription(fileLink + "\n" + textMsg)
                    .setContentUrl(Uri.parse(fileLink))
                    .setImageUrl(Uri.parse(fbPhotoPath))
                    .build();

            shareDialog.show(linkContent, ShareDialog.Mode.AUTOMATIC);
        } else {
            new FP_PrefsManager(this).removeKey(FB_ACCESS_TOKEN);
        }
    }

    public void shareToFacebookPage_Admin(int requestCode, NetworkCallBack networkCallBack) {
        //123627274869506 //Sharemyplace rs
        //295372787327265 original
        final String requestUrl = "https://graph.facebook.com/295372787327265/feed";//client id
        final List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("name", "FracturedPhoto Puzzle"));
        nameValuePairs.add(new BasicNameValuePair("link", fileLink));
        nameValuePairs.add(new BasicNameValuePair("message", "FractredPhoto"));
        nameValuePairs.add(new BasicNameValuePair("picture", fbPhotoPath));
        nameValuePairs.add(new BasicNameValuePair("access_token", "EAAHvh9XwIlwBAI2vH0J3HxfxKBxITHZC5TnMBpwGTCDuCk1WWiwPUbEpv0rk1mZCLvjajD7XCCdJHZCGEDZAgYBJnA1CfM7p9uSfDJ71J0bQGY8Ccx6Fm8KMjQH8gF6g0Odf0bNSZAtcOCFZBNT8mqXGtDNNcsrHr5kUHoeL48nwZDZD"));

        final RequestHandler requestHandler = new RequestHandler(requestCode, networkCallBack);
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //HttpRestConnection2.execute(requestUrl, nameValuePairs, HttpRestConnection2.RequestMethod2.POST, requestHandler);
            }
        }).start();
    }

    private void doInitiateFBAuthentication() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        final FP_PrefsManager fpPrefsManager = new FP_PrefsManager(PlaySquarePuzzleActivity.this);
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        fpPrefsManager.save(FB_ACCESS_TOKEN, loginResult.getAccessToken().getToken());
                        doSharePuzzleOnFaceBook();
                    }

                    @Override
                    public void onCancel() {
                        showToast("Authentication Cancelled");
                        fpPrefsManager.removeKey(FB_ACCESS_TOKEN);
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        fpPrefsManager.removeKey(FB_ACCESS_TOKEN);
                        showToast(exception.getMessage());
                    }
                });

        if (Utils.isConnectionAvailable(this) && !fpPrefsManager.hasKey(FB_ACCESS_TOKEN)) {
            LoginManager.getInstance().logInWithReadPermissions(PlaySquarePuzzleActivity.this, Arrays.asList("public_profile", "email"));
        } else if (Utils.isConnectionAvailable(this) && fpPrefsManager.hasKey(FB_ACCESS_TOKEN)) {
            doSharePuzzleOnFaceBook();
        } else {
            showToast("You are offline.");
        }
    }

    private void getFacebookAutocode() {

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/Sharemyplace",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        Log.v("facebook access token", response.toString());
                        String accesstoken = response.getRequest().getAccessToken().getToken();
                        Log.v("final token", accesstoken);
                        /* handle the result */
                    }
                }
        ).executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHARE_PUZZLE_REQUEST_CODE && resultCode != Activity.RESULT_CANCELED) {
            this.finish();
        } else if (requestCode == SHARE_PUZZLE_REQUEST_CODE_INSTAGRAM && resultCode != Activity.RESULT_CANCELED) {
            this.finish();
        } else {
            //mFacebook.authorizeCallback(requestCode, resultCode, data);
            if (mCallbackManager != null && mCallbackManager.onActivityResult(requestCode, resultCode, data)) {
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindIAPService();
        setActivityStopped(false);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        /*
         * SharedPreferences prfs = getSharedPreferences(
         * "AUTHENTICATION_FILE_NAME", Context.MODE_PRIVATE); String Astatus =
         * prfs.getString("Authentication_Status", "");
         *
         *
         * SharedPreferences prfs1 = getSharedPreferences(
         * "AUTHENTICATION_FILE_NAME_msg", Context.MODE_PRIVATE); String
         * Astatus1 = prfs1.getString("Authentication_Status", ""); String msg =
         * prfs1.getString("Authentication_Msg", "");
         *
         * if (Astatus.equals("true")) {
         * relativeLayout.setVisibility(View.GONE);
         *
         * }else if(Astatus1.equals("true")){ if(msg.equalsIgnoreCase(
         * "Error: Error purchasing: labResult: User canceled. (response: -1005: User cancelled)"
         * )) relativeLayout.setVisibility(View.VISIBLE); else
         * relativeLayout.setVisibility(View.GONE); }
         *
         * else { relativeLayout.setVisibility(View.VISIBLE); }
         */
        orientationManager = new OrientationManager(this, SensorManager.SENSOR_DELAY_NORMAL, this, is10InchTablet());
        orientationManager.enable();
    }

    public boolean is10InchTablet() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float scaleFactor = metrics.density;
        float widthDp = widthPixels / scaleFactor;
        float heightDp = heightPixels / scaleFactor;
        float smallestWidth = Math.min(widthDp, heightDp);
        return smallestWidth >= 719;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (orientationManager != null) {
            orientationManager.disable();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setActivityStopped(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        setActivityStopped(true);
        if (progressBar != null && progressBar.isShowing()) {
            progressBar.dismiss();
        }
    }

    private void setActivityStopped(boolean isStopped) {
        this.isActivityStopped = isStopped;
    }

    private boolean isActivityStopped() {
        return isActivityStopped;
    }

    @Override
    protected void onDestroy() {
        if (photoBitmap != null && !photoBitmap.isRecycled()) {
            photoBitmap.recycle();
            photoBitmap = null;
        }
        // Log.d(TAG, "Destroying helper.");
        /*
         * if (mHelper != null) { mHelper.dispose(); mHelper = null; }
         */
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        FracturePhotoApplication.setOriginalTilesListSquare(tilesList);

        outState.putBoolean("isSharedPuzzle", isSharedPuzzle);
        outState.putBoolean("isDuplicateShareClick", isDuplicateShareClick);
        outState.putBoolean("isCreatePuzzleMode", isCreatePuzzleMode);
        outState.putBoolean("isGridTouched", isGridTouched);
        outState.putString("metaDataFilePath", metaDataFilePath);
        outState.putString("imagePathToShare", imagePathToShare);
        outState.putString("photoPath", photoPath);
        outState.putString("puzzleName", puzzleName);
        outState.putInt("numOfRows", numOfRows);
        outState.putInt("numOfColsInGrid", numOfColsInGrid);
        outState.putInt("scale", scale);
        outState.putInt("numOfPieces", numOfPieces);
        outState.putInt("touchedItemPos", touchedItemPos);
        outState.putInt("screenWidth", screenWidth);
        outState.putInt("bmpWidth", bmpWidth);
        outState.putInt("bmpHeight", bmpHeight);
        outState.putLong("savedSharedPuzzleRowId", savedSharedPuzzleRowId);
        outState.putString("pathOfDuplicateSharedPhoto",
                pathOfDuplicateSharedPhoto);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        tilesList = FracturePhotoApplication.getOriginalTilesListSquare();

        isSharedPuzzle = savedInstanceState.getBoolean("isSharedPuzzle");
        isDuplicateShareClick = savedInstanceState.getBoolean("isDuplicateShareClick");
        isCreatePuzzleMode = savedInstanceState.getBoolean("isCreatePuzzleMode");
        isGridTouched = savedInstanceState.getBoolean("isGridTouched");
        metaDataFilePath = savedInstanceState.getString("metaDataFilePath");
        imagePathToShare = savedInstanceState.getString("imagePathToShare");
        photoPath = savedInstanceState.getString("photoPath");
        puzzleName = savedInstanceState.getString("puzzleName");
        numOfRows = savedInstanceState.getInt("numOfRows", numOfRows);
        numOfColsInGrid = savedInstanceState.getInt("numOfColsInGrid", numOfColsInGrid);
        scale = savedInstanceState.getInt("scale", scale);
        numOfPieces = savedInstanceState.getInt("numOfPieces", numOfPieces);
        touchedItemPos = savedInstanceState.getInt("touchedItemPos", touchedItemPos);
        screenWidth = savedInstanceState.getInt("screenWidth", screenWidth);
        bmpWidth = savedInstanceState.getInt("bmpWidth", bmpWidth);
        bmpHeight = savedInstanceState.getInt("bmpHeight", bmpHeight);
        savedSharedPuzzleRowId = savedInstanceState.getLong("savedSharedPuzzleRowId");
        pathOfDuplicateSharedPhoto = savedInstanceState.getString("pathOfDuplicateSharedPhoto");

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (adapter != null && !adapter.isPuzzleSolved()) {
            Builder closeAlertBox = new Builder(PlaySquarePuzzleActivity.this);
            closeAlertBox.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (isSharedPuzzle) {
                                Intent i = new Intent(PlaySquarePuzzleActivity.this, DashboardActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                            }
                            PlaySquarePuzzleActivity.this.finish();
                        }
                    });

            closeAlertBox.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            closeAlertBox.setTitle("Confirm");
            closeAlertBox.setMessage("Are you sure you want to quit the puzzle?");
            closeAlertBox.setCancelable(false);
            closeAlertBox.show();
        } else if (adapter != null && adapter.isPuzzleSolved()) {
            Intent i = new Intent(PlaySquarePuzzleActivity.this, DashboardActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            PlaySquarePuzzleActivity.this.finish();
        } else {
            super.onBackPressed();
        }
    }

    public void displayAds() {
        // Create an ad.
        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);

        adView.setAdUnitId(getString(string.banner_ad_unit_id));

        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a
        // physical device.
        // AdView adView = (AdView)this.findViewById(R.id.adView);
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

    @Override
    public void onSuccess(int requestCode, Object object) {
        Log.v("TAG", "Response From Server:" + object.toString());
        showToast("Posted on Facebook Fractured Photo Page");
        dismissProgressDialog();
    }

    @Override
    public void onFailure(int requestCode, String message) {
        Log.v("TAG", requestCode + " =====> Response From Server:" + message);
        dismissProgressDialog();
    }

    @Override
    public void onCancelled() {
        dismissProgressDialog();
    }

    public void showProgressDialog() {
        try {
            if (dialog == null) {
                dialog = ProgressDialog.show(PlaySquarePuzzleActivity.this, "", "Please wait..");
            }
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissProgressDialog() {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
                dialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOrientationChange(OrientationManager.ScreenOrientation screenOrientation) {
        saveButton.setBackgroundColor(Color.TRANSPARENT);
        previewButton.setBackgroundColor(Color.TRANSPARENT);
        sharePuzzleButton.setBackgroundColor(Color.TRANSPARENT);
        turnLeftImage.setBackgroundColor(Color.TRANSPARENT);
        turnRightImage.setBackgroundColor(Color.TRANSPARENT);
        fpLogo.setBackgroundColor(Color.TRANSPARENT);
        fpLogo.setImageResource(0);

        switch (screenOrientation) {
            case PORTRAIT:
                saveButton.setImageResource(R.drawable.save_background);
                previewButton.setImageResource(R.drawable.preview_background);
                sharePuzzleButton.setImageResource(R.drawable.share_background);
                turnLeftImage.setImageResource(R.drawable.left_rotate);
                turnRightImage.setImageResource(R.drawable.right_rotate);
                fpLogo.setImageResource(R.drawable.small_logo);

                saveButton.setRotation(0);
                previewButton.setRotation(0);
                sharePuzzleButton.setRotation(0);
                turnLeftImage.setRotation(0);
                turnRightImage.setRotation(0);
                fpLogo.setRotation(0);
                rotateTo = 0;
                if (adapter != null && adapter.congratsParentLayout != null) {
                    adapter.congratsParentLayout.setAngle(0);
                }
                break;

            case REVERSED_PORTRAIT:
                saveButton.setImageResource(R.drawable.save_background);
                previewButton.setImageResource(R.drawable.preview_background);
                sharePuzzleButton.setImageResource(R.drawable.share_background);
                turnLeftImage.setImageResource(R.drawable.left_rotate);
                turnRightImage.setImageResource(R.drawable.right_rotate);
                fpLogo.setImageResource(R.drawable.small_logo);

                saveButton.setRotation(180);
                previewButton.setRotation(180);
                sharePuzzleButton.setRotation(180);
                turnLeftImage.setRotation(180);
                turnRightImage.setRotation(180);
                fpLogo.setRotation(180);
                rotateTo = 180;
                if (adapter != null && adapter.congratsParentLayout != null) {
                    adapter.congratsParentLayout.setAngle(180);
                }
                break;

            case REVERSED_LANDSCAPE:
                saveButton.setImageResource(R.drawable.save_background_land);
                previewButton.setImageResource(R.drawable.preview_background_land);
                sharePuzzleButton.setImageResource(R.drawable.share_background_land);
                turnLeftImage.setImageResource(R.drawable.left_rotate_land);
                turnRightImage.setImageResource(R.drawable.right_rotate_land);
                fpLogo.setImageResource(R.drawable.small_logo_land);

                saveButton.setRotation(270);
                previewButton.setRotation(270);
                sharePuzzleButton.setRotation(270);
                turnLeftImage.setRotation(270);
                turnRightImage.setRotation(270);
                fpLogo.setRotation(270);
                rotateTo = 90;
                if (adapter != null && adapter.congratsParentLayout != null) {
                    adapter.congratsParentLayout.setAngle(90);
                }
                break;

            case LANDSCAPE:
                saveButton.setImageResource(R.drawable.save_background_land);
                previewButton.setImageResource(R.drawable.preview_background_land);
                sharePuzzleButton.setImageResource(R.drawable.share_background_land);
                turnLeftImage.setImageResource(R.drawable.left_rotate_land);
                turnRightImage.setImageResource(R.drawable.right_rotate_land);
                fpLogo.setImageResource(R.drawable.small_logo_land);

                saveButton.setRotation(90);
                previewButton.setRotation(90);
                sharePuzzleButton.setRotation(90);
                turnLeftImage.setRotation(90);
                turnRightImage.setRotation(90);
                fpLogo.setRotation(90);
                rotateTo = 270;
                if (adapter != null && adapter.congratsParentLayout != null) {
                    adapter.congratsParentLayout.setAngle(270);
                }
                break;
        }
    }

    public void clickAlertDialog() {
        OkDialog okDialog = OkDialog.newInstance("This is the default content Dialog\n Current dialog rotation is : ");
        okDialog.show(getFragmentManager(), 180);

        autoRotateDialog(okDialog);
    }

    private void autoRotateDialog(final BaseDialogFragment dialogFragment) {

        Timer mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            int count = 0;
            @Override
            public void run() {
                if (dialogFragment == null || !dialogFragment.isResumed()) {
                    cancel();
                    return;
                }
                //change to UI thread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogFragment.setRotation(count++ % 4);
                    }
                });
            }
        }, 500, 1500);
    }
}
