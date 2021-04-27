package com.logictreeit.android.fracturedphoto.db;

public class FracturePhotoDBModel {
	// column names
	public static final String COL_ID1 = "_id";
	public static final String COL_PZLNAME = "PzlName";
	public static final String COL_TIME = "Time";
	public static final String COL_DATE = "Date";
	public static final String COL_PHOTOPATH = "PhotoPath";
	public static final String COL_NUM_OF_PIECES = "NumOfPieces";
	public static final String COL_TYPE_OF_PUZZLE = "PuzzleType";

	private static final String PUZZLE_HISTORY_TABLE_NAME = "PuzzleTable";
	private static final String DB_NAME = "FracturePhotoDB";
	private static final int DB_VERSION = 10;

	private static final String puzzleHistoryTableAllColumns[] = {COL_ID1, COL_PZLNAME,
		COL_PHOTOPATH, COL_NUM_OF_PIECES, COL_DATE, COL_TIME, COL_TYPE_OF_PUZZLE};

	private static final String puzzleHistoryColumnTypes[] = {
		"text primary key", "text", "text", "text", "text", "text", "text"};

	public static String[] getPuzzleHistoryAllColumns() {
		return puzzleHistoryColumnTypes;
	}

	public static String getPuzzleHistoryCreateString() {
		StringBuilder query = new StringBuilder();
		for (int i = 0; i < puzzleHistoryTableAllColumns.length; ++i) {
			if (i != 0){
				query.append(", ");
			}
			query.append((new StringBuilder(String.valueOf(puzzleHistoryTableAllColumns[i]))).append(" ").append(puzzleHistoryColumnTypes[i]).toString());
		}
		return query.toString();
	}

	public static String getPuzzleHistoryTableName() {
		return PUZZLE_HISTORY_TABLE_NAME;
	}

	public static String getDBName() {
		return DB_NAME;
	}

	public static int getDBVersion() {
		return DB_VERSION;
	}

	public static final String COL_ID2 = "_id";
	public static final String COL_BASE_ROW_ID = "BaseRowId";
	public static final String COL_SQUARE_TILE_ID = "TileId";
	public static final String COL_SQUARE_TILE_ORIG_POS = "OrigPos";
	public static final String COL_SQUARE_TILE_CUR_POS = "CurPos";
	public static final String COL_SQUARE_TILE_ROTATION = "Rotation";
	public static final String COL_SQUARE_TILE_WIDTH = "Width";
	public static final String COL_SQUARE_TILE_HEIGHT = "Height";
	public static final String COL_SQUARE_TILE_BITMAP = "Bitmap";
	public static final String COL_SQUARE_TILE_NUM_OF_GRID_COLUMNS = "ColsNum";
	public static final String COL_SQUARE_TILE_GRID_COLUMNWIDTH = "ColWidth";

	private static final String TILE_TABLE_NAME = "SquareTilesDetailsTable";


	private static final String tileTableAllColumns[] = { COL_ID2, COL_BASE_ROW_ID, COL_SQUARE_TILE_ID,
		COL_SQUARE_TILE_ORIG_POS, COL_SQUARE_TILE_CUR_POS, COL_SQUARE_TILE_HEIGHT, COL_SQUARE_TILE_ROTATION, COL_SQUARE_TILE_WIDTH, COL_SQUARE_TILE_BITMAP, COL_SQUARE_TILE_NUM_OF_GRID_COLUMNS, COL_SQUARE_TILE_GRID_COLUMNWIDTH };

	private static final String tileTableColumnTypes[] = {
		"integer primary key autoincrement", "text", "text", "text", "text",  "text", "text", "text", "BLOB", "text", "text" };


	public static String[] getTileTableAllColumns() {
		return tileTableColumnTypes;
	}

	public static String getTileCreateString() {
		StringBuilder query = new StringBuilder();
		for (int i = 0; i < tileTableAllColumns.length; ++i) {
			if (i != 0){
				query.append(", ");
			}
			query.append((new StringBuilder(String.valueOf(tileTableAllColumns[i]))).append(" ").append(tileTableColumnTypes[i]).toString());
		}
		return query.toString();
	}

	public static String getTileTableName() {
		return TILE_TABLE_NAME;
	}

	public static final String COL_ID3 = "_id";
	public static final String COL_BASE_ROW_ID2 = "BaseRowId";
	public static final String COL_SHATTERED_TILE_ID = "TileId";
	public static final String COL_SHATTERED_TILE_CURRENT_X = "CurrentX";
	public static final String COL_SHATTERED_TILE_CURRENT_Y = "CurrentY";
	public static final String COL_SHATTERED_TILE_ORIGINAL_X1 = "OriginalX1";
	public static final String COL_SHATTERED_TILE_ORIGINAL_Y1 = "OriginalY1";
	public static final String COL_SHATTERED_TILE_ORIGINAL_X2 = "OriginalX2";
	public static final String COL_SHATTERED_TILE_ORIGINAL_Y2 = "OriginalY2";
	public static final String COL_SHATTERED_TILE_ORIGINAL_BITMAP = "OriginalBitmap";
	public static final String COL_SHATTERED_TILE_ROTATED_BITMAP = "RatatedBitmap";
	public static final String COL_SHATTERED_TILE_ROTATION = "Rotation";
	public static final String COL_SHATTERED_TILE_ISDROPPED = "IsDropped";
	public static final String COL_SHATTERED_TILE_SURROUNDINGTILES = "SurroundingTiles";
	public static final String COL_SHATTERED_TILE_ATTACHED_TILES = "AttachedTiles";
	public static final String COL_SHATTERED_TILE_IS_RECYCLED = "IsRecycled";
	public static final String COL_SHATTERED_PUZZLE_PATTERN_TYPE = "PatternType";
	public static final String COL_SHATTERED_TILE_CURRENT_POSITION = "CurrentPosition";
	public static final String COL_SHATTERED_TILE_CENTER_PIECE = "centerPiece";

	private static final String SHATTERED_TILE_TABLE_NAME = "ShatteredTilesDetailsTable";

	private static final String shatteredTilesTableAllColumns[] = { COL_ID3, COL_BASE_ROW_ID2, COL_SHATTERED_TILE_ID,
		COL_SHATTERED_TILE_CURRENT_X, COL_SHATTERED_TILE_CURRENT_Y, COL_SHATTERED_TILE_ORIGINAL_X1, COL_SHATTERED_TILE_ORIGINAL_Y1,COL_SHATTERED_TILE_ORIGINAL_X2, COL_SHATTERED_TILE_ORIGINAL_Y2,
		COL_SHATTERED_TILE_ROTATION, COL_SHATTERED_TILE_ORIGINAL_BITMAP, COL_SHATTERED_TILE_ROTATED_BITMAP, COL_SHATTERED_TILE_ISDROPPED, COL_SHATTERED_TILE_SURROUNDINGTILES, COL_SHATTERED_TILE_ATTACHED_TILES, COL_SHATTERED_TILE_IS_RECYCLED, COL_SHATTERED_PUZZLE_PATTERN_TYPE, COL_SHATTERED_TILE_CURRENT_POSITION, COL_SHATTERED_TILE_CENTER_PIECE};

	private static final String shatteredTilesTableColumnTypes[] = {
		"integer primary key autoincrement", "text", "text", "text", "text", "text", "text", "text", "text", "text", "BLOB", "BLOB", "text", "text", "text", "text", "integer", "integer", "text" };


	public static String[] getShatteredTileTableAllColumns() {
		return shatteredTilesTableAllColumns;
	}

	public static String getShatteredTileCreateString() {
		StringBuilder query = new StringBuilder();
		for (int i = 0; i < shatteredTilesTableAllColumns.length; ++i) {
			if (i != 0)
				query.append(", ");
			query.append((new StringBuilder(String.valueOf(shatteredTilesTableAllColumns[i])))
					.append(" ").append(shatteredTilesTableColumnTypes[i]).toString());
		}
		return query.toString();
	}

	public static String getShatteredTileTableName() {
		return SHATTERED_TILE_TABLE_NAME;
	}

}
