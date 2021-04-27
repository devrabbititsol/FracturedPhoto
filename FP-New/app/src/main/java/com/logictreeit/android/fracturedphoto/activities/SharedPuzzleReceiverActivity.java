package com.logictreeit.android.fracturedphoto.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
//import com.crashlytics.android.Crashlytics;
import com.dran.fracturedphoto.R;
import com.logictreeit.android.fracturedphoto.helpers.UnzipHelper;
import com.logictreeit.android.fracturedphoto.utils.ApplicationConstants;
import com.logictreeit.android.fracturedphoto.utils.PuzzleType;
import com.logictreeit.android.fracturedphoto.utils.Utils;
//import io.fabric.sdk.android.Fabric;

public class SharedPuzzleReceiverActivity extends Activity implements ApplicationConstants {

    private static final int PERMISSIONS_REQUEST = 321;
    private String cachedDir;
    public static final String ATTACHMENT_NAME = "Attachment.fp";
    private String PUZZLE_DOWNLOAD_URL = FP_BASE_URL + "puzzles/";
    private String puzzleNameWithExtension;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shared_puzzle_receiver_activity);
        //Fabric.with(this, new Crashlytics());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
            return;
        }
        onCreate();
    }

    public void onCreate() {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                cachedDir = getExternalCacheDir().getAbsolutePath() + File.separator + getString(R.string.app_name) + File.separator;
            } else {
                cachedDir = getCacheDir().getAbsolutePath() + File.separator + getString(R.string.app_name) + File.separator;
            }
            if (!new File(cachedDir).exists()) {
                new File(cachedDir).mkdir();
            }
            String[] tokens;
            Uri data = getIntent().getData();
            if (data != null) {
                String content = data.toString();
                this.puzzleNameWithExtension = null;
                if (content.startsWith("http")) {
                    tokens = content.split("//");
                    System.out.println("token  is" + content);
                    System.out.println("token of 1 is" + tokens[1]);
                    puzzleNameWithExtension = tokens[1].split("=")[1];
                    System.out.println("appendToken is" + puzzleNameWithExtension);
                } else if (content.startsWith("https")) {
                    tokens = content.split("//");
                    System.out.println("token  is" + content);
                    System.out.println("token of 1 is" + tokens[1]);
                    puzzleNameWithExtension = tokens[1].split("=")[1];
                    System.out.println("appendToken is" + puzzleNameWithExtension);
                } else {
                    tokens = content.split("//");
                    System.out.println("token  is" + content);
                    System.out.println("token of 1 is" + tokens[1]);
                    if (tokens[1].startsWith("admin")) {
                        puzzleNameWithExtension = tokens[1].replace("admin", "");
                    } else {
                        puzzleNameWithExtension = tokens[1];
                    }
                    System.out.println("appendToken 2 is" + puzzleNameWithExtension);
                }
                if (content.endsWith(".fp")) {
                    initiateDownload(PUZZLE_DOWNLOAD_URL + puzzleNameWithExtension);
                }
            } else {
                showToast2("Something went wrong.\nPlease try again later");
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast2("Something went wrong.\nPlease try again later");
            finish();
        }
    }

    private void initiateDownload(String puzzleUrl) {
        if (Utils.isConnectionAvailable(SharedPuzzleReceiverActivity.this)) {
            new DownloadPuzzleTask().execute(puzzleUrl);
        } else {
            String message = "You are offline. Please check your connectivity.";
            showAlert(SharedPuzzleReceiverActivity.this, message);
        }
    }

    private class DownloadPuzzleTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressBar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = ProgressDialog.show(SharedPuzzleReceiverActivity.this, "Downloading", "Please wait...");
            progressBar.setCancelable(true);
            progressBar.setCanceledOnTouchOutside(false);
            progressBar.setIndeterminate(true);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String downloadedFilePath = cachedDir + ATTACHMENT_NAME;
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
                showToast2("Couldn't download. Something went wrong. " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String downloadedAttachmentPath) {
            super.onPostExecute(downloadedAttachmentPath);
            if (progressBar != null && progressBar.isShowing()) {
                progressBar.dismiss();
            }
            if (downloadedAttachmentPath != null) {
                readAndExtractAttachment(SharedPuzzleReceiverActivity.this, downloadedAttachmentPath);
            } else {
                showToast2("Something went wrong.\nPlease try again later");
                finish();
            }
        }
    }

    public static void readAndExtractAttachment(Activity act, String downloadedAttachmentPath) {
        try {
            String allSharedPuzzlesFolderPath = SDCARD_PATH + File.separator + act.getString(R.string.app_name) + "_SharedPuzzles" + File.separator;
            File allSharedPuzzlesFolder = new File(allSharedPuzzlesFolderPath);
            if (!allSharedPuzzlesFolder.exists()) {
                allSharedPuzzlesFolder.mkdir();
            }
            allSharedPuzzlesFolderPath = allSharedPuzzlesFolder.getAbsolutePath();
            File unzipTempFolder = new File(allSharedPuzzlesFolderPath + File.separator + "xxx");
            if (!unzipTempFolder.exists()) {
                unzipTempFolder.mkdir();
            }

            //Unzipping
            UnzipHelper unzip = new UnzipHelper();
            unzip.unzip(downloadedAttachmentPath, unzipTempFolder.getAbsolutePath());

            String imageFileName = "";
            String metaFileName = "";
            if (unzipTempFolder.listFiles().length >= 1) {
                for (File f : unzipTempFolder.listFiles()) {
                    String unzippedFileName = f.getName();
                    if (unzippedFileName.endsWith(META_FILE_EXTENSION)) {
                        metaFileName = unzippedFileName.replace(META_FILE_EXTENSION, "");
                    } else {
                        imageFileName = unzippedFileName.replace(IMAGE_FILE_EXTENSION, "");
                    }
                }

                File singleRecievedFolder = new File(allSharedPuzzlesFolderPath + File.separator + imageFileName);
                unzipTempFolder.renameTo(singleRecievedFolder);

                if (unzipTempFolder.exists()) {
                    for (File f : unzipTempFolder.listFiles()) {
                        f.delete();
                    }
                    unzipTempFolder.delete();
                }
                readContents(act, imageFileName + IMAGE_FILE_EXTENSION, metaFileName + META_FILE_EXTENSION, singleRecievedFolder.getAbsolutePath());
            } else {
                //This is what happening if the size of attachment is less
                showToast(act,"Something went wrong.\nPlease try again later");
                act.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast(act,"Something went wrong.\nPlease try again later");
            act.finish();
        }
    }

    public static void readContents(Activity act, String unzippedImageFileName, String metaFileName, String unzippedFolderPath) {
        try {
            String puzzleName = null;
            String puzzleType = null;
            String numOfPieces = null;
            String patternTypeString = null;

            File folder = new File(unzippedFolderPath);
            boolean isMetaFileExists = false;
            for (File file : folder.listFiles()) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (fileName.endsWith(META_FILE_EXTENSION)) {
                        isMetaFileExists = true;
                        puzzleName = fileName.replace(META_FILE_EXTENSION, "");
                        if (Utils.isShatteredPuzzle(file)) {
                            puzzleType = PuzzleType.SHATTERED_PUZZLE.getPuzzleType();
                            patternTypeString = Utils.getShatteredPuzzlePatternType(file);
                        } else {
                            puzzleType = PuzzleType.SQUARE_PUZZLE.getPuzzleType();
                            numOfPieces = "12";
                        }
                    }
                }
            }
            Log.v("puzzleName", "" + puzzleName);
            Log.v("patternTypeString", "" + patternTypeString);
            if (unzippedImageFileName != null && puzzleName != null && puzzleType != null && isMetaFileExists) {
                if (puzzleType.equals(PuzzleType.SHATTERED_PUZZLE.getPuzzleType())) {
                    Intent iShare = new Intent(act, PlayShatteredPuzzleActivity.class);
                    iShare.putExtra(Extras_Keys.IS_SHARED_PUZZLE, true);
                    iShare.putExtra(Extras_Keys.PUZZLE_NAME, puzzleName);
                    iShare.putExtra(Extras_Keys.PATTERN_TYPE, Integer.parseInt(patternTypeString));
                    iShare.putExtra(Extras_Keys.PHOTO_PATH, unzippedFolderPath + File.separator + unzippedImageFileName);
                    iShare.putExtra(Extras_Keys.META_DATA_FILE_PATH, unzippedFolderPath + File.separator + metaFileName);
                    act.startActivity(iShare);
                } else if (puzzleType.equals(PuzzleType.SQUARE_PUZZLE.getPuzzleType())) {
                    Intent iShare = new Intent(act, PlaySquarePuzzleActivity.class);
                    iShare.putExtra(Extras_Keys.IS_SHARED_PUZZLE, true);
                    iShare.putExtra(Extras_Keys.PUZZLE_NAME, puzzleName);
                    iShare.putExtra(Extras_Keys.PHOTO_PATH, unzippedFolderPath + File.separator + unzippedImageFileName);
                    iShare.putExtra(Extras_Keys.META_DATA_FILE_PATH, unzippedFolderPath + File.separator + metaFileName);
                    iShare.putExtra(Extras_Keys.NUM_OF_PIECES, Integer.parseInt(numOfPieces));
                    act.startActivity(iShare);
                }
                act.finish();
            } else {
                showToast(act,"Missing Meta File");
                act.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast(act,"Something went wrong.\nPlease try again later");
            act.finish();
        }
    }

    public static void showToast(Activity act, final String msg) {
        act.runOnUiThread(() -> Toast.makeText(act, msg, Toast.LENGTH_LONG).show());
    }

    private void showToast2(final String msg) {
        runOnUiThread(() -> Toast.makeText(SharedPuzzleReceiverActivity.this, msg, Toast.LENGTH_LONG).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                if (grantResults.length >= 3 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    onCreate();
                } else {
                    Toast.makeText(this, "Please grant all permissions to proceed.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void showAlert(Activity act, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(act);
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, id) -> {
                    dialog.dismiss();
                    SharedPuzzleReceiverActivity.this.finish();
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
