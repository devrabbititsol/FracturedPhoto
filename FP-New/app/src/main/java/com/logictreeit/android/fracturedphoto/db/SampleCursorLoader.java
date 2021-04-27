package com.logictreeit.android.fracturedphoto.db;

import android.content.Context; 
import android.content.CursorLoader;
import android.database.Cursor;

public class SampleCursorLoader extends CursorLoader {
	private FracturePhotoDB db;
	private final ForceLoadContentObserver mObserver = new ForceLoadContentObserver();
	private String methodName;
	private String pzlRowId;

	public SampleCursorLoader(Context context, FracturePhotoDB db, String methodName, String pzlRowId) {
		super(context);
		this.db = db;
		this.methodName = methodName;
		this.pzlRowId = pzlRowId;
	}

	@Override
	public Cursor loadInBackground() {
		Cursor cursor = null;
		if(methodName.equals("loadPuzzleHistory")){
			cursor = db.loadPuzzlesHistory();
		}else if(methodName.equals("loadSquareTilesDetailsOfPuzzleId")){
			cursor = db.loadSquareTilesDetailsOfPuzzleId(pzlRowId);
		}else if(methodName.equals("loadShatteredTilesDetailsOfPuzzleId")){
			cursor = db.loadShatteredTilesDetailsOfPuzzleId(pzlRowId);
		}
		
		if (cursor != null) {
			cursor.getCount();
			cursor.registerContentObserver(mObserver);
		}
		return cursor;
	}
};