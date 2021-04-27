package com.logictreeit.android.fracturedphoto.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.loader.content.CursorLoader;

import com.dran.fracturedphoto.R;
import com.logictreeit.android.fracturedphoto.activities.PlayShatteredPuzzleActivity;
import com.logictreeit.android.fracturedphoto.helpers.BitmapHelper;
import com.logictreeit.android.fracturedphoto.helpers.Mask;
import com.logictreeit.android.fracturedphoto.helpers.VolumeControlHelper;
import com.logictreeit.android.fracturedphoto.models.ShatteredTile;

import org.json.JSONException;
import org.json.JSONObject;

public class Utils implements ApplicationConstants {
    private static SharedPreferences preferences = null;
    private static final String KEY_DISCLAIMER = "key_disclaimer";

    public static String getRealPathFromURI(Context ctxt, Uri contentUri) {
        try {
            Cursor cursor = new CursorLoader(ctxt, contentUri, new String[]{MediaStore.Images.Media.DATA}, null, null, null).loadInBackground();
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            return cursor.getString(column_index);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getPhotoOrientation(String photoPath) {
        /*Cursor cursor = new CursorLoader(ctxt, photoUri,new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null).loadInBackground();
		try {
			cursor.moveToFirst();
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
			return cursor.getInt(column_index);
		} catch (Exception e) {
			System.err.println("ORIENTATIOIN Cursor may be NULL");
		 	e.printStackTrace();
			return 90; 
		}*/
        try {
            switch (new ExifInterface(photoPath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean deleteLastPhotoFromDCIM() {
        try {
            File[] images = new File(Environment.getExternalStorageDirectory() + File.separator + "DCIM/Camera").listFiles();
            File latestSavedImage = images[0];
            for (int i = 1; i < images.length; ++i) {
                if (images[i].lastModified() > latestSavedImage.lastModified()) {
                    latestSavedImage = images[i];
                }
            }
            return latestSavedImage.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void deleteLastPhotoTaken(Context ctxt) {
        Cursor cursor = new CursorLoader(ctxt,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.MIME_TYPE}, null, null,
                MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC")
                .loadInBackground();

        if (cursor != null) {
            cursor.moveToFirst();
            File file = new File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public static Uri getImageUriFromPath(Context context, String filePath) {
        Cursor cursor = new CursorLoader(context,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null).loadInBackground();

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            refreshMediaProvider(context, filePath);
            return Uri.withAppendedPath(baseUri, String.valueOf(id));
        } else {
            if (new File(filePath).exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                refreshMediaProvider(context, filePath);
                return uri;
            } else {
                return null;
            }
        }
    }

    private static void refreshMediaProvider(Context appContext, String fileName) {
        MediaScannerConnection scanner = null;
        try {
            scanner = new MediaScannerConnection(appContext, null);
            scanner.connect();

            if (scanner.isConnected()) {
                Log.d("XXXX", "Requesting scan for file " + fileName);
                scanner.scanFile(fileName, null);
            }
        } catch (Exception e) {
            Log.e("XXXX", "Cannot to scan file", e);
        } finally {
            if (scanner != null) {
                scanner.disconnect();
            }
        }
    }

    public static String getNewFileName() {
        return "fracturedphoto_" + getCurrentTime();
    }

    public static String getCurrentTime() {
        return new SimpleDateFormat(DATE_TIME_FORMAT_IN_FILE_NAMES, Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    public static Intent openGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra("return-data", true);
        return intent;
    }

    public static String getClickedPhotoPath(Context c) {
        return getAppDirPath(c) + File.separator + Utils.getNewFileName() + IMAGE_FILE_EXTENSION;
    }

    public static String getAppDirPath(Context c) {
        return SDCARD_PATH + File.separator + c.getString(R.string.app_name);
    }

    public static int calculateNumOfPieces(Bitmap photoBitmap) {

        int bmpWidth = photoBitmap.getWidth();
        int bmpHeight = photoBitmap.getHeight();
        int scale;

        if (bmpWidth > bmpHeight) {
            scale = bmpWidth % bmpHeight;
            photoBitmap = BitmapHelper.getRotatedBitmap(photoBitmap, 90);
        } else if (bmpWidth < bmpHeight) {
            scale = bmpHeight % bmpWidth;
        } else {
            photoBitmap = BitmapHelper.getResizedBitmap(photoBitmap, 240, 320);
            scale = bmpHeight % bmpWidth;
        }
        return (bmpWidth / scale) * (bmpHeight / scale);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixels(float dp, Context context) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static void applyFontForWholeView(Context c, View v, String type) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); ++i) {
                    View child = vg.getChildAt(i);
                    applyFontForWholeView(c, child, type);
                }
            } else if (v instanceof TextView || v instanceof EditText) {
                ((TextView) v).setTypeface(type.equalsIgnoreCase("Bold") ? getBoldTypeface(c) : getRegularTypeface(c), type.equalsIgnoreCase("Bold") ? Typeface.BOLD : Typeface.NORMAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Typeface getRegularTypeface(Context c) {
        return Typeface.createFromAsset(c.getAssets(), "fonts/AmaticSC-Regular-webfont.ttf");
    }

    private static Typeface getBoldTypeface(Context c) {
        return Typeface.createFromAsset(c.getAssets(), "fonts/AmaticSC-Bold-webfont.ttf");
    }

    public static ArrayList<String[]> readMetaDataFile(String metaDataFilePath) {

        ArrayList<String[]> list = new ArrayList<String[]>();
        FileInputStream fis = null;
        BufferedReader reader = null;
        try {
            fis = new FileInputStream(metaDataFilePath);
            reader = new BufferedReader(new InputStreamReader(fis));
            String line = reader.readLine();
            while (line != null) {
                System.err.println("line = " + line);
                list.add(line.split(SEPARATOR_SYMBOL));
                line = reader.readLine();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                reader.close();
                fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return list;
    }

    public static String getArrayListItemsAsStringInSingleLine(ArrayList<Integer> list) {
        String result = "";
        for (int i = 0; i < list.size(); ++i) {
            result += (list.get(i) + SEPARATOR_SYMBOL);
        }
        return result;
    }

    public static ArrayList<Integer> getArrayListFromStringLine(String stringLine) {
        String attachedWith[] = stringLine.split(SEPARATOR_SYMBOL);
        ArrayList<Integer> attachedWithList = new ArrayList<Integer>();
        for (int i = 0; i < attachedWith.length; ++i) {
            attachedWithList.add(Integer.parseInt(attachedWith[i]));
        }
        return attachedWithList;
    }

    public static Bitmap getComboBitmapOfTwoTiles(Activity act, ShatteredTile tile1, ShatteredTile tile2, int photoWidth, int photoHeight, boolean isSharedPuzzle) {
        if (!isSharedPuzzle) {
            doPlaySound(act, R.raw.click);
        }
        short currentAngle = tile1.getOrientation();
        if (currentAngle != 0) {
            tile1.turnTile(currentAngle, (short) 0);
            tile2.turnTile(currentAngle, (short) 0);
            tile1.setOrientation((short) 0);
            tile2.setOrientation((short) 0);
        }

        int minX1 = Math.min(tile1.getX1(), tile2.getX1());
        int minY1 = Math.min(tile1.getY1(), tile2.getY1());
        int maxX2 = Math.max(tile1.getX2(), tile2.getX2());
        int maxY2 = Math.max(tile1.getY2(), tile2.getY2());

        int width = maxX2 - minX1;
        int height = maxY2 - minY1;

        Bitmap hugeBitmap = Bitmap.createBitmap(photoWidth, photoHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(hugeBitmap);
        canvas.drawBitmap(tile1.getDisplayBitmap(), tile1.getX1(), tile1.getY1(), null);
        canvas.drawBitmap(tile2.getDisplayBitmap(), tile2.getX1(), tile2.getY1(), null);
        Bitmap comboBitmap = Bitmap.createBitmap(hugeBitmap, minX1, minY1, width, height);

        if (currentAngle != 0) {
            tile1.turnTile((short) 0, currentAngle);
            tile2.turnTile((short) 0, currentAngle);
            tile1.setOrientation(currentAngle);
            tile2.setOrientation(currentAngle);

            if (!isSharedPuzzle) {
                tile1.getDisplayBitmap().recycle();
                tile1.getOriginalBitmap().recycle();
                tile2.getDisplayBitmap().recycle();
                tile2.getOriginalBitmap().recycle();
            }
            hugeBitmap.recycle();

            tile1.setOriginalBitmap(comboBitmap);
            return BitmapHelper.getRotatedBitmap(comboBitmap, currentAngle * 90);
        } else {
            if (!isSharedPuzzle) {
                tile1.getDisplayBitmap().recycle();
                tile1.getOriginalBitmap().recycle();
                tile2.getDisplayBitmap().recycle();
                tile2.getOriginalBitmap().recycle();
            }
            hugeBitmap.recycle();

            tile1.setOriginalBitmap(comboBitmap);
            return comboBitmap;
        }
    }

    public static Bitmap getMaskedBitmap(Resources res, Bitmap source, Mask mask) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            options.inMutable = true;
        }
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap maskedImageBitmap;
        if (source.isMutable()) {
            maskedImageBitmap = source;
        } else {
            maskedImageBitmap = source.copy(Bitmap.Config.ARGB_8888, true);
            source.recycle();
        }
        maskedImageBitmap.setHasAlpha(true);
        Canvas canvas = new Canvas(maskedImageBitmap);
        Bitmap maskBitmap = BitmapFactory.decodeResource(res, mask.getResourceId());
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(maskBitmap, mask.getX1(), mask.getY1(), paint);
        maskBitmap.recycle();
        return maskedImageBitmap;
    }

    public static ArrayList<Integer> removeDuplicatesFromList(ArrayList<Integer> list) {
        HashSet<Integer> hs = new HashSet<Integer>();
        hs.addAll(list);
        list.clear();
        list.addAll(hs);
        Collections.sort(list);
        return list;
        //return new ArrayList<Integer>(new LinkedHashSet<Integer>(list));
    }

    public static void doPlaySound(final Activity act, final int rawFileId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MediaPlayer player = MediaPlayer.create(act, rawFileId);
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    //int maxStreamVolume = VolumeControlHelper.getStreamMaxVolume(act);
                    //if (maxStreamVolume >= 10) {
                        //maxStreamVolume = 10;
                    //}
                    //VolumeControlHelper.getAudioManager(act).setStreamVolume(AudioManager.STREAM_MUSIC, maxStreamVolume, AudioManager.FLAG_PLAY_SOUND);

                    player.setOnCompletionListener(new OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mPlayer) {
                            try {
                                mPlayer.reset();
                                mPlayer.release();
                                mPlayer = null;
                            } catch (Exception ignore) {
                            }
                        }
                    });
                    player.start();
                } catch (Exception ignore) {
                }
            }
        }).start();
    }

    public static String getGmailBody(Activity act) {
        return "To solve this puzzle you need to have FracturedPhoto app installed on your device." +
                "You can download it from\n\nhttps://play.google.com/store/apps/details?id=" + act.getPackageName() + "\n\n";
    }

    public static boolean isConnectionAvailable(Activity activity) {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                    if (capabilities != null) {
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            connected = true;
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            connected = true;
                        }
                    }
                } else {
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    if (activeNetwork != null) {
                        if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                            connected = true;
                        } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                            connected = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return connected;
    }

    public static void NetworkMessage(Activity act, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(act);

        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPathOfExternalPhotoFromUri(final Context context, final Uri uri) {
        try {
            final boolean isKitKatORAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

            // DocumentProvider
            if (isKitKatORAbove && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } /*else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}*/

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                // Return the remote address
                if (isGooglePhotosUri(uri))
                    return uri.getLastPathSegment();

                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static void setDisclaimerAgreed(Context mContext, boolean isAgreed) {
        if (preferences == null) {
            preferences = PreferenceManager
                    .getDefaultSharedPreferences(mContext);
        }

        preferences.edit().putBoolean(KEY_DISCLAIMER, isAgreed).commit();
    }

    public static boolean isDisclaimerAgreed(Context mContext) {
        if (preferences == null) {
            preferences = PreferenceManager
                    .getDefaultSharedPreferences(mContext);
        }
        return preferences.getBoolean(KEY_DISCLAIMER, false);
    }

    public static String getShatteredPuzzlePatternType(File file) {
        FileInputStream fis = null;
        BufferedReader reader = null;
        String patternType = null;
        try {
            fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis));
            String line = reader.readLine();
            while (line != null) {
                patternType = line.split(SEPARATOR_SYMBOL)[12];
                break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                reader.close();
                fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return patternType;
    }

    public static boolean isShatteredPuzzle(File file) {
        FileInputStream fis = null;
        BufferedReader reader = null;
        boolean puzzleType = false;
        try {
            fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis));
            String line = reader.readLine();
            while (line != null) {
                puzzleType = line.split(SEPARATOR_SYMBOL).length > 4;
                break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                reader.close();
                fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return puzzleType;
    }

    public static int getNumOfTilesInPattern(int patternType) {
        if (patternType == 0) {
            return 35;
        } else if (patternType == 1) {
            return 29;
        } else {
            return 27;
        }
    }

    public static String getUniqueDeviceId(Context context) {
        try {
            FP_PrefsManager spManager = new FP_PrefsManager(context);
            if (spManager.hasKey(Prefs.DEVICE_UNIQUE_ID)) {
                return spManager.get(Prefs.DEVICE_UNIQUE_ID);
            }
            spManager.save(Prefs.DEVICE_UNIQUE_ID, Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
            return spManager.get(Prefs.DEVICE_UNIQUE_ID);
        } catch (Exception e) {
            return "";
        }
    }

    public static ArrayList<String[]> doValidateMetaTokens(ArrayList<String[]> metaTokensList, int numOfTiles, boolean isSharedFromAndroid) {
        ArrayList<Integer> receivedTileIds = new ArrayList<>();
        ArrayList<Integer> receivedTilePositions = new ArrayList<>();
        for (String[] tokensArray : metaTokensList) {
            receivedTileIds.add(Integer.parseInt(tokensArray[0]));
            receivedTilePositions.add(Integer.parseInt(tokensArray[17]));
        }

        ArrayList<Integer> missedPositions = new ArrayList<>();
        for (int id = 0; id < numOfTiles; ++id) {
            if (!receivedTilePositions.contains(id)) {
                missedPositions.add(id);
            }
        }
        //Collections.shuffle(missedPositions);
        for (int recycledTileId = 0, j = 0; recycledTileId < numOfTiles; ++recycledTileId) {
            if (!receivedTileIds.contains(recycledTileId)) {
                metaTokensList.add(doConstructNewMetaLine(recycledTileId, missedPositions.get(j)));
                ++j;
            }
        }
        return metaTokensList;
    }
/*
0. tile.getTileId()
1. tile.getCurrentX()
2. tile.getCurrentY()
3. photoBitmap.getWidth()
4. photoBitmap.getHeight()
5. tile.getX2()
6. tile.getY2()
7. tile.getOrientation()
8. (tile.isDroppedOnPuzzleView() ? 1 : 0)
9. (tile.isTileRecycled() ? 1 : 0)
10. tile.getCurrentPosition()
11. (tile.isCenterPieceOfPuzzle() ? 1 : 0)
12. tile.getPatternType()
13. 1 : always 1. BECOZ, its Android
14. (tile.isDroppedOnPuzzleView() ? 0 : galleryAdapter.getTilePosition(tile))
15. total X
16. total Y
17. position from iOS-to-Android
18. curX
19. curY
20. Utils.getArrayListItemsAsStringInSingleLine(tile.getAttachedWith()));*/

    private static String[] doConstructNewMetaLine(int recycledTileId, int curPosition) {
        String line = (recycledTileId + "_0_0_1_1_0_0_0_1_0_" + curPosition + "_0_0_1_15_1_1_" + curPosition + "_0_0_" + recycledTileId);
        Log.v("lineeeeeeeeee:", line);
        return line.split(SEPARATOR_SYMBOL);
    }

    public static void shootPushNotification(final Context context, JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v("FCM Push response", response.toString());
                        String msg;
                        if (response.has("message_id")) {
                            msg = "Push notification sent successfully.";
                        } else {
                            msg = response.toString();
                        }
                        if (context != null) {
                            Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
                            t.setGravity(Gravity.CENTER, 0, 0);
                            t.show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("FCM Push error", new String(error.networkResponse.data));
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "key=AAAAmaewWRM:APA91bE69LwP0six2bCuf1bGtgxB2fnGEG5C0hPtJ5vFt3dDs3lxk80smnm1LbP2yFOdlqB1drDv8JzARWUM2w6IvFe76PGD4_xV7dVCzob1KZ82QFEwBcWPIt64coXnsE5TmU0YCsP9");
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        VolleyRequestHandler.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public static void initiatePushNotification(Context context, String puzzleUrl, String previewUrl, String topic) {

        String title = "Fractured Photo";
        String messageBody = "New puzzle is uploaded to Fractured Photo gallery";

        try {
            JSONObject notification = new JSONObject();
            JSONObject notificationBody = new JSONObject();
            JSONObject dataBody = new JSONObject();

            notificationBody.put("title", title);
            notificationBody.put("body", messageBody);
            notificationBody.put("puzzle_url", puzzleUrl);
            notificationBody.put("preview_image", previewUrl);

            dataBody.put("title", title);
            dataBody.put("body", messageBody);
            dataBody.put("puzzle_url", puzzleUrl);
            dataBody.put("preview_image", previewUrl);

            notification.put("to", "/topics/" + topic);
            notification.put("data", dataBody);
            notification.put("notification", notificationBody);

            Utils.shootPushNotification(context, notification);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.v("Exception", e.getMessage());
        }
    }
}
