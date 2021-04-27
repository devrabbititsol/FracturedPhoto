package com.logictreeit.android.fracturedphoto.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
//import com.crashlytics.android.Crashlytics;
import com.dran.fracturedphoto.R;
import com.dran.fracturedphoto.R.string;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.logictreeit.android.fracturedphoto.adapters.SharePopupAdapter;
import com.logictreeit.android.fracturedphoto.adapters.ShatteredPuzzleGalleryAdapter;
import com.logictreeit.android.fracturedphoto.app.FracturePhotoApplication;
import com.logictreeit.android.fracturedphoto.custom_ui.CustomGalleryView;
import com.logictreeit.android.fracturedphoto.custom_ui.RotateLayout;
import com.logictreeit.android.fracturedphoto.custom_ui.ShatteredPuzzleView;
import com.logictreeit.android.fracturedphoto.db.FracturePhotoDB;
import com.logictreeit.android.fracturedphoto.db.FracturePhotoDBModel;
import com.logictreeit.android.fracturedphoto.db.SampleCursorLoader;
import com.logictreeit.android.fracturedphoto.helpers.AppsInfo;
import com.logictreeit.android.fracturedphoto.helpers.BitmapHelper;
import com.logictreeit.android.fracturedphoto.helpers.Mask;
import com.logictreeit.android.fracturedphoto.helpers.MaskBuilder;
import com.logictreeit.android.fracturedphoto.helpers.ZipHelper;
import com.logictreeit.android.fracturedphoto.listeners.DragOntoPuzzleViewListener;
import com.logictreeit.android.fracturedphoto.listeners.OrientationManager;
import com.logictreeit.android.fracturedphoto.models.ShatteredTile;
import com.logictreeit.android.fracturedphoto.utils.ApplicationConstants;
import com.logictreeit.android.fracturedphoto.utils.FP_PrefsManager;
import com.logictreeit.android.fracturedphoto.utils.PuzzleType;
import com.logictreeit.android.fracturedphoto.utils.Utils;
import com.logictreeit.android.fracturedphoto.network.NetworkCallBack;
import com.logictreeit.android.fracturedphoto.network.RequestHandler;
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
import org.json.JSONObject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class PlayShatteredPuzzleActivity extends Activity implements OnClickListener, ApplicationConstants, NetworkCallBack, View.OnLongClickListener, OrientationManager.OrientationListener {
    protected static final String DEBUG_TAG = null;
    private static final Random RANDOM = new Random();
    private static final int SHARE_PUZZLE_REQUEST_CODE = 1234;
    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final int BILLING_RESPONSE_RESULT_OK = 0;
    private static final String RESPONSE_BUY_INTENT = "BUY_INTENT";
    private static final String TAG = PlayShatteredPuzzleActivity.class.getSimpleName();
    private static final int SHARE_PUZZLE_REQUEST_CODE_INSTAGRAM = 9999;
    public final Handler handler = new Handler();
    public ShatteredPuzzleView puzzleView;
    public ShatteredPuzzleGalleryAdapter galleryAdapter;
    public Bitmap photoBitmap;
    public RelativeLayout relativeLayout;
    public AdView adView, ad;
    protected long savedSharedPuzzleRowId;
    public RelativeLayout ll;
    public View mView;
    private String uploadPuzzleUrl = FP_BASE_URL + "upload.php";
    private String adminPuzzleUploadUrl = FP_BASE_URL + "admin_upload.php";
    private String fileLink, fbPhotoPath;
    private String uploadFPFileName, deviceid, responseStr, puzzleScreenshotImagePath, twitterPath, rootPath, pathOfDuplicateSharedPhoto;
    private File myDir;
    private CustomGalleryView hrzntlListView;
    private ArrayList<ShatteredTile> tilesList = new ArrayList<ShatteredTile>();
    private ArrayList<ShatteredTile> galleryTilesList = new ArrayList<ShatteredTile>();
    private ImageView previewPuzzle, sharePuzzleImage, savePuzzle, turnLeft, turnRight, fpLogo;
    private String pzlRowId, imageUriString, photoPath, imagePathToShare, metaDataFilePath, puzzleName;
    private boolean isSharedPuzzle, isDuplicateShareClick, isActivityStopped, isFirstTileDropped, isCreatePuzzleMode, share_flag;
    private int orientation;
    private int availableWidth = 0;
    private int availableHeight = 0;
    private int patternType;
    private Uri imageUri;
    private File cachedPicturesFolder, cacheFolder;
    private ProgressDialog savingProgressWheel;
    private IInAppBillingService mService;
    private ImageView goLeft;
    private ImageView goRight;
    private CallbackManager mCallbackManager;

    private ServiceConnection mServiceConn = new ServiceConnection() {
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
    private ProgressDialog dialog;
    private Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //shareToFacebookPage_Admin(100, PlayShatteredPuzzleActivity.this);
        }
    };
    private OrientationManager orientationManager;
    private Dialog congratsDialog;
    private RotateLayout congratsParentLayout;
    public static int initialRotation = 0;

    /*
     * private void DeleteRecursive(File fileOrDirectory) { if
     * (fileOrDirectory.isDirectory()) for (File child :
     * fileOrDirectory.listFiles()) DeleteRecursive(child);
     *
     * fileOrDirectory.delete(); }
     */
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

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.goLeft:
                if (galleryAdapter != null && galleryAdapter.getCount() >= 1 && hrzntlListView != null && hrzntlListView.getSelectedItemPosition() != 0) {
                    hrzntlListView.setSelection(0);
                }
                break;
            case R.id.goRight:
                if (galleryAdapter != null && galleryAdapter.getCount() >= 1 && hrzntlListView != null && hrzntlListView.getSelectedItemPosition() != galleryAdapter.getCount() - 1) {
                    hrzntlListView.setSelection(galleryAdapter.getCount() - 1);
                }
                break;
            default:
                break;
        }
        return true;
    }

    public static void alertMessage(final Context ctxt, String message, final String playsStoreId) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctxt);
        alertDialogBuilder.setMessage(message).setCancelable(false).setNegativeButton("Cancel", null).setPositiveButton("Install",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ctxt.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(playsStoreId)));
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        setContentView(R.layout.play_shattered_puzzle);
        ll = (RelativeLayout) findViewById(R.id.ll);
        mView = getLayoutInflater().inflate(R.layout.ad_layout, ll, true);
        relativeLayout = (RelativeLayout) mView.findViewById(R.id.relative_layout);
        ad = (AdView) mView.findViewById(R.id.adView);
        goLeft = (ImageView) findViewById(R.id.goLeft);
        goLeft.setOnLongClickListener(this);
        goLeft.setOnClickListener(this);
        goRight = (ImageView) findViewById(R.id.goRight);
        goRight.setOnLongClickListener(this);
        goRight.setOnClickListener(this);

        hrzntlListView = (CustomGalleryView) findViewById(R.id.horizontal_list_view);
        rootPath = SDCARD_PATH + File.separator + getString(string.app_name) + "_SharedPuzzles" + File.separator;
        turnLeft = (ImageView) findViewById(R.id.turnLeft);
        turnLeft.setOnClickListener(this);

        turnRight = (ImageView) findViewById(R.id.turnRight);
        turnRight.setOnClickListener(this);

        puzzleView = (ShatteredPuzzleView) findViewById(R.id.puzzleView);
        puzzleView.setDrawingCacheEnabled(true);
        puzzleView.setOnDragListener(new DragOntoPuzzleViewListener(this, puzzleView));
        previewPuzzle = (ImageView) findViewById(R.id.preview);
        previewPuzzle.setOnClickListener(this);
        savePuzzle = (ImageView) findViewById(R.id.savePuzzleImage);
        sharePuzzleImage = (ImageView) findViewById(R.id.sharePuzzleImage);
        sharePuzzleImage.setOnClickListener(this);
        savePuzzle.setOnClickListener(this);
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
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            photoPath = intent.getStringExtra(Extras_Keys.PHOTO_PATH);
            orientation = intent.getIntExtra(Extras_Keys.PHOTO_ORIENTATION, 0);
            imageUriString = intent.getStringExtra(Extras_Keys.PHOTO_URI);

            if (imageUriString != null) {
                imageUri = Uri.parse(imageUriString);
            }
            isCreatePuzzleMode = intent.getBooleanExtra(Extras_Keys.IS_CREATE_PUZZLE_MODE, true);

            Bitmap decodedBitmap = BitmapHelper.decodeBitmapFromPath(photoPath, 640, 480, orientation);
            if (decodedBitmap == null) {
                showToast("Something went wrong, Please try again later");
                finish();
                return;
            }
            if (isCreatePuzzleMode) {
                Log.v("isCreatePuzzleMode", "isCreatePuzzleMode");
                System.out.println("Decoded bitmap W & H = " + decodedBitmap.getWidth() + " : " + decodedBitmap.getHeight());
                availableWidth = (int) Utils.convertDpToPixels((int) Utils.convertPixelsToDp(getResources().getDisplayMetrics().widthPixels, getApplicationContext()) - 40, getApplicationContext());
                availableHeight = (int) Utils.convertDpToPixels((int) Utils.convertPixelsToDp(getResources().getDisplayMetrics().heightPixels, getApplicationContext()) - 256, getApplicationContext());
                // Added below check to make the long side of photo match with
                // long side of puzzle assembly area
                if (decodedBitmap.getWidth() > decodedBitmap.getHeight()) {
                    int temp = availableWidth;
                    availableWidth = availableHeight;
                    availableHeight = temp;
                }
                System.out.println("Available width, height for Puzzle = " + availableWidth + ", " + availableHeight);
                if ((availableWidth * decodedBitmap.getHeight()) / decodedBitmap.getWidth() < availableHeight) {
                    photoBitmap = BitmapHelper.getResizedBitmap(decodedBitmap, availableWidth, (availableWidth * decodedBitmap.getHeight()) / decodedBitmap.getWidth());
                } else {
                    photoBitmap = BitmapHelper.getResizedBitmap(decodedBitmap, availableWidth, availableHeight);
                }
                System.err.println("Final bitmap W & H = " + photoBitmap.getWidth() + " : " + photoBitmap.getHeight());
                // Added the below check to make the long side of photo match
                // with long side of puzzle assembly area
                if (photoBitmap.getWidth() > photoBitmap.getHeight()) {
                    photoBitmap = BitmapHelper.getRotatedBitmap(photoBitmap, 90);
                }
                puzzleView.getLayoutParams().width = photoBitmap.getWidth();
                puzzleView.getLayoutParams().height = photoBitmap.getHeight();

                System.err.println("Canvas W & H = " + puzzleView.getLayoutParams().width + " : " + puzzleView.getLayoutParams().height);

                float wScaleFactor = photoBitmap.getWidth() / 640F;
                float hScaleFactor = photoBitmap.getHeight() / 480F;

                System.err.println("wScaleFactor = " + wScaleFactor);
                System.err.println("hScaleFactor = " + hScaleFactor);
                patternType = RANDOM.nextInt(3);

                Log.v("patternType========", "" + patternType);
                List<Mask> masksList = MaskBuilder.loadMasks(patternType, wScaleFactor, hScaleFactor);

                ArrayList<Integer> currentPositions = new ArrayList<>();
                int size = masksList.size();
                for (int i = 0; i < size; ++i) {
                    currentPositions.add(i);
                }
                Collections.shuffle(currentPositions);
                HashMap<Integer, Integer> idsToPositionsMap = new HashMap<>();
                size = currentPositions.size();
                for (int i = 0; i < size; ++i) {
                    idsToPositionsMap.put(i, currentPositions.get(i));
                    tilesList.add(null);
                    galleryTilesList.add(null);
                }
                size = masksList.size();
                ArrayList<ShatteredTile> droppedTilesList = new ArrayList<>();
                int count = 0;

                for (int id = 0; id < size; ++id) {
                    Mask mask = masksList.get(id);

                    Bitmap maskBitmap = BitmapFactory.decodeResource(getResources(), mask.getResourceID(), options);
                    maskBitmap = BitmapHelper.getResizedBitmap(maskBitmap, (int) (maskBitmap.getWidth() * wScaleFactor), (int) (maskBitmap.getHeight() * hScaleFactor));
                    Bitmap pieceBitmap = Bitmap.createBitmap(photoBitmap, (int) mask.getX1(), (int) mask.getY1(), maskBitmap.getWidth(), maskBitmap.getHeight());
                    pieceBitmap.setHasAlpha(true);
                    Canvas cnvs = new Canvas(pieceBitmap);
                    Paint paint = new Paint();
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                    cnvs.drawBitmap(maskBitmap, 0, 0, paint);
                    maskBitmap.recycle();

                    short angle = (short) RANDOM.nextInt(4);

                    Bitmap rotatedBitmap = BitmapHelper.getRotatedBitmap(pieceBitmap, angle * 90);

                    int currentX;
                    int currentY;
                    // Check if it is a center mask/tile, then we need to drop
                    // it on puzzle view.
                    // Hence, we need RANDOM values as currentX and currentY of
                    // that tile.
                    // So currentX and currentY Should not be zero for centered
                    // tiles.
                    if (mask.isCenterMask()) {
                        currentX = RANDOM.nextInt(Math.abs(photoBitmap.getWidth() - rotatedBitmap.getWidth()));
                        currentY = RANDOM.nextInt(Math.abs(photoBitmap.getHeight() - rotatedBitmap.getHeight()));

                        if (count == 0) {// 1st center piece
                            currentX = 5;
                            currentY = 5;

                        } else if (count == 1) {// 2nd center peice
                            currentX = 5;
                            currentY = photoBitmap.getHeight() - rotatedBitmap.getHeight() - 5;

                        } else if (count == 2) {// 3rd center peice
                            currentX = photoBitmap.getWidth() - rotatedBitmap.getWidth() - 5;
                            currentY = 5;

                        } else if (count == 3) {// 4th center peice
                            currentX = photoBitmap.getWidth() - rotatedBitmap.getWidth() - 5;
                            currentY = photoBitmap.getHeight() - rotatedBitmap.getHeight() - 5;

                        } else if (count == 4) {// 5th center peice
                            currentX = photoBitmap.getWidth() / 2 - rotatedBitmap.getWidth() / 2;
                            currentY = photoBitmap.getHeight() / 2 - rotatedBitmap.getHeight() / 2;

                        } else if (count == 5) {// 6th center peice
                            currentX = 5;
                            currentY = photoBitmap.getHeight() / 2 - rotatedBitmap.getHeight() / 2;

                        } else if (count == 6) {// 7th center peice
                            currentX = photoBitmap.getWidth() / 2 - rotatedBitmap.getWidth() / 2;
                            currentY = 5;

                        } else if (count == 7) {// 8th center peice
                            currentX = photoBitmap.getWidth() / 2 - rotatedBitmap.getWidth() / 2;
                            currentY = photoBitmap.getHeight() - rotatedBitmap.getHeight() - 5;

                        } else if (count == 8) {// 9th center peice
                            currentX = photoBitmap.getWidth() - rotatedBitmap.getWidth() - 5;
                            currentY = photoBitmap.getHeight() / 2 - rotatedBitmap.getHeight() / 2;
                        }
                        ++count;
                    } else {// reset currentX and currentY to ZERO if the tile
                        // is not center.
                        currentX = 0;
                        currentY = 0;
                    }

                    ShatteredTile tile = new ShatteredTile(this, id, currentX, currentY, pieceBitmap,
                            rotatedBitmap, angle, (int) mask.getX1(), (int) mask.getY1(), (int) mask.getX2(),
                            (int) mask.getY2(), patternType, puzzleView);
                    // ShatteredTile tile = new ShatteredTile(this, id,
                    // currentX, currentY, pieceBitmap, rotatedBitmap, angle,
                    // (int)mask.getX1(), (int)mask.getY1(), (int)mask.getX2(),
                    // (int)mask.getY2(), patternType, puzzleView);
                    tile.setCenterPieceOfPuzzle(mask.isCenterMask());
                    tile.setCurrentPosition(idsToPositionsMap.get(id));
                    tilesList.set(idsToPositionsMap.get(id), tile);
                    galleryTilesList.set(idsToPositionsMap.get(id), tile);
                }

                for (ShatteredTile tile : tilesList) {
                    if (tile.isCenterPieceOfPuzzle()) {
                        tile.setDroppedOnPuzzleView(true);
                        droppedTilesList.add(tile);
                        galleryTilesList.remove(tile);
                    }
                }

                if (savedInstanceState != null && FracturePhotoApplication.getOriginalTilesList() != null) {
                    tilesList = FracturePhotoApplication.getOriginalTilesList();
                }
                if (savedInstanceState != null && FracturePhotoApplication.getGalleryTilesList() != null) {
                    galleryTilesList = FracturePhotoApplication.getGalleryTilesList();
                }

                System.out.println("droppedTilesList size" + droppedTilesList.size());

                puzzleView.doInitialSetupWithDroppedTiles(this, tilesList, droppedTilesList);
                galleryAdapter = new ShatteredPuzzleGalleryAdapter(PlayShatteredPuzzleActivity.this, galleryTilesList);
                System.out.println("what is in galleryTilesList size" + galleryTilesList.size());
                hrzntlListView.setAdapter(galleryAdapter);

            } else {
                Log.v("SOLVE PUZZLE MODE", "we are in SOLVE PUZZLE MODE");
                // we are in SOLVE PUZZLE MODE...i.e SOLVING THE SAVED PUZZLE
                pzlRowId = intent.getStringExtra(Extras_Keys.PUZZLE_HISTORY_ROW_ID_IN_DB);
                System.err.println("Decoded bitmap W & H = " + decodedBitmap.getWidth() + " : " + decodedBitmap.getHeight());
                availableWidth = (int) Utils.convertDpToPixels((int) Utils.convertPixelsToDp(getResources().getDisplayMetrics().widthPixels, getApplicationContext()) - 40, getApplicationContext());
                availableHeight = (int) Utils.convertDpToPixels((int) Utils.convertPixelsToDp(getResources().getDisplayMetrics().heightPixels, getApplicationContext()) - 256, getApplicationContext());
                System.err.println("Available width, height for Puzzle = " + availableWidth + ", " + availableHeight);
                // Added the below check to make the long side of photo match
                // with long side of puzzle assembly area
                if (decodedBitmap.getWidth() > decodedBitmap.getHeight()) {
                    int temp = availableWidth;
                    availableWidth = availableHeight;
                    availableHeight = temp;
                }
                if ((availableWidth * decodedBitmap.getHeight()) / decodedBitmap.getWidth() < availableHeight) {
                    photoBitmap = BitmapHelper.getResizedBitmap(decodedBitmap, availableWidth, (availableWidth * decodedBitmap.getHeight()) / decodedBitmap.getWidth());
                } else {
                    photoBitmap = BitmapHelper.getResizedBitmap(decodedBitmap, availableWidth, availableHeight);
                }
                System.err.println("Final bitmap W & H = " + photoBitmap.getWidth() + " : " + photoBitmap.getHeight());
                // Added the below check to make the long side of photo match
                // with long side of puzzle assembly area
                if (photoBitmap.getWidth() > photoBitmap.getHeight()) {
                    photoBitmap = BitmapHelper.getRotatedBitmap(photoBitmap, 90);
                }
                puzzleView.getLayoutParams().width = photoBitmap.getWidth();
                puzzleView.getLayoutParams().height = photoBitmap.getHeight();

                // load from db and create tiles list and set adapter
                FracturePhotoDB db = new FracturePhotoDB(getApplicationContext());
                db.openDB();
                Cursor c = new SampleCursorLoader(getApplicationContext(), db, "loadShatteredTilesDetailsOfPuzzleId", pzlRowId).loadInBackground();
                c.moveToFirst();
                int count = c.getCount();
                ArrayList<ShatteredTile> droppedTilesList = new ArrayList<ShatteredTile>();

                for (int i = 0; i < count; ++i) {
                    String center = c.getString(c.getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_TILE_CENTER_PIECE));
                    boolean centerpiece = Boolean.parseBoolean(center);
                    byte[] origImageArray = c.getBlob(c.getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_TILE_ORIGINAL_BITMAP));
                    // byte[] rotatedImageArray =
                    // c.getBlob(c.getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_TILE_ROTATED_BITMAP));
                    ShatteredTile tile = new ShatteredTile(
                            this,
                            Integer.parseInt(c.getString(c
                                    .getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_TILE_ID))),
                            Integer.parseInt(c.getString(c
                                    .getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_TILE_CURRENT_X))),
                            Integer.parseInt(c.getString(c
                                    .getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_TILE_CURRENT_Y))),
                            BitmapHelper.getBitmapFromByteArray(origImageArray),
                            BitmapHelper.getRotatedBitmap(
                                    BitmapHelper
                                            .getBitmapFromByteArray(origImageArray),
                                    Integer.parseInt(c.getString(c
                                            .getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_TILE_ROTATION))) * 90)/*
                                                                     * BitmapHelper
																	 * .
																	 * getBitmapFromByteArray
																	 * (
																	 * rotatedImageArray
																	 * )
																	 */,
                            (short) Integer.parseInt(c.getString(c
                                    .getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_TILE_ROTATION))),
                            Integer.parseInt(c.getString(c
                                    .getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_TILE_ORIGINAL_X1))),
                            Integer.parseInt(c.getString(c
                                    .getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_TILE_ORIGINAL_Y1))),
                            Integer.parseInt(c.getString(c
                                    .getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_TILE_ORIGINAL_X2))),
                            Integer.parseInt(c.getString(c
                                    .getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_TILE_ORIGINAL_Y2))),
                            patternType = c.getInt(c
                                    .getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_PUZZLE_PATTERN_TYPE)),
                            puzzleView);
                    tile.setCenterPieceOfPuzzle(centerpiece); // center piece or
                    // not??
                    // actually, not
                    // required this
                    // here
                    tile.setDroppedOnPuzzleView(Boolean.parseBoolean(c.getString(c
                            .getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_TILE_ISDROPPED))));
                    tile.setSurroundingTiles(Utils.getArrayListFromStringLine(c.getString(c
                            .getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_TILE_SURROUNDINGTILES))));
                    tile.setAttachedWith(Utils.getArrayListFromStringLine(c.getString(c
                            .getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_TILE_ATTACHED_TILES))));
                    tile.setTileRecycled((Boolean.parseBoolean(c.getString(c
                            .getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_TILE_IS_RECYCLED)))));
                    tile.setCurrentPosition(c.getInt(c
                            .getColumnIndexOrThrow(FracturePhotoDBModel.COL_SHATTERED_TILE_CURRENT_POSITION)));

                    tilesList.add(tile);
                    if (!tile.isDroppedOnPuzzleView()) {
                        galleryTilesList.add(tile);
                    } else {
                        droppedTilesList.add(tile);
                    }
                    c.moveToNext();
                }
                db.closeDB();

                if (savedInstanceState != null && FracturePhotoApplication.getOriginalTilesList() != null) {
                    tilesList = FracturePhotoApplication.getOriginalTilesList();
                }
                if (savedInstanceState != null && FracturePhotoApplication.getGalleryTilesList() != null) {
                    galleryTilesList = FracturePhotoApplication.getGalleryTilesList();
                }
                System.out.println("droppedTilesList size save" + droppedTilesList.size());
                puzzleView.doInitialSetupWithDroppedTiles(this, tilesList, droppedTilesList);

                galleryAdapter = new ShatteredPuzzleGalleryAdapter(PlayShatteredPuzzleActivity.this, galleryTilesList);
                System.out.println("what is in galleryTilesList size save" + galleryTilesList.size());
                hrzntlListView.setAdapter(galleryAdapter);
            }
        } else {
            Log.v("share PUZZLE MODE", "we are in share PUZZLE MODE");
            // solving SHARED PUZZLE
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            photoPath = intent.getStringExtra(Extras_Keys.PHOTO_PATH);
            patternType = intent.getIntExtra(Extras_Keys.PATTERN_TYPE, 1);
            if (new File(photoPath).exists()) {
                metaDataFilePath = intent.getStringExtra(Extras_Keys.META_DATA_FILE_PATH);
                if (new File(metaDataFilePath).exists()) {
                    Bitmap decodedBitmap = BitmapHelper.decodeBitmapFromPath(photoPath, 640, 480, 0);
                    int availableWidth = 0;
                    int availableHeight = 0;
                    if (decodedBitmap == null) {
                        showToast("Something went wrong, Please try again later");
                        finish();
                        return;
                    }
                    System.err.println("Decoded bitmap W & H = " + decodedBitmap.getWidth() + " : " + decodedBitmap.getHeight());
                    availableWidth = (int) Utils.convertDpToPixels((int) Utils.convertPixelsToDp(getResources().getDisplayMetrics().widthPixels, getApplicationContext()) - 40, getApplicationContext());
                    availableHeight = (int) Utils.convertDpToPixels((int) Utils.convertPixelsToDp(getResources().getDisplayMetrics().heightPixels, getApplicationContext()) - 256, getApplicationContext());
                    System.err.println("Available width, height for Puzzle = " + availableWidth + ", " + availableHeight);
                    if ((availableWidth * decodedBitmap.getHeight()) / decodedBitmap.getWidth() < availableHeight) {
                        photoBitmap = BitmapHelper.getResizedBitmap(decodedBitmap, availableWidth, (availableWidth * decodedBitmap.getHeight()) / decodedBitmap.getWidth());
                    } else {
                        photoBitmap = BitmapHelper.getResizedBitmap(decodedBitmap, availableWidth, availableHeight);
                    }
                    System.err.println("Final bitmap W & H = " + photoBitmap.getWidth() + " : " + photoBitmap.getHeight());
                    // Added the below check to make the long side of photo match
                    // with long side of puzzle assembly area....................
                    if (photoBitmap.getWidth() > photoBitmap.getHeight()) {
                        photoBitmap = BitmapHelper.getRotatedBitmap(photoBitmap, 90);
                    }
                    puzzleView.getLayoutParams().width = photoBitmap.getWidth();
                    puzzleView.getLayoutParams().height = photoBitmap.getHeight();

                    System.err.println("Canvas W & H = " + puzzleView.getLayoutParams().width + " : " + puzzleView.getLayoutParams().height);

                    float wScaleFactor = photoBitmap.getWidth() / 640F;
                    float hScaleFactor = photoBitmap.getHeight() / 480F;

                    System.err.println("wScaleFactor = " + wScaleFactor);
                    System.err.println("hScaleFactor = " + hScaleFactor);

                    List<Mask> masksList = MaskBuilder.loadMasks(patternType, wScaleFactor, hScaleFactor);

                    //READ META FILE HERE AND ARRANGE ALL THE IMAGES ACCORDINGLY
                    ArrayList<String[]> metaTokensList = Utils.readMetaDataFile(metaDataFilePath);
                    boolean isSharedFromAndroid = false;
                    if (Integer.parseInt(metaTokensList.get(0)[13]) == 1) {//if the puzzle is shared from Android
                        isSharedFromAndroid = true;
                    }
                    int numOfTiles = Utils.getNumOfTilesInPattern(patternType);//metaTokensList.size();
                    if (numOfTiles > metaTokensList.size()) {
                        metaTokensList = Utils.doValidateMetaTokens(metaTokensList, numOfTiles, isSharedFromAndroid);
                    }
                    HashMap<Integer, Integer> idToPosMap = new HashMap<>();
                    Log.v("metaTokensList size", String.valueOf(numOfTiles));
                    for (int i = 0; i < numOfTiles; ++i) {
                        String[] metaTokensRow = metaTokensList.get(i);
                        idToPosMap.put(Integer.parseInt(metaTokensRow[0]), Integer.parseInt(metaTokensRow[17]));
                        Log.v("idToPosMap : ", Integer.parseInt(metaTokensRow[0]) + ", " + idToPosMap.get(Integer.parseInt(metaTokensRow[0])));
                        tilesList.add(null);
                        galleryTilesList.add(null);
                    }
                    if (Integer.parseInt(metaTokensList.get(0)[13]) == 1) {//if the puzzle is shared from Android
                        isSharedFromAndroid = true;
                    } /*else {//We are not getting the currentPositions of all tiles when a puzzle is shared from IOS.
                        // We are just getting the positions for the tiles which are NOT there on canvas.
                        //We are getting the positions for tiles which are there on gallery.... as 0, 1, 2, 3, 4.... and so on.
                        //So we need to get the max value in received positions, and form rest of the positions randomly.
                        isSharedFromAndroid = false;
                        int position = 0;
                        for (int i = 0; i < numOfTiles; ++i) {
                            String[] metaTokensRow = metaTokensList.get(i);
                            if (position < Integer.parseInt(metaTokensRow[10])) {
                                position = Integer.parseInt(metaTokensRow[10]);
                            }
                        }
                        ArrayList<Integer> missedPositions = new ArrayList<>();
                        for (int i = position + 1; i < numOfTiles; ++i) {
                            Log.v("MP", ""+i);
                            missedPositions.add(i);
                        }
                        Collections.shuffle(missedPositions);
                        for (int i = 0, j = 0; i < numOfTiles; ++i) {
                            String[] metaTokensRow = metaTokensList.get(i);
                            if (Integer.parseInt(metaTokensRow[8]) == 0) {//if NOT isDroppedOnCanvas
                                idToPosMap.put(Integer.parseInt(metaTokensRow[0]), Integer.parseInt(metaTokensRow[10]));
                            } else {
                                idToPosMap.put(Integer.parseInt(metaTokensRow[0]), missedPositions.get(j));
                                ++j;
                            }
                            Log.v("idToPosMap : ", Integer.parseInt(metaTokensRow[0]) + ", " + idToPosMap.get(Integer.parseInt(metaTokensRow[0])));
                            tilesList.add(null);
                            galleryTilesList.add(null);
                        }
                    }*/

                    for (int id = 0; id < numOfTiles; ++id) {
                        Mask mask = masksList.get(id);
                        Bitmap maskBitmap = BitmapFactory.decodeResource(getResources(), mask.getResourceID(), options);
                        maskBitmap = BitmapHelper.getResizedBitmap(maskBitmap, (int) (maskBitmap.getWidth() * wScaleFactor), (int) (maskBitmap.getHeight() * hScaleFactor));
                        Bitmap pieceBitmap = Bitmap.createBitmap(photoBitmap, (int) mask.getX1(), (int) mask.getY1(), maskBitmap.getWidth(), maskBitmap.getHeight());
                        pieceBitmap.setHasAlpha(true);
                        Canvas cnvs = new Canvas(pieceBitmap);
                        Paint paint = new Paint();
                        paint.setColor(Color.BLACK);
                        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                        cnvs.drawBitmap(maskBitmap, 0, 0, paint);
                        maskBitmap.recycle();

                        short angle = (short) RANDOM.nextInt(4);

                        Bitmap rotatedBitmap = BitmapHelper.getRotatedBitmap(pieceBitmap, angle * 90);
                        ShatteredTile tile = new ShatteredTile(this, id, -1, -1, pieceBitmap, rotatedBitmap, angle, (int) mask.getX1(), (int) mask.getY1(), (int) mask.getX2(), (int) mask.getY2(), patternType, puzzleView);
                        //tile.setCenterPieceOfPuzzle(mask.getCentrePiece());
                        tile.setSharedFromAndroid(isSharedFromAndroid);
                        tile.setPatternType(patternType);
                        tile.setTileRecycled(false);
                        int currentPos = idToPosMap.get(id);
                        tile.setCurrentPosition(currentPos);
                        tilesList.set(currentPos, tile);
                        galleryTilesList.set(currentPos, tile);
                    }
                    for (int i = 0; i < numOfTiles; ++i) {
                        String[] metaTokensRow = metaTokensList.get(i);
                        ShatteredTile tile = getTileByID(Integer.parseInt(metaTokensRow[0]));
                        // We should not set x1, y1, x2, y2 because these values
                        // may not be same for different devices as each device
                        // has different width, height
                        // tile.setX1(Integer.parseInt(metaTokensRow[3]));
                        // tile.setY1(Integer.parseInt(metaTokensRow[4]));
                        // tile.setX2(Integer.parseInt(metaTokensRow[5]));
                        // tile.setY2(Integer.parseInt(metaTokensRow[6]));
                        tile.setOrientation((short) Integer.parseInt(metaTokensRow[7]));
                        tile.turnTile((short) 0, tile.getOrientation());
                        tile.setDroppedOnPuzzleView(Integer.parseInt(metaTokensRow[8]) == 1);
                        tile.setTileRecycled(Integer.parseInt(metaTokensRow[9]) == 1);
                        tile.setCenterPieceOfPuzzle(Integer.parseInt(metaTokensRow[11]) == 1);
                        tile.setPatternType(patternType);
                        tile.setAttachedWith(getArrayElementsAsStringFromIndex(metaTokensRow, 20));

                        if (tile.isSharedFromAndroid()) {
                            // set curX, curY to the tiles who are not recycled
                            if (!tile.isTileRecycled()) {
                                // curX, curY should be calculated according to new
                                // device screen width, height based on old photo
                                // (which was shared from another device) width,
                                // height
                                tile.setCurrentX((Integer.parseInt(metaTokensRow[1]) * photoBitmap.getWidth()) / Integer.parseInt(metaTokensRow[3]));
                                tile.setCurrentY((Integer.parseInt(metaTokensRow[2]) * photoBitmap.getHeight()) / Integer.parseInt(metaTokensRow[4]));
                            }
                        } else {
                            if (tile.isDroppedOnPuzzleView()) {
                                // curX, curY should be calculated according to new
                                // device screen width, height based on old photo (which was shared from another device) width, height

                                //curX, curY values are at different positions(i.e 18, 19) in case puzzle is shared from iOS.
                                tile.setCurrentX((Integer.parseInt(metaTokensRow[18]) * photoBitmap.getWidth()) / Integer.parseInt(metaTokensRow[15]));
                                tile.setCurrentY((Integer.parseInt(metaTokensRow[19]) * photoBitmap.getHeight()) / Integer.parseInt(metaTokensRow[16]));
                            }
                        }
                    }

                    for (ShatteredTile tile : tilesList) {
                        ArrayList<Integer> attachedWith = tile.getAttachedWith();
                        if (tile.isTileRecycled()) {
                            System.err.println("Tile #" + tile.getTileId() + " isRecycled? " + tile.isTileRecycled());
                        }
                        if (!tile.isTileRecycled() && attachedWith.size() > 1) {
                            for (int j = 0; j < attachedWith.size(); ++j) {
                                ShatteredTile otherTile = getTileByID(attachedWith.get(j));
                                if (tile.getTileId() != otherTile.getTileId()) {
                                    System.err.println("Tile #" + tile.getTileId() + " is attaching with " + otherTile.getTileId());
                                    Bitmap comboBitmap = Utils.getComboBitmapOfTwoTiles(this, tile, otherTile, photoBitmap.getWidth(), photoBitmap.getHeight(), true);
                                    int newWidth = comboBitmap.getWidth();
                                    int newHeight = comboBitmap.getHeight();
                                    tile.setDisplayBitmap(comboBitmap);
                                    tile.setBitmapWidth(newWidth);
                                    tile.setBitmapHeight(newHeight);
                                    tile.getSurroundingTiles().addAll(otherTile.getSurroundingTiles());
                                    otherTile.getSurroundingTiles().addAll(tile.getSurroundingTiles());
                                    tile.setSurroundingTiles(Utils.removeDuplicatesFromList(tile.getSurroundingTiles()));
                                    otherTile.setSurroundingTiles(Utils.removeDuplicatesFromList(otherTile.getSurroundingTiles()));
                                    otherTile.setTileRecycled(true);

                                    tile.setX1(Math.min(tile.getX1(), otherTile.getX1()));
                                    tile.setY1(Math.min(tile.getY1(), otherTile.getY1()));
                                    tile.setX2(Math.max(tile.getX2(), otherTile.getX2()));
                                    tile.setY2(Math.max(tile.getY2(), otherTile.getY2()));

                                    if (tile.getCurrentX() == -1 && tile.getCurrentY() == -1) {
                                        tile.setCurrentX(Math.min(tile.getCurrentX(), otherTile.getCurrentX()));
                                        tile.setCurrentY(Math.min(tile.getCurrentY(), otherTile.getCurrentY()));
                                    } else {
                                        tile.setCurrentX(Math.max(tile.getCurrentX(), otherTile.getCurrentX()));
                                        tile.setCurrentY(Math.max(tile.getCurrentY(), otherTile.getCurrentY()));
                                    }

                                    tile.getAttachedWith().addAll(otherTile.getAttachedWith());
                                    otherTile.getAttachedWith().addAll(tile.getAttachedWith());
                                    tile.setAttachedWith(Utils.removeDuplicatesFromList(tile.getAttachedWith()));
                                    otherTile.setAttachedWith(Utils.removeDuplicatesFromList(otherTile.getAttachedWith()));
                                }
                            }
                        }
                    }

                    ArrayList<ShatteredTile> droppedTilesList = new ArrayList<ShatteredTile>();
                    for (ShatteredTile tile : tilesList) {
                        if (tile.isDroppedOnPuzzleView() || tile.isCenterPieceOfPuzzle()) {
                            droppedTilesList.add(tile);
                            galleryTilesList.remove(tile);
                        }
                    }

                    for (ShatteredTile tile : tilesList) {
                        if (tile.isTileRecycled() && droppedTilesList.contains(tile)) {
                            droppedTilesList.remove(tile);
                            galleryTilesList.remove(tile);

                            tile.getOriginalBitmap().recycle();
                            tile.getDisplayBitmap().recycle();
                        }
                    }

                    if (savedInstanceState != null && FracturePhotoApplication.getOriginalTilesList() != null) {
                        tilesList = FracturePhotoApplication.getOriginalTilesList();
                    }
                    if (savedInstanceState != null && FracturePhotoApplication.getGalleryTilesList() != null) {
                        galleryTilesList = FracturePhotoApplication.getGalleryTilesList();
                    }
                    System.out.println("droppedTilesList size shared" + droppedTilesList.size());
                    puzzleView.doInitialSetupWithDroppedTiles(this, tilesList, droppedTilesList);

                    for (ShatteredTile tile : galleryTilesList) {
                        Log.v("Tile id ", tile.getTileId() + ", " + tile.isTileRecycled());
                    }
                    galleryAdapter = new ShatteredPuzzleGalleryAdapter(PlayShatteredPuzzleActivity.this, galleryTilesList);
                    System.out.println("galleryTilesList size shared" + galleryTilesList.size());
                    hrzntlListView.setAdapter(galleryAdapter);
                    saveSharedPath(getIntent().getStringExtra(Extras_Keys.PHOTO_PATH));
                    saveSharedPuzzle();
                    try {
                        Thread.sleep(2000);
                        if (rootPath != null && !rootPath.isEmpty()) {
                            File f = new File(rootPath);
                            DeleteRecursive(f);
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
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
        Log.v("sharedDownloadPhotoPath", sharedDownloadPhotoPath);
        Bitmap sharedBitmap = BitmapFactory.decodeFile(sharedDownloadPhotoPath);
        File sharedFile = null;
        String root = SDCARD_PATH + File.separator
                + getString(string.app_name) + File.separator;
        myDir = new File(root + "/SharedPhotos");
        myDir.mkdirs();

        String fname = new SimpleDateFormat("yyyyMMddhhmmss'_SharedShatteredPhto.jpg'").format(new Date());
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                FracturePhotoDB db = new FracturePhotoDB(getApplicationContext());
                db.openDB();
                if (isSharedPuzzle) {
                    savedSharedPuzzleRowId = db.savePuzzleHistory(puzzleName,
                            pathOfDuplicateSharedPhoto, tilesList.size(),
                            PuzzleType.SHATTERED_PUZZLE.getPuzzleType());
                    if (savedSharedPuzzleRowId != 0) {
                        for (ShatteredTile tile : tilesList) {
                            if (!tile.isTileRecycled()) {
                                if (db.saveShatteredTilesDetails(savedSharedPuzzleRowId, tile) == -1) {
                                    db.deletePuzzleHistoryWithId(String.valueOf(savedSharedPuzzleRowId));
                                    showToast("Something went wrong\nPlease try again later");
                                    db.closeDB();
                                    return;
                                }
                            }
                        }
                        db.closeDB();
                    }
                } else {
                    db.closeDB();
                }
            }
        }).start();
    }

    private ArrayList<Integer> getArrayElementsAsStringFromIndex(
            String[] tokensRow, int fromIndex) {
        ArrayList<Integer> associationsList = new ArrayList<Integer>();
        for (int i = fromIndex; i < tokensRow.length; ++i) {
            associationsList.add(Integer.parseInt(tokensRow[i]));
        }
        return associationsList;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sharePuzzleImage:
            /*
             * if (tilesList != null && tilesList.size() >= 1 &&
			 * !puzzleView.isPuzzleSolved()) { final ProgressDialog progressBar
			 * = ProgressDialog.show(PlayShatteredPuzzleActivity.this,
			 * "Preparing", "Please wait..."); progressBar.setCancelable(false);
			 * progressBar.setCanceledOnTouchOutside(false);
			 * progressBar.setIndeterminate(true); new Thread(new Runnable(){
			 *
			 * @Override public void run(){ if
			 * (makeDuplicateOfPuzzlePhoto(puzzleName, photoPath,
			 * PuzzleType.SHATTERED_PUZZLE.getPuzzleType())) {
			 * doPrepareShatteredTileMetaInfo(); doPrepareUploadFileInfo();
			 * uploadFile(); //doSendEmail(); //showSharePopup(); }
			 * if(progressBar != null && progressBar.isShowing()){
			 * progressBar.dismiss(); } } }).start(); }else
			 * if(puzzleView.isPuzzleSolved()){ showToast(
			 * "You are not allowed to share this puzzle as it has been solved"
			 * ); }
			 */

                if (FracturePhotoApplication.getInstalledAppsList() == null) {
                    final ProgressDialog progressBar = ProgressDialog.show(PlayShatteredPuzzleActivity.this, "Loading",
                            "Please wait...");
                    progressBar.setCancelable(false);
                    progressBar.setCanceledOnTouchOutside(false);
                    progressBar.setIndeterminate(true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            FracturePhotoApplication.setInstalledAppsList(AppsInfo.getInstalledApps(PlayShatteredPuzzleActivity.this, true));
                            if (progressBar != null && progressBar.isShowing()) {
                                progressBar.dismiss();
                            }
                            if (!isDuplicateShareClick) {
                                isDuplicateShareClick = true;
                                PlayShatteredPuzzleActivity.this
                                        .runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (Utils.isConnectionAvailable(PlayShatteredPuzzleActivity.this))
                                                    showSharePopup();
                                                else {
                                                    String message = "Your Device is not connected to Internet. Please connect to working Internet.";
                                                    Utils.NetworkMessage(
                                                            PlayShatteredPuzzleActivity.this,
                                                            message);
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
                        if (Utils.isConnectionAvailable(PlayShatteredPuzzleActivity.this))
                            showSharePopup();
                        else {
                            String message = "Your Device is not connected to Intetnet. Please connect to working Internet.";
                            Utils.NetworkMessage(PlayShatteredPuzzleActivity.this,
                                    message);
                            isDuplicateShareClick = false;
                        }
                    }
                }
                break;

            case R.id.turnLeft:
                if (puzzleView.tappedTile != null
                        && puzzleView.tappedTile.isTapped()/*
             * && !puzzleView.
			 * isPuzzleSolved()
			 */) {
                    puzzleView.tappedTile
                            .turnTile(puzzleView.tappedTile.getOrientation(),
                                    (short) ((puzzleView.tappedTile
                                            .getOrientation() - 1) % 4));
                    puzzleView.invalidate();
                    if (puzzleView.isPuzzleSolved()) {
                        showSuccessDialogBox();
                        Utils.doPlaySound(this, R.raw.congratulations_audio);
                    }
                }
                break;

            case R.id.turnRight:
                if (puzzleView.tappedTile != null
                        && puzzleView.tappedTile.isTapped()/*
             * && !puzzleView.
			 * isPuzzleSolved()
			 */) {
                    puzzleView.tappedTile
                            .turnTile(puzzleView.tappedTile.getOrientation(),
                                    (short) ((puzzleView.tappedTile
                                            .getOrientation() + 1) % 4));
                    puzzleView.invalidate();
                    System.err.println("isTileWithinPuzzleViewBoundaries : "
                            + puzzleView.tappedTile
                            .isTileWithinPuzzleViewBoundaries());
                    if (puzzleView.isPuzzleSolved()) {
                        showSuccessDialogBox();
                        Utils.doPlaySound(this, R.raw.congratulations_audio);
                    }
                }
                break;

            case R.id.savePuzzleImage:
                if (tilesList != null && tilesList.size() >= 1 && !puzzleView.isPuzzleSolved()) {
                    doSavePuzzle();
                } else if (puzzleView.isPuzzleSolved()) {
                    doSavePuzzle();
                }
                break;

            case R.id.preview:
                final Dialog d = new Dialog(PlayShatteredPuzzleActivity.this,
                        R.style.preview_dialog);
                d.setContentView(R.layout.preview_dialog);
                ImageView img = (ImageView) d.findViewById(R.id.previewImage);
                ImageView closebtn = (ImageView) d.findViewById(R.id.Closebtn);
                closebtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        d.dismiss();
                    }
                });
                if (photoBitmap != null && !photoBitmap.isRecycled()) {
                    img.setImageBitmap(photoBitmap);
                    // d.setTitle("Preview");
                } else {
                    d.setTitle("Preview Not Available");
                }
                d.show();
            /*
             * ArrayList<ShatteredTile> droppedTilesList = new
			 * ArrayList<ShatteredTile>(); for (ShatteredTile tile : tilesList)
			 * { tile.setCurrentX(tile.getX1()); tile.setCurrentY(tile.getY1());
			 * tile.turnTile((short)0, (short)0); tile.setOrientation((short)0);
			 * droppedTilesList.add(tile); }
			 * puzzleView.doInitialSetupWithDroppedTiles(this, tilesList,
			 * droppedTilesList);
			 */
                break;
            case R.id.goLeft:
                scrollGalleryToPrevious(v);
                break;
            case R.id.goRight:
                scrollGalleryToNext(v);
                break;
            default:
                break;
        }
    }

    protected void doPrepareUploadFileInfo() {

        String zipName = cacheFolder + File.separator + puzzleName + ZIP_EXTENSION;
        File zipfile = new File(zipName);
        Log.v("imagePathToShare", imagePathToShare);
        Log.v("metaDataFilePath", metaDataFilePath);

        String[] files = new String[]{imagePathToShare, metaDataFilePath};

        String newZipStrName = cacheFolder + File.separator + deviceid + ZIP_EXTENSION;
        final File newZipFileName = new File(newZipStrName);
        if (zipfile.exists()) {
            zipfile.renameTo(newZipFileName);
        }
        ZipHelper cmprs_new = new ZipHelper(files, newZipFileName.getAbsolutePath());
        cmprs_new.zip();
        uploadFPFileName = newZipFileName.getAbsolutePath();
    }

    private void showSharePopup() {
        final Dialog dialog = new Dialog(PlayShatteredPuzzleActivity.this);
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
            FracturePhotoApplication.setInstalledAppsList(AppsInfo.getInstalledApps(PlayShatteredPuzzleActivity.this, true));
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
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                String selectedClient = adapterView.getItemAtPosition(position)
                        .toString();
                if (selectedClient.equals("Gmail")) {
                    if (tilesList != null && tilesList.size() >= 1
                            && !puzzleView.isPuzzleSolved()) {
                        final ProgressDialog progressBar = ProgressDialog.show(
                                PlayShatteredPuzzleActivity.this, "Preparing",
                                "Please wait...");
                        progressBar.setCancelable(false);
                        progressBar.setCanceledOnTouchOutside(false);
                        progressBar.setIndeterminate(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (makeDuplicateOfPuzzlePhoto(puzzleName,
                                        photoPath, PuzzleType.SHATTERED_PUZZLE
                                                .getPuzzleType())) {
                                    doPrepareShatteredTileMetaInfo();
                                    doPrepareUploadFileInfo();
                                    doPrepareCaptureImage();
                                    uploadFile("Gmail", false);
                                    try {
                                        if (progressBar != null
                                                && progressBar.isShowing()) {
                                            progressBar.dismiss();
                                        }
                                        Thread.sleep(sleepTime);
                                        if (rootPath != null
                                                && !rootPath.isEmpty()) {
                                            File f = new File(rootPath);
                                            DeleteRecursive(f);
                                        }
                                    } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                if (progressBar != null
                                        && progressBar.isShowing()) {
                                    progressBar.dismiss();
                                }
                            }
                        }).start();
                    } else if (puzzleView.isPuzzleSolved()) {
                        showToast("You are not allowed to share this puzzle as it has been solved");
                    }
                } else if (selectedClient.equals("Facebook")) {
                    if (tilesList != null && tilesList.size() >= 1 && !puzzleView.isPuzzleSolved()) {
                        final ProgressDialog progressBar = ProgressDialog.show(PlayShatteredPuzzleActivity.this, "Preparing", "Please wait...");
                        progressBar.setCancelable(false);
                        progressBar.setCanceledOnTouchOutside(false);
                        progressBar.setIndeterminate(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (makeDuplicateOfPuzzlePhoto(puzzleName,
                                        photoPath, PuzzleType.SHATTERED_PUZZLE
                                                .getPuzzleType())) {
                                    doPrepareShatteredTileMetaInfo();
                                    doPrepareUploadFileInfo();
                                    doPrepareCaptureImage();
                                    uploadFile("Facebook", false);
                                    try {
                                        if (progressBar != null
                                                && progressBar.isShowing()) {
                                            progressBar.dismiss();
                                        }
                                        Thread.sleep(sleepTime);
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
                    } else if (puzzleView.isPuzzleSolved()) {
                        showToast("You are not allowed to share this puzzle as it has been solved");
                    }
                } else if (selectedClient.equals(GALLERY_PAGE_NAME)) {
                    if (tilesList != null && tilesList.size() >= 1 && !puzzleView.isPuzzleSolved()) {
                        final ProgressDialog progressBar = ProgressDialog.show(
                                PlayShatteredPuzzleActivity.this, "Preparing",
                                "Please wait...");
                        progressBar.setCancelable(false);
                        progressBar.setCanceledOnTouchOutside(false);
                        progressBar.setIndeterminate(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (makeDuplicateOfPuzzlePhoto(puzzleName,
                                        photoPath, PuzzleType.SHATTERED_PUZZLE.getPuzzleType())) {
                                    doPrepareShatteredTileMetaInfo();
                                    doPrepareUploadFileInfo();
                                    doPrepareCaptureImage();
                                    uploadFile(GALLERY_PAGE_NAME, true);
                                    try {
                                        if (progressBar != null
                                                && progressBar.isShowing()) {
                                            progressBar.dismiss();
                                        }
                                        Thread.sleep(sleepTime);
                                        if (rootPath != null
                                                && !rootPath.isEmpty()) {
                                            File f = new File(rootPath);
                                            DeleteRecursive(f);
                                        }
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                if (progressBar != null
                                        && progressBar.isShowing()) {
                                    progressBar.dismiss();
                                }
                            }
                        }).start();
                    } else if (puzzleView.isPuzzleSolved()) {
                        showToast("You are not allowed to share this puzzle as it has been solved");
                    }
                } else if (selectedClient.equals("Twitter")) {
                    if (tilesList != null && tilesList.size() >= 1
                            && !puzzleView.isPuzzleSolved()) {
                        final ProgressDialog progressBar = ProgressDialog.show(
                                PlayShatteredPuzzleActivity.this, "Preparing",
                                "Please wait...");
                        progressBar.setCancelable(false);
                        progressBar.setCanceledOnTouchOutside(false);
                        progressBar.setIndeterminate(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (makeDuplicateOfPuzzlePhoto(puzzleName,
                                        photoPath, PuzzleType.SHATTERED_PUZZLE
                                                .getPuzzleType())) {
                                    doPrepareShatteredTileMetaInfo();
                                    doPrepareUploadFileInfo();
                                    doPrepareCaptureImage();
                                    uploadFile("Twitter", false);
                                    try {
                                        if (progressBar != null
                                                && progressBar.isShowing()) {
                                            progressBar.dismiss();
                                        }
                                        Thread.sleep(sleepTime);
                                        if (rootPath != null
                                                && !rootPath.isEmpty()) {
                                            File f = new File(rootPath);
                                            DeleteRecursive(f);
                                        }
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                if (progressBar != null
                                        && progressBar.isShowing()) {
                                    progressBar.dismiss();
                                }
                            }
                        }).start();
                    } else if (puzzleView.isPuzzleSolved()) {
                        showToast("You are not allowed to share this puzzle as it has been solved");
                    }
                }  else if (selectedClient.equals("Messages")) {
                    if (tilesList != null && tilesList.size() >= 1
                            && !puzzleView.isPuzzleSolved()) {
                        final ProgressDialog progressBar = ProgressDialog.show(
                                PlayShatteredPuzzleActivity.this, "Preparing",
                                "Please wait...");
                        progressBar.setCancelable(false);
                        progressBar.setCanceledOnTouchOutside(false);
                        progressBar.setIndeterminate(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (makeDuplicateOfPuzzlePhoto(puzzleName,
                                        photoPath, PuzzleType.SHATTERED_PUZZLE
                                                .getPuzzleType())) {
                                    doPrepareShatteredTileMetaInfo();
                                    doPrepareUploadFileInfo();
                                    doPrepareCaptureImage();
                                    uploadFile("Messages", false);
                                    try {
                                        if (progressBar != null
                                                && progressBar.isShowing()) {
                                            progressBar.dismiss();
                                        }
                                        Thread.sleep(sleepTime);
                                        if (rootPath != null
                                                && !rootPath.isEmpty()) {
                                            File f = new File(rootPath);
                                            DeleteRecursive(f);
                                        }
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                if (progressBar != null
                                        && progressBar.isShowing()) {
                                    progressBar.dismiss();
                                }
                            }
                        }).start();
                    } else if (puzzleView.isPuzzleSolved()) {
                        showToast("You are not allowed to share this puzzle as it has been solved");
                    }
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void doSavePuzzle() {
        savingProgressWheel = ProgressDialog.show(PlayShatteredPuzzleActivity.this, "Saving Puzzle", "Please wait...");
        savingProgressWheel.setCancelable(false);
        savingProgressWheel.setCanceledOnTouchOutside(false);
        savingProgressWheel.setIndeterminate(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                FracturePhotoDB db = new FracturePhotoDB(getApplicationContext());
                db.openDB();
                if (isCreatePuzzleMode) {
                    System.out.print("doSavePuzzle ----- isCreatePuzzleMode");
                    long newRowId = db.savePuzzleHistory(puzzleName,
                            getIntent().getStringExtra(Extras_Keys.PHOTO_PATH),
                            tilesList.size(),
                            PuzzleType.SHATTERED_PUZZLE.getPuzzleType());
                    if (newRowId != 0) {
                        for (ShatteredTile tile : tilesList) {
                            if (!tile.isTileRecycled()) {
                                System.out.print("doSavePuzzle ----- !tile.isTileRecycled()");
                                if (db.saveShatteredTilesDetails(newRowId, tile) == -1) {
                                    db.deletePuzzleHistoryWithId(String.valueOf(newRowId));
                                    showToast("Something went wrong\nPlease try again later");
                                    db.closeDB();
                                    if (savingProgressWheel != null
                                            && savingProgressWheel.isShowing()) {
                                        savingProgressWheel.dismiss();
                                    }
                                    PlayShatteredPuzzleActivity.this.finish();
                                    return;
                                }
                            }
                        }
                        showToast("Puzzle has been saved succesfully");
                        db.closeDB();
                        if (savingProgressWheel != null
                                && savingProgressWheel.isShowing()) {
                            savingProgressWheel.dismiss();
                        }
                        Intent i = new Intent(PlayShatteredPuzzleActivity.this, DashboardActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        if (!isActivityStopped()) {
                            startActivity(i);
                        }
                        PlayShatteredPuzzleActivity.this.finish();
                    }
                } else {// THIS IS SAVE PUZZLE MODE or Shared puzzle
                    String oldRowId;
                    if (isSharedPuzzle) {
                        oldRowId = String.valueOf(savedSharedPuzzleRowId);
                    } else {
                        oldRowId = getIntent().getStringExtra(
                                Extras_Keys.PUZZLE_HISTORY_ROW_ID_IN_DB);
                    }
                    long newRowId = db.updatePuzzle(oldRowId, puzzleName,
                            photoPath, -1);
                    if (newRowId != -1) {
                        int numOfRowsDeleted = db
                                .deleteShatteredTilesAssociatedWithBaseRowId(oldRowId);
                        System.err.println("Num Of Rows Deleted == "
                                + numOfRowsDeleted);
                        for (int i = 0; i < tilesList.size(); ++i) {
							/*
							 * ShatteredTile tile = tilesList.get(i); if
							 * (db.updateShatteredTilesDetails(oldRowId,
							 * newRowId, numOfPieces, tile,
							 * gridView.getNumColumns() + "", "" +
							 * gridView.getColumnWidth()) == 0) {
							 * Toast.makeText(getApplicationContext(),
							 * "Something went wrong",
							 * Toast.LENGTH_LONG).show(); break; }
							 */
                            ShatteredTile tile = tilesList.get(i);
                            if (!tile.isTileRecycled()) {
                                if (db.saveShatteredTilesDetails(newRowId, tile) == -1) {
                                    showToast("Something went wrong\nPlease try again later");
                                    db.closeDB();
                                    if (savingProgressWheel != null
                                            && savingProgressWheel.isShowing()) {
                                        savingProgressWheel.dismiss();
                                    }
                                    PlayShatteredPuzzleActivity.this.finish();
                                    return;
                                }
                            }
                        }
                        showToast("Puzzle has been updated succesfully");
                        db.closeDB();
                        if (savingProgressWheel != null
                                && savingProgressWheel.isShowing()) {
                            savingProgressWheel.dismiss();
                        }
                        Intent i = new Intent(PlayShatteredPuzzleActivity.this,
                                DashboardActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        if (!isActivityStopped()) {
                            startActivity(i);
                        }
                        PlayShatteredPuzzleActivity.this.finish();
                    }
					/*
					 * }else if(puzzleView.isPuzzleSolved()){ db.closeDB();
					 * if(savingProgressWheel != null &&
					 * savingProgressWheel.isShowing()){
					 * savingProgressWheel.dismiss(); }
					 * PlayShatteredPuzzleActivity.this.finish(); }
					 */
                }
            }
        }).start();
    }

    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast t = Toast.makeText(PlayShatteredPuzzleActivity.this, msg,
                        Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            }
        });
    }

    public void showSuccessDialogBox() {
       /*alertDialogBox = new AlertDialog.Builder(PlayShatteredPuzzleActivity.this);
        alertDialogBox.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!isCreatePuzzleMode && !isSharedPuzzle && pzlRowId != null) {
                            FracturePhotoDB db = new FracturePhotoDB(getApplicationContext());
                            db.openDB();
                            db.closeDB();
                        }
                    }
                });
        alertDialogBox.setTitle("Congratulations!");
        alertDialogBox.setMessage("You Solved It");
        alertDialogBox.setCancelable(false);
        alertDialogBox.show();*/
    
        congratsDialog = new Dialog(this);
        congratsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        congratsDialog.setCancelable(false);
        congratsDialog.setCanceledOnTouchOutside(false);
        congratsDialog.setContentView(R.layout.congratulations_alert);
        congratsParentLayout = (RotateLayout) congratsDialog.findViewById(R.id.congratsParentLayout);
        congratsDialog.show();
        congratsParentLayout.setAngle(initialRotation);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (congratsDialog != null && congratsDialog.isShowing()) {
                    congratsDialog.dismiss();
                }
            }
        }, 9000);
        startCongratsTimer();
    }
    
    Timer congratsTimer;
    TimerTask congratsTimerTask;
    final Handler congratsHandler = new Handler();
    
    public void startCongratsTimer() {
        //set a new Timer
        congratsTimer = new Timer();
        congratsBlinkingCount = 0;
        initializeCongratsTimerTask();
        
        //schedule the timer, after the first 100ms the TimerTask will run every 10000ms
        congratsTimer.schedule(congratsTimerTask, 100, 500); //
    }
    
    public void stopCongratsTimerTask() {
        if (congratsTimer != null) {
            congratsTimer.cancel();
            congratsTimer = null;
            congratsBlinkingCount = 0;
        }
        if (congratsDialog != null && congratsDialog.isShowing()) {
            congratsDialog.setCancelable(true);
            congratsDialog.setCanceledOnTouchOutside(true);
        }
    }
    
    int congratsBlinkingCount = 0;
    
    public void initializeCongratsTimerTask() {
        
        congratsTimerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                congratsHandler.post(new Runnable() {
                    public void run() {
                        if (congratsBlinkingCount > 7) {
                            stopCongratsTimerTask();
                            return;
                        }
                        if (congratsDialog != null && congratsDialog.isShowing()) {
                            RotateLayout parent = (RotateLayout) congratsDialog.findViewById(R.id.congratsParentLayout);
                            TextView label = (TextView) congratsDialog.findViewById(R.id.congratsLable);
                            
                            if (congratsBlinkingCount % 2 == 1) {
                                label.setTextColor(Color.BLACK);
                                parent.setBackgroundResource(R.drawable.bg_black_border_of_congrats);
                            } else {
                                label.setTextColor(Color.RED);
                                parent.setBackgroundResource(R.drawable.bg_red_border_of_congrats);
                            }
                            ++congratsBlinkingCount;
                        } else {
                            stopCongratsTimerTask();
                        }
                    }
                });
            }
        };
    }

    public void onTileDropFromPuzzleView(ShatteredTile tile) {
        if (galleryAdapter != null && tile != null && puzzleView != null) {
            tile.setDroppedOnPuzzleView(false);
            tile.setCenterPieceOfPuzzle(false);
            galleryAdapter.addTileToGallery(tile, hrzntlListView);
            puzzleView.removeTileFromPuzzleView(tile);
        }
    }

    public boolean isTileAddedToGallery(ShatteredTile droppingTile) {
        for (ShatteredTile galleryTile : galleryTilesList) {
            if (galleryTile.getTileId() == droppingTile.getTileId()) {
                return true;
            }
        }
        return false;
    }

    public void scrollGalleryToPrevious(View v) {
        int curPosition = hrzntlListView.getFirstVisiblePosition();
        if (galleryAdapter != null && curPosition != 0 && galleryAdapter.getCount() >= 1) {
            hrzntlListView.setSelection(curPosition - 1);
        }
    }

    public void scrollGalleryToNext(View v) {
        int curPosition = hrzntlListView.getFirstVisiblePosition();
        if (galleryAdapter != null && curPosition != galleryAdapter.getCount() - 1 && galleryAdapter.getCount() >= 1) {
            hrzntlListView.setSelection(curPosition + 1);
        }
    }

    private ShatteredTile getTileByID(final int tileID) {
        for (ShatteredTile tile : tilesList) {
            if (tile != null) {
                if (tile.getTileId() == tileID) {
                    return tile;
                }
            }
        }
        return null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isSharedPuzzle", isSharedPuzzle);
        outState.putBoolean("isDuplicateShareClick", isDuplicateShareClick);
        outState.putBoolean("isSolved", puzzleView.isPuzzleSolved());
        outState.putBoolean("isCreatePuzzleMode", isCreatePuzzleMode);
        outState.putBoolean("isFirstTileDropped", isFirstTileDropped);
        outState.putString("metaDataFilePath", metaDataFilePath);
        outState.putString("imagePathToShare", imagePathToShare);
        outState.putString("imageUriString", imageUriString);
        outState.putString("filePath", photoPath);
        outState.putString("puzzleName", puzzleName);
        outState.putString("pzlRowId", pzlRowId);
        outState.putInt("orientation", orientation);
        outState.putInt("availableWidth", availableWidth);
        outState.putInt("patternType", patternType);
        outState.putInt("availableHeight", availableHeight);
        outState.putLong("savedSharedPuzzleRowId", savedSharedPuzzleRowId);
        outState.putString("pathOfDuplicateSharedPhoto", pathOfDuplicateSharedPhoto);

        if (hrzntlListView != null) {
            outState.putInt("galleryCurrentPosition", hrzntlListView.getSelectedItemPosition());
        }
        if (puzzleView.tappedTile != null) {
            outState.putInt("lastTappedTileId", puzzleView.tappedTile.getTileId());
        }

        FracturePhotoApplication.setOriginalTilesList(tilesList);
        FracturePhotoApplication.setGalleryTilesList(galleryTilesList);
        puzzleView.onSaveInstanceState_(outState);

        super.onSaveInstanceState(outState);
    }

    // THIS IS NOT WORKING... LAUNCHING GMAIL APP AND COMING BACK WILL NOT
    // TRIGGER THIS METHOD
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

    private boolean isActivityStopped() {
        return isActivityStopped;
    }

    private void setActivityStopped(boolean isStopped) {
        this.isActivityStopped = isStopped;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        isSharedPuzzle = savedInstanceState.getBoolean("isSharedPuzzle");
        isDuplicateShareClick = savedInstanceState.getBoolean("isDuplicateShareClick");
        isCreatePuzzleMode = savedInstanceState.getBoolean("isCreatePuzzleMode");
        // /setFirstTileDropped(savedInstanceState.getBoolean("isFirstTileDropped"));
        metaDataFilePath = savedInstanceState.getString("metaDataFilePath");
        imagePathToShare = savedInstanceState.getString("imagePathToShare");
        imageUriString = savedInstanceState.getString("imageUriString");
        photoPath = savedInstanceState.getString("filePath");
        puzzleName = savedInstanceState.getString("puzzleName");
        pzlRowId = savedInstanceState.getString("pzlRowId");
        orientation = savedInstanceState.getInt("orientation");
        availableWidth = savedInstanceState.getInt("availableWidth");
        patternType = savedInstanceState.getInt("patternType");
        availableHeight = savedInstanceState.getInt("availableHeight");
        savedSharedPuzzleRowId = savedInstanceState.getLong("savedSharedPuzzleRowId");
        puzzleView.restoreLastTappedTile(savedInstanceState.getInt("lastTappedTileId"));
        pathOfDuplicateSharedPhoto = savedInstanceState.getString("pathOfDuplicateSharedPhoto");

        tilesList = FracturePhotoApplication.getOriginalTilesList();
        galleryTilesList = FracturePhotoApplication.getGalleryTilesList();
        hrzntlListView.setSelection(savedInstanceState.getInt("galleryCurrentPosition"));
        puzzleView.onRestoreInstanceState_(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindIAPService();
        setActivityStopped(false);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        orientationManager = new OrientationManager(this, SensorManager.SENSOR_DELAY_NORMAL, this, is10InchTablet());
        orientationManager.enable();
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
        if (savingProgressWheel != null && savingProgressWheel.isShowing()) {
            savingProgressWheel.dismiss();
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

    private boolean makeDuplicateOfPuzzlePhoto(String puzzleName, String filePath, String
            typeOfPuzzle) {

        String extDir;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            extDir = getExternalCacheDir().getAbsolutePath() + File.separator + getString(string.app_name);
        } else {
            extDir = getCacheDir().getAbsolutePath() + File.separator + getString(string.app_name);
        }
        cacheFolder = new File(extDir);
        Log.v(TAG, "File Path:" + cacheFolder.getPath());
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
        String puzzlePhotoFileName = puzzleName + SEPARATOR_SYMBOL + typeOfPuzzle + SEPARATOR_SYMBOL + tilesList.size()
                + SEPARATOR_SYMBOL + patternType + SEPARATOR_SYMBOL
                + "PuzzlePhoto" + Utils.getCurrentTime() + IMAGE_FILE_EXTENSION;
        File photoFile = new File(cachedPicturesFolder.getAbsolutePath(), puzzlePhotoFileName);
        imagePathToShare = photoFile.getAbsolutePath();
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

    private void doPrepareShatteredTileMetaInfo() {
        ArrayList<String> detailsList = new ArrayList<String>();
        File file = new File(cachedPicturesFolder.getAbsolutePath(), puzzleName + META_FILE_EXTENSION);
        if (file.exists()) {
            file.delete();
        }

        File newFile = new File(cachedPicturesFolder.getAbsolutePath(), puzzleName + META_FILE_EXTENSION);
        metaDataFilePath = newFile.getAbsolutePath();
        Log.v("metaDataFilePath", metaDataFilePath);

        // IMPORTANT NOTE : Adding of new entry OR removing of existing entry OR
        // OR Changing the order of entries NEEDS TO BE DONE CAREFULLY.
        // WE NEED TO EXTRACT IN THE SAME ORDER WHILE WE ARE SOLVING THE SHARED
        // PUZZLE.
        for (ShatteredTile tile : tilesList) {
            // IMPORTANT NOTE : Adding of new entry OR removing of existing
            // entry OR
            // OR Changing the order of entries NEEDS TO BE DONE CAREFULLY.
            // WE NEED TO EXTRACT IN THE SAME ORDER WHILE WE ARE SOLVING THE
            // SHARED PUZZLE.
            if (!tile.isTileRecycled()) {
                detailsList.add(
                                /*0*/  tile.getTileId()
                        + SEPARATOR_SYMBOL
                                /*1*/ + tile.getCurrentX()
                        + SEPARATOR_SYMBOL
                                /*2*/ + tile.getCurrentY()
                        + SEPARATOR_SYMBOL
                               /*3*/ + photoBitmap.getWidth()
                        + SEPARATOR_SYMBOL
                               /*4*/ + photoBitmap.getHeight()
                        + SEPARATOR_SYMBOL
                               /*5*/ + tile.getX2()
                        + SEPARATOR_SYMBOL
                                /*6*/ + tile.getY2()
                        + SEPARATOR_SYMBOL
                                /*7*/ + tile.getOrientation()
                        + SEPARATOR_SYMBOL
                                /*8*/ + (tile.isDroppedOnPuzzleView() ? 1 : 0)
                        + SEPARATOR_SYMBOL
                                /*9*/ + (tile.isTileRecycled() ? 1 : 0)
                        + SEPARATOR_SYMBOL
                                /*10*/ + tile.getCurrentPosition()
                        + SEPARATOR_SYMBOL
                                /*11*/ + (tile.isCenterPieceOfPuzzle() ? 1 : 0)
                        + SEPARATOR_SYMBOL
                                /*12*/ + tile.getPatternType()
                        + SEPARATOR_SYMBOL
                                /*13*/ + 1
                        + SEPARATOR_SYMBOL
                                /*14*/ + (tile.isDroppedOnPuzzleView() ? 0 : galleryAdapter.getTilePosition(tile))
                        + SEPARATOR_SYMBOL
                                /*15*/ + ((int) puzzleView.getX() + tile.getCurrentX())
                        + SEPARATOR_SYMBOL
                                /*16*/ + ((int) puzzleView.getY() + tile.getCurrentY())
                        + SEPARATOR_SYMBOL
                                /*17*/ + tile.getCurrentPosition()
                        + SEPARATOR_SYMBOL
                                /*18*/ + tile.getCurrentX()
                        + SEPARATOR_SYMBOL
                                /*19*/ + tile.getCurrentY()
                        + SEPARATOR_SYMBOL
                                /*20*/ + Utils.getArrayListItemsAsStringInSingleLine(tile.getAttachedWith()));
            }
            Log.v("datalist tile id", detailsList.size() + "center peice " + (tile.isCenterPieceOfPuzzle()) + ", tile id " + tile.getTileId());
        }
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(newFile, true));
            for (String line : detailsList) {
                if (line.endsWith(SEPARATOR_SYMBOL)) {
                    line = line.substring(0, line.length() - 1);
                }
                System.out.println("line = " + line);
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean uploadFile(final String selectedShareClient, boolean isAdminPuzzle) {
        try {
            Log.v("TAG", "uploading file.....");
            Log.v("metaDataFilePath FName", uploadFPFileName);
            Log.v("screenshotImagePath", puzzleScreenshotImagePath);
            HttpClient httpClient = new DefaultHttpClient();
            // post header
            HttpPost httpPost = new HttpPost(isAdminPuzzle ? adminPuzzleUploadUrl : uploadPuzzleUrl);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("device_id", deviceid));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            if (uploadFPFileName == null || uploadFPFileName.isEmpty() || puzzleScreenshotImagePath == null || puzzleScreenshotImagePath.isEmpty()){
                showToast("Oops, Something went wrong.. Please try sharing again.");
                showToast("Oops, Something went wrong.. Please try sharing again.");
                return false;
            }
            File file = new File(uploadFPFileName);
            File shared_image = new File(puzzleScreenshotImagePath);
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
                            twitterPath = fileFbResponse.getString(0);
                            Log.v("fbPhotoPath", fbPhotoPath);
                            Log.v("twitterPath", twitterPath);
                        }
                        if (selectedShareClient.equals("Gmail")) {
                            doSendGmail();
                        } else if (selectedShareClient.equalsIgnoreCase(GALLERY_PAGE_NAME)) {
                            showToast("Puzzle is uploaded to Gallery page.");
                            //Utils.initiatePushNotifications(PlayShatteredPuzzleActivity.this, fileLink, fbPhotoPath, PUBLIC_BROADCAST_TOPIC_NAME);
                        } else if (selectedShareClient.equals("Facebook")) {
                            doInitiateFBAuthentication();
                        } else if (selectedShareClient.equals("Messages"))
                            doSendMMS();
                        else if (selectedShareClient.equals("Twitter")) {
                            makeTwitterBrowser(PlayShatteredPuzzleActivity.this);
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
            return false;
        }
        return false;
    }

    private void doInitiateFBAuthentication() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        final FP_PrefsManager fpPrefsManager = new FP_PrefsManager(PlayShatteredPuzzleActivity.this);
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
                        showToast(exception.getMessage());
                        fpPrefsManager.removeKey(FB_ACCESS_TOKEN);
                    }
                });

        if (Utils.isConnectionAvailable(this) && !fpPrefsManager.hasKey(FB_ACCESS_TOKEN)) {
            LoginManager.getInstance().logInWithReadPermissions(PlayShatteredPuzzleActivity.this, Arrays.asList("public_profile", "email"));
        } else if (Utils.isConnectionAvailable(this) && fpPrefsManager.hasKey(FB_ACCESS_TOKEN)) {
            doSharePuzzleOnFaceBook();
        } else {
            showToast("You are offline.");
        }
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

	/*protected boolean isInstagramInstalled() {
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
		return check_info;
	}*/

	/*protected void doShareInstagram() {
		Intent instagramIntent = new Intent(android.content.Intent.ACTION_SEND);
		if (uploadFbFileName != null && !uploadFbFileName.isEmpty()) {
			instagramIntent.setType("image/*");
					instagramIntent.putExtra(Intent.EXTRA_TEXT, CARRIAGE_RETURN + fileLink
					+ CARRIAGE_RETURN + textMsg + CARRIAGE_RETURN);
			 		//instagramIntent.putExtra(Intent.EXTRA_TEXT, fileLink);
			 instagramIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + uploadFbFileName));
		}
		instagramIntent.setPackage("com.instagram.android"); 
		try {
			startActivityForResult(instagramIntent, SHARE_PUZZLE_REQUEST_CODE_INSTAGRAM);
		} catch (ActivityNotFoundException e) {
			showToast("No application found on this device to perform this action");
		}
	}*/

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

    private void doShareTwitter() {
        Intent twitterIntent = new Intent(Intent.ACTION_SEND);
        twitterIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        twitterIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        twitterIntent.setType("text/plain");
        twitterIntent.putExtra(Intent.EXTRA_TEXT, CARRIAGE_RETURN + fileLink);
        if (puzzleScreenshotImagePath != null && !puzzleScreenshotImagePath.isEmpty()) {
            twitterIntent.setType("image/jpeg");
            twitterIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(puzzleScreenshotImagePath)));
        }
        twitterIntent.setPackage("com.twitter.android");
        startActivity(twitterIntent);
    }

    private void doSendGmail() {
        if (puzzleScreenshotImagePath != null) { // we need to send this photo as an
            // attachment (New Requirement on Feb
            // 14, 2015)
            File attachmentFile = new File(puzzleScreenshotImagePath);
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

                    if(className != null && !className.isEmpty()){
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

    private void doSendMMS() {
        Intent smsIntent = new Intent();
        smsIntent.setAction(Intent.ACTION_SEND);
        smsIntent.setType("text/plain");
        smsIntent.putExtra(Intent.EXTRA_TEXT, CARRIAGE_RETURN + fileLink);
        if (puzzleScreenshotImagePath != null && !puzzleScreenshotImagePath.isEmpty()) {
            smsIntent.setType("image/*");
            smsIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(puzzleScreenshotImagePath)));
        }
       // smsIntent.setPackage("com.android.mms");
        String defalutSmspackage = getDefalutSmsPackageName();
        if (defalutSmspackage!=null || !defalutSmspackage.isEmpty()){
            smsIntent.setPackage(defalutSmspackage);
        }
        try {
            startActivity(smsIntent);
        } catch (ActivityNotFoundException e) {
            showToast("No application found on this device to perform this action");
        }
    }

    private String getDefalutSmsPackageName(){
        String defalutSmsPackage = "";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            defalutSmsPackage =  Telephony.Sms.getDefaultSmsPackage(this);

        }else{
            String defApp = Settings.Secure.getString(getContentResolver(), "sms_default_application");
            PackageManager pm = getApplicationContext().getPackageManager();
            Intent iIntent = pm.getLaunchIntentForPackage(defApp);
            ResolveInfo mInfo = pm.resolveActivity(iIntent,0);
            defalutSmsPackage = mInfo.activityInfo.packageName;
        }
        return defalutSmsPackage;
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

    protected void doSharePuzzleOnFaceBook() {

        Log.v("srs-textMsg", textMsg);
        Log.v("srs-fileLink", fileLink);
        Log.v("srs-fbPhotoPath", fbPhotoPath);

        ShareDialog shareDialog = new ShareDialog(PlayShatteredPuzzleActivity.this);
        if (shareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("FracturedPhoto Puzzle")
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

        final String requestUrl = "https://graph.facebook.com/295372787327265/feed";//client id
        final List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("name", "FracturedPhoto Puzzle"));
        nameValuePairs.add(new BasicNameValuePair("link", fileLink));
        nameValuePairs.add(new BasicNameValuePair("message", "FractredPhoto"));
        nameValuePairs.add(new BasicNameValuePair("picture", fbPhotoPath));
        nameValuePairs.add(new BasicNameValuePair("access_token", "EAAHvh9XwIlwBAI2vH0J3HxfxKBxITHZC5TnMBpwGTCDuCk1WWiwPUbEpv0rk1mZCLvjajD7XCCdJHZCGEDZAgYBJnA1CfM7p9uSfDJ71J0bQGY8Ccx6Fm8KMjQH8gF6g0Odf0bNSZAtcOCFZBNT8mqXGtDNNcsrHr5kUHoeL48nwZDZD"));

        for (NameValuePair pair : nameValuePairs) {
            Log.v("xyz", pair.getName() + " : " + pair.getValue());
        }

        final RequestHandler requestHandler = new RequestHandler(requestCode, networkCallBack);
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //HttpRestConnection.execute(requestUrl, nameValuePairs, HttpRestConnection.RequestMethod.POST, requestHandler);
            }
        }).start();
    }

    protected void doPrepareCaptureImage() {
        puzzleView.setDrawingCacheEnabled(true);
        Bitmap b = Bitmap.createBitmap(puzzleView.getDrawingCache());
        puzzleView.setDrawingCacheEnabled(false);

        // temp store
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
            puzzleScreenshotImagePath = imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (puzzleView != null && !puzzleView.isPuzzleSolved()) {
            AlertDialog.Builder closeAlertBox = new AlertDialog.Builder(PlayShatteredPuzzleActivity.this);
            closeAlertBox.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (isSharedPuzzle) {
                                Intent i = new Intent(PlayShatteredPuzzleActivity.this, DashboardActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                            }
                            PlayShatteredPuzzleActivity.this.finish();
                        }
                    });

            closeAlertBox.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            closeAlertBox.setTitle("Confirm");
            closeAlertBox.setMessage("Are you sure you want to quit the puzzle?");
            closeAlertBox.setCancelable(false);
            closeAlertBox.show();
        } else if (puzzleView != null && puzzleView.isPuzzleSolved()) {
            Intent i = new Intent(PlayShatteredPuzzleActivity.this, DashboardActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            PlayShatteredPuzzleActivity.this.finish();
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

    private void bindIAPService() {
        getApplicationContext().bindService(getExplicitIapIntent(), mServiceConn, BIND_AUTO_CREATE);
    }

    private Intent getExplicitIapIntent() {
        PackageManager pm = getPackageManager();
        Intent implicitIntent = new Intent(
                "com.android.vending.billing.InAppBillingService.BIND");
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(implicitIntent,
                0);

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
                ArrayList<String> ownedSkus = activeSubs
                        .getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
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

    @Override
    public void onSuccess(int requestCode, Object object) {
        showToast("Posted on Facebook Fractured Photo Page");
        Log.v(TAG, "Response From Server:" + object.toString());
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

    /*public final class FacebookLoginDialogListener implements DialogListener {

        public void onComplete(Bundle values) {
            FacebookSessionStore.saveFBSession(mFacebook, PlayShatteredPuzzleActivity.this);
            doShareFaceBook();
            // shareToFacebookPage(100,PlayShatteredPuzzleActivity.this);
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

    public void showProgressDialog() {
        try {
            if (dialog == null) {
                dialog = ProgressDialog.show(PlayShatteredPuzzleActivity.this, "", "Please wait..");
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
        savePuzzle.setBackgroundColor(Color.TRANSPARENT);
        previewPuzzle.setBackgroundColor(Color.TRANSPARENT);
        sharePuzzleImage.setBackgroundColor(Color.TRANSPARENT);
        turnLeft.setBackgroundColor(Color.TRANSPARENT);
        turnRight.setBackgroundColor(Color.TRANSPARENT);
        fpLogo.setBackgroundColor(Color.TRANSPARENT);
        fpLogo.setImageResource(0);

        switch (screenOrientation) {
            case PORTRAIT:
                savePuzzle.setImageResource(R.drawable.save_background);
                previewPuzzle.setImageResource(R.drawable.preview_background);
                sharePuzzleImage.setImageResource(R.drawable.share_background);
                turnLeft.setImageResource(R.drawable.left_rotate);
                turnRight.setImageResource(R.drawable.right_rotate);
                fpLogo.setImageResource(R.drawable.small_logo);

                savePuzzle.setRotation(0);
                previewPuzzle.setRotation(0);
                sharePuzzleImage.setRotation(0);
                turnLeft.setRotation(0);
                turnRight.setRotation(0);
                fpLogo.setRotation(0);
                initialRotation = 0;
                if (congratsParentLayout != null) {
                    congratsParentLayout.setAngle(0);
                }
                break;

            case REVERSED_PORTRAIT:

                savePuzzle.setImageResource(R.drawable.save_background);
                previewPuzzle.setImageResource(R.drawable.preview_background);
                sharePuzzleImage.setImageResource(R.drawable.share_background);
                turnLeft.setImageResource(R.drawable.left_rotate);
                turnRight.setImageResource(R.drawable.right_rotate);
                fpLogo.setImageResource(R.drawable.small_logo);

                savePuzzle.setRotation(180);
                previewPuzzle.setRotation(180);
                sharePuzzleImage.setRotation(180);
                turnLeft.setRotation(180);
                turnRight.setRotation(180);
                fpLogo.setRotation(180);
                initialRotation = 180;
                if (congratsParentLayout != null) {
                    congratsParentLayout.setAngle(180);
                }
                break;

            case REVERSED_LANDSCAPE:

                savePuzzle.setImageResource(R.drawable.save_background_land);
                previewPuzzle.setImageResource(R.drawable.preview_background_land);
                sharePuzzleImage.setImageResource(R.drawable.share_background_land);
                turnLeft.setImageResource(R.drawable.left_rotate_land);
                turnRight.setImageResource(R.drawable.right_rotate_land);
                fpLogo.setImageResource(R.drawable.small_logo_land);

                savePuzzle.setRotation(270);
                previewPuzzle.setRotation(270);
                sharePuzzleImage.setRotation(270);
                turnLeft.setRotation(270);
                turnRight.setRotation(270);
                fpLogo.setRotation(270);
                initialRotation = 90;
                if (congratsParentLayout != null) {
                    congratsParentLayout.setAngle(90);
                }
                break;

            case LANDSCAPE:

                savePuzzle.setImageResource(R.drawable.save_background_land);
                previewPuzzle.setImageResource(R.drawable.preview_background_land);
                sharePuzzleImage.setImageResource(R.drawable.share_background_land);
                turnLeft.setImageResource(R.drawable.left_rotate_land);
                turnRight.setImageResource(R.drawable.right_rotate_land);
                fpLogo.setImageResource(R.drawable.small_logo_land);

                savePuzzle.setRotation(90);
                previewPuzzle.setRotation(90);
                sharePuzzleImage.setRotation(90);
                turnLeft.setRotation(90);
                turnRight.setRotation(90);
                fpLogo.setRotation(90);
                initialRotation = 270;
                if (congratsParentLayout != null) {
                    congratsParentLayout.setAngle(270);
                }
                break;
        }
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
}