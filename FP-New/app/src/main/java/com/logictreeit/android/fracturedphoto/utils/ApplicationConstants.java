package com.logictreeit.android.fracturedphoto.utils;

import android.os.Environment;

public interface ApplicationConstants {

	int ONE_SECOND                                             = 1000;
	int SPLASH_SCREEN_DURATION                                 = 2 * ONE_SECOND;
	String SDCARD_PATH                                         = String.valueOf(Environment.getExternalStorageDirectory());
	String DATE_TIME_FORMAT_IN_FILE_NAMES                      = "dd_MMM_yyyy_HH_mm_ss";
	String IMAGE_FILE_EXTENSION                                = ".jpg";
	String META_FILE_EXTENSION                                 = ".txt";
	String META_DATA_FILE_NAME                                 = "MetaFile" + META_FILE_EXTENSION;
	String SEPARATOR_SYMBOL                                    = "_";
	String ZIP_EXTENSION                                       = ".fp";
	String SHARING_ZIP_FOLDER_NAME                             = SEPARATOR_SYMBOL + "SharingPuzzle" + ZIP_EXTENSION;
	String DASH_LINE                                           = "\n";
	int sleepTime                                              = 60000;
	String hyperLinkText                                       = "Tap the link to download the puzzle. Requires Fractured Photo app. ";
	String CARRIAGE_RETURN                                     = "Tap the link to download the puzzle. This requires Fractured Photo app on your device. ";
	String textMsg                                             = "[Tap the link to download the puzzle. Requires Fractured Photo app]";
	boolean IS_ADMIN_MODE                                      = true;
	String FB_ACCESS_TOKEN                                     = "FB_ACCESS_TOKEN";
	String PUZZLE_DOWNLOADS_SKU								   = "com.dran.fracturedphoto.gallary";
	String ADS_SKU                                             = "com.dran.fracturedphoto.ads";
	String SHATTERED_PUZZLE_SKU                                = "com.dran.fracturedphoto.fracturedpuzzle";
	String NODE_NAME                                           = "registration_tokens";
	String GALLERY_PAGE_NAME                                   = "Fractured Photo Gallery";
	String PUBLIC_BROADCAST_TOPIC_NAME                         = "admin_uploads";
	String SELF_BROADCAST_TOPIC_NAME                           = "admin_uploads_self";
	String FP_BASE_URL                                         = "https://fracturedphotoapp.com/";

	// Keys Extras
	public static class Extras_Keys {
		public static final String PHOTO_PATH                  = "clickedPhotoPath";
		public static final String PHOTO_URI                   = "clickedPhotoURI";
		public static final String PHOTO_ORIENTATION           = "clickedPhotoOrientation";
		public static final String NUM_OF_PIECES               = "numOfPieces";
		public static final String PUZZLE_NAME                 = "puzzleName";
		public static final String PUZZLE_HISTORY_ROW_ID_IN_DB = "pzlRowID";
		public static final String IS_CREATE_PUZZLE_MODE       = "isCreatePuzzleMode";
		public static final String IS_SHARED_PUZZLE            = "isSharedPuzzle";
		public static final String META_DATA_FILE_PATH         = "MetaDataFilePath";
		public static final String PATTERN_TYPE                = "puzzlePatternType";
        public static final String FROM_PUSH                   = "fromPush";
        public static final String PUZZLE_URL                  = "puzzle_url"; //this should be matched with notification data
    }

	class Prefs{
		public static final String DEVICE_UNIQUE_ID            = "deviceUniqueId";
	}
}
