package com.logictreeit.android.fracturedphoto.helpers;
 
 
import com.logictreeit.android.fracturedphoto.models.ShatteredTile;

public class CheckForPuzzleSolveHelper {

	private static final int TOLERENCE   = 30;
	private static final int X_TOLERENCE = TOLERENCE;
	private static final int Y_TOLERENCE = TOLERENCE;

	@SuppressWarnings("unused")
	private static boolean isTileProperlyPlaced(ShatteredTile tile
			/*, int initialTolerenceX, int initialTolerenceY*/) {
		return tile.getOrientation() == 0 && isTileValidIn_XY_Region(tile, 10, 10);
	}

	private static boolean isTileValidIn_XY_Region(ShatteredTile tile, int initialTolerenceX, int initialTolerenceY) {
		return isXValid(tile, initialTolerenceX) && isYValid(tile, initialTolerenceY);
	}

	private static boolean isXValid(ShatteredTile tile, int initialTolerenceX) {
		return (tile.getX1() + initialTolerenceX - X_TOLERENCE <= tile.getCurrentX()) && (tile.getX1() + initialTolerenceX + X_TOLERENCE >= tile.getCurrentX());
	}

	private static boolean isYValid(ShatteredTile tile, int initialTolerenceY) {
		return (tile.getY1() + initialTolerenceY - Y_TOLERENCE <= tile.getCurrentY()) && (tile.getY1() + initialTolerenceY + Y_TOLERENCE >= tile.getCurrentY());
	}

	public static boolean isTileProperlyPlacedToAttach(ShatteredTile tile, ShatteredTile movingTile) { 
		short angle1 = tile.getOrientation();
		short angle2 = movingTile.getOrientation();
		 
		if(angle1 == 0 && angle2 == 0){
		 	return is_X_Valid_In_0_Rotation(tile, movingTile) && is_Y_Valid_In_0_Rotation(tile, movingTile);
		}else if((angle1 == 1 && angle2 == -3) || (angle1 == -3 && angle2 == 1) || (angle1 == 1 && angle2 == 1) || (angle1 == -3 && angle2 == -3)){
			return is_X_Valid_In_90_Rotation(tile, movingTile) && is_Y_Valid_In_90_Rotation(tile, movingTile);
		}else if((angle1 == 2 && angle2 == -2) || (angle1 == -2 && angle2 == 2) || (angle1 == 2 && angle2 == 2) || (angle1 == -2 && angle2 == -2)){
			return is_X_Valid_In_180_Rotation(tile, movingTile) && is_Y_Valid_In_180_Rotation(tile, movingTile);
		}else if((angle1 == 3 && angle2 == -1) || (angle1 == -1 && angle2 == 3) || (angle1 == 3 && angle2 == 3) || (angle1 == -1 && angle2 == -1)){
			return is_X_Valid_In_270_Rotation(tile, movingTile) && is_Y_Valid_In_270_Rotation(tile, movingTile);
		}
		return false;
	}

	private static boolean is_X_Valid_In_0_Rotation(ShatteredTile tile, ShatteredTile movingTile) {
		return (tile.getCurrentX() - tile.getX1() + movingTile.getX1() - X_TOLERENCE <=  movingTile.getCurrentX()) && (movingTile.getX1() + tile.getCurrentX() - tile.getX1() + X_TOLERENCE >= movingTile.getCurrentX());
	}

	private static boolean is_Y_Valid_In_0_Rotation(ShatteredTile tile, ShatteredTile movingTile) {
		return tile.getCurrentY() - tile.getY1() + movingTile.getY1() - Y_TOLERENCE <=  movingTile.getCurrentY() && (movingTile.getY1() + tile.getCurrentY() - tile.getY1() + Y_TOLERENCE >= movingTile.getCurrentY());
	}

	private static boolean is_X_Valid_In_90_Rotation(ShatteredTile tile, ShatteredTile movingTile ) {
		return (movingTile.getX1() - tile.getX1() - X_TOLERENCE <= movingTile.getCurrentY() - tile.getCurrentY() && movingTile.getCurrentY() - tile.getCurrentY() <= movingTile.getX1() - tile.getX1() + X_TOLERENCE ); 
	}

	private static boolean is_Y_Valid_In_90_Rotation(ShatteredTile tile, ShatteredTile movingTile ) {
		return (Math.abs(movingTile.getY2() - tile.getY2()) - Y_TOLERENCE <= Math.abs(movingTile.getCurrentX() - tile.getCurrentX()) &&  Math.abs(movingTile.getCurrentX() - tile.getCurrentX()) <= Math.abs(movingTile.getY2() - tile.getY2()) + Y_TOLERENCE);
	}

	private static boolean is_X_Valid_In_180_Rotation(ShatteredTile tile, ShatteredTile movingTile ) {
		return (Math.abs(movingTile.getX2() - tile.getX2()) - X_TOLERENCE <=  Math.abs(movingTile.getCurrentX() - tile.getCurrentX()) && Math.abs(movingTile.getX2()  - tile.getX2()) + X_TOLERENCE >= Math.abs(movingTile.getCurrentX()- tile.getCurrentX()));
	}

	private static boolean is_Y_Valid_In_180_Rotation(ShatteredTile tile, ShatteredTile movingTile ) {
		return (Math.abs(movingTile.getY2() - tile.getY2()) - Y_TOLERENCE <=  Math.abs(movingTile.getCurrentY() - tile.getCurrentY()) && Math.abs(movingTile.getY2()  - tile.getY2()) + Y_TOLERENCE >= Math.abs(movingTile.getCurrentY()- tile.getCurrentY()));
	}

	private static boolean is_X_Valid_In_270_Rotation(ShatteredTile tile, ShatteredTile movingTile ) {
		return (Math.abs(movingTile.getY1() - tile.getY1()) - X_TOLERENCE <= Math.abs(movingTile.getCurrentX() - tile.getCurrentX()) && Math.abs(movingTile.getCurrentX() - tile.getCurrentX()) <= Math.abs(movingTile.getY1() - tile.getY1()) + X_TOLERENCE ); 
	}

	private static boolean is_Y_Valid_In_270_Rotation(ShatteredTile tile, ShatteredTile movingTile) {
		return (Math.abs(movingTile.getX2() - tile.getX2()) - Y_TOLERENCE <= Math.abs(movingTile.getCurrentY() - tile.getCurrentY()) &&  Math.abs(movingTile.getCurrentY() - tile.getCurrentY()) <= Math.abs(movingTile.getX2() - tile.getX2()) + Y_TOLERENCE);
	}
}
