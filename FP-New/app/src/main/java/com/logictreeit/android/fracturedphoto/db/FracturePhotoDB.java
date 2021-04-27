package com.logictreeit.android.fracturedphoto.db;
 
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.logictreeit.android.fracturedphoto.helpers.BitmapHelper;
import com.logictreeit.android.fracturedphoto.models.ShatteredTile;
import com.logictreeit.android.fracturedphoto.models.SquareTile;
import com.logictreeit.android.fracturedphoto.utils.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class FracturePhotoDB {

	//private static FracturePhotoDB fDB; 
	//private SQLiteDatabase db;
	private FracturePhotoDBHelper dbHelper;
	private SQLiteDatabase db;

	FracturePhotoDB(){
	}

	/*public FracturePhotoDB(Context context){
		if(fDB == null){
			FracturePhotoDBHelper helper = new FracturePhotoDBHelper(context);
			db = helper.getWritableDatabase();
		}
	}

	public static synchronized FracturePhotoDB getInstance(Context context ) {
		if (null == nomSnapDB) {
			fDB = new FracturePhotoDB(context);
		}
		return fDB;
	}*/
	public FracturePhotoDB(Context context) {
		dbHelper = new FracturePhotoDBHelper(context);
	}

	public void openDB() throws SQLException {
		db = dbHelper.getWritableDatabase();
	}

	public void closeDB() {
		dbHelper.close();
	}
		 
	public Cursor loadPuzzlesHistory() {
		 return db.query(FracturePhotoDBModel.getPuzzleHistoryTableName(), null, null, null, null, null, FracturePhotoDBModel.COL_ID1+" DESC");
	}
	
	public long saveSquareTilesDetails(long rowId, int numOfPieces, SquareTile tile, String numOfColumns, String colWidth) {
		
		ContentValues values = new ContentValues();
		values.put(FracturePhotoDBModel.COL_BASE_ROW_ID, String.valueOf(rowId));
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_ID, tile.getTileId());
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_CUR_POS, tile.getCurrentPosition());
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_ORIG_POS, tile.getTileOriginalPosition());
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_ROTATION, tile.getRotation());
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_WIDTH, tile.getWidth());
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_HEIGHT, tile.getHeight());
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_BITMAP, BitmapHelper.getByteArrayOfBitmap(tile.getBitmap()));
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_NUM_OF_GRID_COLUMNS, numOfColumns);
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_GRID_COLUMNWIDTH, colWidth);
				
		return db.insertOrThrow(FracturePhotoDBModel.getTileTableName(), null, values);
	}

	public Cursor loadSquareTilesDetailsOfPuzzleId(String pzlRowId) {
		 return db.query(FracturePhotoDBModel.getTileTableName(), null, FracturePhotoDBModel.COL_BASE_ROW_ID+" = '"+pzlRowId+"'", null, null, null, null);
	}

	public long updatePuzzle(String oldRowId, String puzzleName, String filePath, int numOfPieces) {
		ContentValues values = new ContentValues();
		long id = System.currentTimeMillis();
		values.put(FracturePhotoDBModel.COL_ID1, String.valueOf(id));
		///values.put(FracturePhotoDBModel.COL_ID1, oldRowId);
		values.put(FracturePhotoDBModel.COL_PZLNAME, puzzleName);
		values.put(FracturePhotoDBModel.COL_PHOTOPATH, filePath);
		values.put(FracturePhotoDBModel.COL_NUM_OF_PIECES, numOfPieces);
		values.put(FracturePhotoDBModel.COL_DATE, new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime()));
		values.put(FracturePhotoDBModel.COL_TIME, new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Calendar.getInstance().getTime()));
			 
		return (db.update(FracturePhotoDBModel.getPuzzleHistoryTableName(), values, FracturePhotoDBModel.COL_ID1+"= ?", new String[]{oldRowId}) > 0 ? id : -1);
	}

	/*public int updateSquareTileDetails(String oldRowId, long rowId, int numOfPieces, SquareTile tile, String numOfColumns, String colWidth) {
		ContentValues values = new ContentValues();
		values.put(FracturePhotoDBModel.COL_BASE_ROW_ID, rowId+"");
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_ID, tile.getTileId());
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_CUR_POS, tile.getCurrentPosition());
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_ORIG_POS, tile.getTileOriginalPosition());
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_ROTATION, tile.getRotation());
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_WIDTH, tile.getWidth());
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_HEIGHT, tile.getHeight());
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_BITMAP, BitmapUtils.getByteArrayOfBitmap(tile.getBitmap()));
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_NUM_OF_GRID_COLUMNS, numOfColumns);
		values.put(FracturePhotoDBModel.COL_SQUARE_TILE_GRID_COLUMNWIDTH, colWidth);
				
		return db.update(FracturePhotoDBModel.getTileTableName(), null, FracturePhotoDBModel.COL_BASE_ROW_ID+"= ?", new String[]{oldRowId});
	}*/

	public int deleteSquareTilesAssociatedWithBaseRowId(String oldRowId) {
		return db.delete(FracturePhotoDBModel.getTileTableName(), FracturePhotoDBModel.COL_BASE_ROW_ID + "= ?", new String[]{oldRowId});
	}
	
	public int deleteShatteredTilesAssociatedWithBaseRowId(String oldRowId) {
		return db.delete(FracturePhotoDBModel.getShatteredTileTableName(), FracturePhotoDBModel.COL_BASE_ROW_ID + "= ?", new String[]{oldRowId});
	}
	
	public int deletePuzzleHistoryWithId(String pzlRowId) {
		return db.delete(FracturePhotoDBModel.getPuzzleHistoryTableName(), FracturePhotoDBModel.COL_ID1 + "= ?", new String[]{pzlRowId});
	}

	public long savePuzzleHistory(String puzzleName, String filePath, int numOfPieces, String puzzleType) {
		ContentValues values = new ContentValues();
		long id = System.currentTimeMillis();
		values.put(FracturePhotoDBModel.COL_ID1, String.valueOf(id));
		values.put(FracturePhotoDBModel.COL_PZLNAME, puzzleName);
		values.put(FracturePhotoDBModel.COL_PHOTOPATH, filePath);
		values.put(FracturePhotoDBModel.COL_NUM_OF_PIECES, numOfPieces);
		values.put(FracturePhotoDBModel.COL_DATE, new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime()));
		values.put(FracturePhotoDBModel.COL_TIME, new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Calendar.getInstance().getTime()));
		values.put(FracturePhotoDBModel.COL_TYPE_OF_PUZZLE, puzzleType);
		
		return (db.insertOrThrow(FracturePhotoDBModel.getPuzzleHistoryTableName(), null, values) != -1 ? id : 0);
	}

	public long saveShatteredTilesDetails(long newRowId, ShatteredTile tile) {
		
		ContentValues values = new ContentValues();
		values.put(FracturePhotoDBModel.COL_BASE_ROW_ID2, String.valueOf(newRowId));
		values.put(FracturePhotoDBModel.COL_SHATTERED_TILE_CURRENT_X, tile.getCurrentX());
		values.put(FracturePhotoDBModel.COL_SHATTERED_TILE_CURRENT_Y, tile.getCurrentY());
		values.put(FracturePhotoDBModel.COL_SHATTERED_TILE_ID, tile.getTileId());
		values.put(FracturePhotoDBModel.COL_SHATTERED_TILE_ORIGINAL_X1, tile.getX1());
		values.put(FracturePhotoDBModel.COL_SHATTERED_TILE_ORIGINAL_Y1, tile.getY1());
		values.put(FracturePhotoDBModel.COL_SHATTERED_TILE_ORIGINAL_X2, tile.getX2());
		values.put(FracturePhotoDBModel.COL_SHATTERED_TILE_ORIGINAL_Y2, tile.getY2());
		values.put(FracturePhotoDBModel.COL_SHATTERED_TILE_ROTATION, tile.getOrientation());
		values.put(FracturePhotoDBModel.COL_SHATTERED_TILE_ORIGINAL_BITMAP, BitmapHelper.getByteArrayOfBitmap(tile.getOriginalBitmap()));
		values.put(FracturePhotoDBModel.COL_SHATTERED_TILE_ROTATED_BITMAP, new byte[0]/*BitmapHelper.getByteArrayOfBitmap(tile.getDisplayBitmap()*/);
		values.put(FracturePhotoDBModel.COL_SHATTERED_TILE_ISDROPPED, String.valueOf(tile.isDroppedOnPuzzleView()));
		values.put(FracturePhotoDBModel.COL_SHATTERED_TILE_SURROUNDINGTILES, Utils.getArrayListItemsAsStringInSingleLine(tile.getSurroundingTiles()));
		values.put(FracturePhotoDBModel.COL_SHATTERED_TILE_IS_RECYCLED, String.valueOf(tile.isTileRecycled()));
		values.put(FracturePhotoDBModel.COL_SHATTERED_TILE_ATTACHED_TILES, Utils.getArrayListItemsAsStringInSingleLine(tile.getAttachedWith()));
		values.put(FracturePhotoDBModel.COL_SHATTERED_PUZZLE_PATTERN_TYPE, tile.getPatternType());
		values.put(FracturePhotoDBModel.COL_SHATTERED_TILE_CURRENT_POSITION, tile.getCurrentPosition());
		values.put(FracturePhotoDBModel.COL_SHATTERED_TILE_CENTER_PIECE, String.valueOf(tile.isCenterPieceOfPuzzle()));
		 
		return db.insertOrThrow(FracturePhotoDBModel.getShatteredTileTableName(), null, values);
	}

	public Cursor loadShatteredTilesDetailsOfPuzzleId(String pzlRowId) {
		 return db.query(FracturePhotoDBModel.getShatteredTileTableName(), null, FracturePhotoDBModel.COL_BASE_ROW_ID+" = '"+pzlRowId+"'", null, null, null, null);
	}

}