package com.logictreeit.android.fracturedphoto.models;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.view.MotionEvent;

import com.logictreeit.android.fracturedphoto.activities.PlayShatteredPuzzleActivity;
import com.logictreeit.android.fracturedphoto.custom_ui.ShatteredPuzzleView;
import com.logictreeit.android.fracturedphoto.helpers.SurroundingsTilesMapper;

public class ShatteredTile {

    public static final int MOVE_TOLERANCE = 5;
    //x1, y1 are co-ordinate pair of image's top left corner
    //x2, y2 are co-ordinate pair of image's bottom right corner
    private int currentX, currentY, x1, y1, x2, y2, bitmapWidth, bitmapHeight, puzzleViewWidth, puzzleViewHeight, patternType, currentPosition, tileId;
    private ArrayList<Integer> surroundingTiles = new ArrayList<Integer>();
    private ArrayList<Integer> attachedWith = new ArrayList<Integer>();
    private short rotate = 0;
    private Bitmap displayBitmap, originalBitmap;
    private PlayShatteredPuzzleActivity puzzleActivity;
    private ShatteredPuzzleView puzzleView;
    private boolean isTileRecycled, isTapped, isDroppedOnPuzzleView;
    private boolean isCenterPiece;
    private boolean isSharedFromAndroid;
    private boolean isTopTile;
    private boolean isSingleTile;

    public ShatteredTile(PlayShatteredPuzzleActivity puzzleActivity, int tileId, int currentX, int currentY, Bitmap pieceBitmap,
                         Bitmap rotatedBitmap, short rotate, int x1, int y1, int x2, int y2, int patternType, ShatteredPuzzleView puzzleView) {

        this.puzzleActivity = puzzleActivity;
        this.puzzleView = puzzleView;

        setTileId(tileId);
        setCurrentX(currentX);
        setCurrentY(currentY);
        setOrientation(rotate);
        setOriginalBitmap(pieceBitmap);
        setDisplayBitmap(rotatedBitmap);
        setX1(x1);
        setY1(y1);
        setX2(x2);
        setY2(y2);
        setBitmapWidth(getDisplayBitmap().getWidth());
        setBitmapHeight(getDisplayBitmap().getHeight());
        setTapped(false);
        setTileRecycled(false);
        setPatternType(patternType);
        setSurroundingTiles(SurroundingsTilesMapper.getSurroundingTiles(getPatternType(), getTileId()));

        attachedWith.add(getTileId());
        setAttachedWith(attachedWith);
        setTopTile(false);
    }

    public void turnTile(short fromAngle, short toAngle) {
        Matrix m = new Matrix();
        m.setRotate(toAngle * 90);
        this.displayBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), m, false);
        this.rotate = toAngle;
        setBitmapWidth(this.getDisplayBitmap().getWidth());
        setBitmapHeight(this.getDisplayBitmap().getHeight());

        //Fixed : Issue reported by client... (Feb 15, 2015)
        doCalculatePuzzleViewWidthHeight();
        if (!isTileWithinPuzzleViewBoundaries()) {
            System.err.println("Tile is gone out of boundaries when rotated, so bring it back");
            bringTileBackToPuzzleViewBoundaries();
        }
    }

    public void bringTileBackToPuzzleViewBoundaries() {
        if (getCurrentY() + getDisplayBitmap().getHeight() < -MOVE_TOLERANCE) { //tile is beyond the TOP boundary, so bring it back
            setCurrentY(1);
        } else if (getCurrentX() + getDisplayBitmap().getWidth() < -MOVE_TOLERANCE) {//tile is beyond the LEFT boundary, so bring it back
            setCurrentX(1);
        } else if (getCurrentX() > puzzleViewWidth + MOVE_TOLERANCE) {//tile is beyond the RIGHT boundary, so bring it back
            setCurrentX(puzzleViewWidth - getDisplayBitmap().getWidth());
        }
    }

    public void drawItOnCanvas(Canvas c) {
        if (getDisplayBitmap() != null && !getDisplayBitmap().isRecycled()) {
            c.drawBitmap(displayBitmap, currentX, currentY, null);
        }
    }

    public boolean isCenterPieceOfPuzzle() {
        return isCenterPiece;
    }

    public void setCenterPieceOfPuzzle(boolean isCenterPiece) {
        this.isCenterPiece = isCenterPiece;
    }

    public short getOrientation() {
        return rotate;
    }

    public void setOrientation(short rotate) {
        this.rotate = rotate;
    }

    public Bitmap getDisplayBitmap() {
        return displayBitmap;
    }

    public void setDisplayBitmap(Bitmap displayBitmap) {
        this.displayBitmap = displayBitmap;
    }

    public void moveTile(ShatteredTile movingTile, MotionEvent startMotionEvent, MotionEvent endMotionEvent, int distanceMovedInX, int distanceMovedInY) {

        doCalculatePuzzleViewWidthHeight();

        if (endMotionEvent.getY() < puzzleViewHeight) {
            int newX = movingTile.getCurrentX() - distanceMovedInX;
            int newY = movingTile.getCurrentY() - distanceMovedInY;
            if (isMoveValid(movingTile, newX, newY)) {
                movingTile.setCurrentX(newX);
                movingTile.setCurrentY(newY);
            } else {
                //System.err.println("invalid move : tile has gone out of boundaries... hence resetting curX, curY");
                //invalid move.. moving out of boundaries... so reset currentX, currentY
                if (movingTile.getCurrentX() <= 0) {
                    movingTile.setCurrentX(0);
                }
                if (movingTile.getCurrentX() > (puzzleViewWidth - movingTile.getDisplayBitmap().getWidth())) {
                    movingTile.setCurrentX(puzzleViewWidth - movingTile.getDisplayBitmap().getWidth());
                }
                if (movingTile.getCurrentY() <= 0) {
                    movingTile.setCurrentY(0);
                }
            }
        } else {//moving beyond the bottom boundary of puzzelview.. hence drop the tile onto gallery
            if (!puzzleActivity.isTileAddedToGallery(movingTile)) { //check if the tile has been added to gallery already??
                puzzleActivity.onTileDropFromPuzzleView(movingTile);
            }
        }
    }

    private boolean isMoveValid(ShatteredTile movingTile, int newX, int newY) {
        return (newX + movingTile.getCurrentX() >= -MOVE_TOLERANCE //left-validation
                && newY + movingTile.getCurrentY() >= -MOVE_TOLERANCE  //top-validation
                && newX + movingTile.getBitmapWidth() <= puzzleViewWidth + MOVE_TOLERANCE); //right-validation
    }

    private void doCalculatePuzzleViewWidthHeight() {
        if (puzzleViewWidth == 0 || puzzleViewHeight == 0) {
            puzzleViewWidth = puzzleView.getWidth();
            puzzleViewHeight = puzzleView.getHeight();
        }
    }

    public boolean isTapped() {
        return isTapped;
    }

    public void setTapped(boolean isTouched) {
        this.isTapped = isTouched;
    }

    public boolean isTouchWithinMe(float touchedX, float touchedY) {
        return (touchedX >= this.currentX
                && touchedX <= (this.currentX + this.displayBitmap.getWidth())
                && touchedY >= this.currentY
                && touchedY <= (this.currentY + this.displayBitmap.getHeight()));
    }

    public int getTileId() {
        return tileId;
    }

    public void setTileId(int tileId) {
        this.tileId = tileId;
    }

    public int getCurrentX() {
        return currentX;
    }

    public void setCurrentX(int x) {
        this.currentX = x;
    }

    public int getCurrentY() {
        return currentY;
    }

    public void setCurrentY(int y) {
        this.currentY = y;
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getBitmapWidth() {
        return bitmapWidth;
    }

    public void setBitmapWidth(int width) {
        this.bitmapWidth = width;
    }

    public int getBitmapHeight() {
        return bitmapHeight;
    }

    public void setBitmapHeight(int height) {
        this.bitmapHeight = height;
    }

    public Bitmap getOriginalBitmap() {
        return originalBitmap;
    }

    public void setOriginalBitmap(Bitmap originalBitmap) {
        this.originalBitmap = originalBitmap;
    }

    public boolean isDroppedOnPuzzleView() {
        return isDroppedOnPuzzleView;
    }

    public void setDroppedOnPuzzleView(boolean isDroppedOnPuzzleView) {
        this.isDroppedOnPuzzleView = isDroppedOnPuzzleView;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public ArrayList<Integer> getSurroundingTiles() {
        return surroundingTiles;
    }

    public void setSurroundingTiles(ArrayList<Integer> associations) {
        this.surroundingTiles = associations;
    }

    public boolean isTileRecycled() {
        return isTileRecycled;
    }

    public void setTileRecycled(boolean isTileRecycled) {
        this.isTileRecycled = isTileRecycled;
    }

    public ArrayList<Integer> getAttachedWith() {
        return attachedWith;
    }

    public void setAttachedWith(ArrayList<Integer> associatedWith) {
        this.attachedWith = associatedWith;
    }

    public int getPatternType() {
        return patternType;
    }

    public void setPatternType(int patternType) {
        this.patternType = patternType;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public boolean isTileWithinPuzzleViewBoundaries() {
        if (getCurrentY() + getDisplayBitmap().getHeight() < -MOVE_TOLERANCE) { //top
            return false;
        } else if (getCurrentX() + getDisplayBitmap().getWidth() < -MOVE_TOLERANCE) {//left
            return false;
        } else if (getCurrentX() > puzzleViewWidth + MOVE_TOLERANCE) {//right
            return false;
        }
        // no need to check bottom boundary as we are dropping on to gallery
        return true;
    }

    public boolean isSharedFromAndroid() {
        return isSharedFromAndroid;
    }

    public void setSharedFromAndroid(boolean isSharedFromAndroid) {
        this.isSharedFromAndroid = isSharedFromAndroid;
    }

    public boolean isTouchOnTransparentAreaOfBitmap(int touchedX, int touchedY) {
        try {
            return getDisplayBitmap().getPixel(touchedX - currentX, touchedY - currentY) == Color.TRANSPARENT;
        }catch (Exception e){
            e.printStackTrace();
            return true;
        }
    }

    public void setTopTile(boolean isTopTile) {
        this.isTopTile = isTopTile;
    }

    public boolean isTopTile(){
        return isTopTile;
    }

    public boolean isSingleTile() {
        return getAttachedWith() == null || getAttachedWith().size() <= 1;
    }
}
