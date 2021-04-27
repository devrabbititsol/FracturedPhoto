 package com.logictreeit.android.fracturedphoto.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper; 

public class FracturePhotoDBHelper  extends SQLiteOpenHelper{

	private static final String CREATE_TABLE_FORMAT = "CREATE TABLE %1$s (%2$s)";


	private static final String CREATE_QUERY1 = String.format(CREATE_TABLE_FORMAT, new Object[] {
			FracturePhotoDBModel.getPuzzleHistoryTableName(), FracturePhotoDBModel.getPuzzleHistoryCreateString()
	}); 
	private static final String CREATE_QUERY2 = String.format(CREATE_TABLE_FORMAT, new Object[] {
			FracturePhotoDBModel.getTileTableName(), FracturePhotoDBModel.getTileCreateString()
	}); 
	private static final String CREATE_QUERY3 = String.format(CREATE_TABLE_FORMAT, new Object[] {
			FracturePhotoDBModel.getShatteredTileTableName(), FracturePhotoDBModel.getShatteredTileCreateString()
	}); 

	public FracturePhotoDBHelper(Context context) {
		super(context, FracturePhotoDBModel.getDBName(), null, FracturePhotoDBModel.getDBVersion());
	}

	public void onCreate(SQLiteDatabase db) {
		try  {
			db.execSQL(CREATE_QUERY1);
			db.execSQL(CREATE_QUERY2);
			db.execSQL(CREATE_QUERY3);
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		try{
			db.execSQL((new StringBuilder("DROP TABLE IF EXISTS ")).append(FracturePhotoDBModel.getPuzzleHistoryTableName()).toString());
			db.execSQL((new StringBuilder("DROP TABLE IF EXISTS ")).append(FracturePhotoDBModel.getTileTableName()).toString());
			db.execSQL((new StringBuilder("DROP TABLE IF EXISTS ")).append(FracturePhotoDBModel.getShatteredTileTableName()).toString());
		}catch(SQLiteException e) {
			e.printStackTrace();
		} 
		onCreate(db);
	}

}

