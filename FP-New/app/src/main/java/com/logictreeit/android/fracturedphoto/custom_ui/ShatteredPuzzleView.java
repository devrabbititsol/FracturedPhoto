package com.logictreeit.android.fracturedphoto.custom_ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.dran.fracturedphoto.R;
import com.logictreeit.android.fracturedphoto.activities.PlayShatteredPuzzleActivity;
import com.logictreeit.android.fracturedphoto.app.FracturePhotoApplication;
import com.logictreeit.android.fracturedphoto.helpers.CheckForPuzzleSolveHelper;
import com.logictreeit.android.fracturedphoto.models.ShatteredTile;
import com.logictreeit.android.fracturedphoto.utils.Utils;

public class ShatteredPuzzleView extends View implements OnGestureListener {

    private GestureDetector gestureDetector;
    private ArrayList<ShatteredTile> originalTilesList = new ArrayList<ShatteredTile>();
    private ArrayList<ShatteredTile> droppedTilesList = new ArrayList<ShatteredTile>();
    public ShatteredTile tappedTile;
    private PlayShatteredPuzzleActivity puzzleActivity;
    private Bitmap photoBitmap;

    public ShatteredPuzzleView(Context context) {
        super(context);
    }

    public ShatteredPuzzleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ShatteredPuzzleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.parseColor("#CCF8F8F8"));
        /*if (photoBitmap == null && droppedTilesList != null && droppedTilesList.size() >= 1) {
            ShatteredTile topTile = null;
            int size = droppedTilesList.size();
            for (int i = 0; i < size; ++i) {
                ShatteredTile tile = droppedTilesList.get(i);
                //here we should draw all the tiles...
                //but we should draw the top tile on top of all the tiles.
                //(i.e draw the top tile on canvas after all tiles have been drawn).
                if (tile.isTopTile()) {
                    topTile = tile;
                } else {
                    tile.drawItOnCanvas(canvas);
                }
            }
            if (topTile != null) {
                topTile.setTopTile(true);
                topTile.drawItOnCanvas(canvas);
            }
        } else if (photoBitmap != null && isPuzzleSolved()) {
            canvas.drawBitmap(photoBitmap, 0, 0, null);
        }*/

        if (photoBitmap == null && droppedTilesList != null && droppedTilesList.size() >= 1) {
            int size = droppedTilesList.size();
            Collections.sort(droppedTilesList, new Comparator<ShatteredTile>() {
                @Override
                public int compare(ShatteredTile t1, ShatteredTile t2) {
                    if (t1.getAttachedWith().size() > t2.getAttachedWith().size()) {
                        return -1;
                    } else if (t1.getAttachedWith().size() < t2.getAttachedWith().size()) {
                        return 1;
                    }
                    return 0;
                }
            });
            for (int i = 0; i < size; ++i) {
                ShatteredTile tile = droppedTilesList.get(i);
                //Here we should draw all the tiles as follows...
                //The individual pieces that have not been connected to other pieces should always appear in front of the groups
                //of pieces that are joined together when they occupy the same area.
                //Ideally the group of pieces can be moved while the individual pieces remain visible.
                /*if (tile.isSingleTile()) {
                    singleTilesList.add(tile);
                } else {
                    tile.drawItOnCanvas(canvas);
                }*/
                tile.drawItOnCanvas(canvas);
            }
            /*for (ShatteredTile singleTile : singleTilesList) {
                singleTile.drawItOnCanvas(canvas);
            }
            singleTilesList.clear();*/
        } else if (photoBitmap != null && isPuzzleSolved()) {
            canvas.drawBitmap(photoBitmap, 0, 0, null);
        }
    }

    /*private int getNumOfTopTiles(ArrayList<ShatteredTile> droppedTilesList) {
        int count = 0;
        int size = droppedTilesList.size();
        for (int i = 0; i < size; ++i) {
            ShatteredTile tile = droppedTilesList.get(i);
            if (tile.isTopTile())
                ++count;
        }
        return count;
    }*/

    @Override
    public boolean onDown(MotionEvent e) {
        /*int size = droppedTilesList.size();
        int touchedTilesCount = 0;
        for (int i = 0; i < size; ++i) {
            ShatteredTile tile = droppedTilesList.get(i);
            if (tile.isTouchWithinMe(e.getX(), e.getY()) && !tile.isTouchOnTransparentAreaOfBitmap((int) e.getX(), (int) e.getY())) {
                ++touchedTilesCount;
            } else if (tile.isTouchWithinMe(e.getX(), e.getY()) && tile.isTouchOnTransparentAreaOfBitmap((int) e.getX(), (int) e.getY())) {
                invalidate();
            }
        }
        Log.v("touchedTilesCount = ", "" + touchedTilesCount);
        boolean isTapFound = false;
        for (int i = 0; i < size; ++i) {
            ShatteredTile tile = droppedTilesList.get(i);
            if (!isTapFound
                    && tile.isTouchWithinMe(e.getX(), e.getY())
                    && !tile.isTouchOnTransparentAreaOfBitmap((int) e.getX(), (int) e.getY())) {

                if (touchedTilesCount != 1) {
                    if ((tile.getBitmapWidth() - 10 <= getWidth() && tile.getBitmapWidth() + 10 >= getWidth()) || (tile.getBitmapHeight() - 10 <= getHeight() && tile.getBitmapHeight() + 10 >= getHeight())) {
                        continue;
                    }
                }
                //Toast.makeText(getContext(), "Tapped Tile W & H = " + tile.getBitmapWidth() + ", " + tile.getBitmapHeight(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(getContext(), "View W & H = " + getWidth() + ", " + getHeight(), Toast.LENGTH_SHORT).show();
                tile.setTapped(true);
                tile.setTopTile(true);
                isTapFound = true;
                tappedTile = tile;
                tappedTile.setTopTile(true);
            } else {
                tile.setTopTile(false);
                tile.setTapped(false);
            }
        }
        Log.v("Num of Top Tiles = ", "" + getNumOfTopTiles(droppedTilesList));
        if (getNumOfTopTiles(droppedTilesList) >= 2) {
            for (int i = 0; i < size; ++i) {
                ShatteredTile tile = droppedTilesList.get(i);
                tile.setTopTile(false);
            }
            Log.v("Invalidating", "Again");
            invalidate();
        }
        //Toast.makeText(getContext(), "Num of Top Tiles = " + getNumOfTopTiles(droppedTilesList), Toast.LENGTH_SHORT).show();
        return true;*/

        int size = droppedTilesList.size();
        for (int x = size - 1; x >= 0; --x) {
            ShatteredTile tile = droppedTilesList.get(x);
            if (tile.isTouchWithinMe(e.getX(), e.getY()) && !tile.isTouchOnTransparentAreaOfBitmap((int) e.getX(), (int) e.getY())) {
                //tile.setTopTile(true);
                tile.setTapped(true);
                tappedTile = tile;
                break;
            } else {
                tile.setTapped(false);
            }
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        System.err.println(new Object() {}.getClass().getEnclosingMethod().getName());
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent startMotionEvent, MotionEvent endMotionEvent, float distanceMovedInX, float distanceMovedInY) {
        if (tappedTile != null && tappedTile.isTapped()) {
            tappedTile.moveTile(tappedTile, startMotionEvent, endMotionEvent, (int) distanceMovedInX, (int) distanceMovedInY);
        }
        invalidate();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        onTouch(this, event);
        return true;
    }

    public void doInitialSetup(PlayShatteredPuzzleActivity puzzleActivity, ArrayList<ShatteredTile> tilesList, ArrayList<ShatteredTile> droppedTilesList) {
        System.err.println(new Object() {
        }.getClass().getEnclosingMethod().getName());
        gestureDetector = new GestureDetector(this.getContext(), this);
        this.originalTilesList = tilesList;
        this.puzzleActivity = puzzleActivity;
    }

    public void doInitialSetupWithDroppedTiles(PlayShatteredPuzzleActivity puzzleActivity, ArrayList<ShatteredTile> tilesList, ArrayList<ShatteredTile> droppedTilesList) {
        System.err.println(new Object() {
        }.getClass().getEnclosingMethod().getName());
        gestureDetector = new GestureDetector(this.getContext(), this);
        this.originalTilesList = tilesList;
        this.puzzleActivity = puzzleActivity;
        this.droppedTilesList = droppedTilesList;
        this.invalidate();
    }

    public void onTileDropFromGallery(int droppedTileID, Point droppingPoint) {
        final ShatteredTile tile = getTileByID(droppedTileID);
        System.out.println("droppedTileID Shatterd puzzle view" + droppedTileID + "point" + droppingPoint);

        // Check Where exactly are we trying to drop the Tile?? Is it out of PuzzleView Boundaries ????
        if (isDroppingPointValid(tile, droppingPoint) && tile != null) {

            for (ShatteredTile droppedTile : droppedTilesList) {
                droppedTile.setTapped(false);
                //droppedTile.setTopTile(false);
            }

            System.err.println("A New Tile with id " + tile.getTileId() + " has been " + "Dropped onto PuzzleView, and its orientation is : " + tile.getOrientation());
            tile.setCurrentX(droppingPoint.x - tile.getBitmapWidth() / 2);
            tile.setCurrentY(droppingPoint.y - tile.getBitmapHeight() / 2);
            tile.setTapped(true);
            //tile.setTopTile(true);
            tappedTile = tile;
            droppedTilesList.add(tile);
            tappedTile.setDroppedOnPuzzleView(true);
            //Now remove the Tile from Gallery
            if (puzzleActivity != null && puzzleActivity.galleryAdapter != null) {
                puzzleActivity.galleryAdapter.removeTileByID(droppedTileID);
            } else {
                Toast.makeText(getContext(), "Something went wrong.\nPlease try again later", Toast.LENGTH_LONG).show();
            }
            //redraw the puzzle view
            this.invalidate();
        }
    }

    private boolean isDroppingPointValid(ShatteredTile tile, Point droppedPoint) {
        int droppedX = droppedPoint.x;
        int droppedY = droppedPoint.y;

        //Toast.makeText(getContext(), "("+droppedX + ", " + droppedY+")", Toast.LENGTH_LONG).show();

        if (tile != null) {
            return (droppedX >= 0/* && droppedX <= (getWidth() - tile.getBitmapWidth() / 2)*/)
                    && (droppedY >= 0/* && droppedY <= (getHeight() - tile.getBitmapHeight() / 2)*/);
        } else {
            return false;
        }

        /*if (tile != null) {
            return droppedX >= 0 && droppedX - tile.getBitmapWidth() / 2 >= 0
                    && droppedY >= 0 && droppedY - tile.getBitmapHeight() / 2 >= 0
                    && droppedY - tile.getBitmapHeight() / 2 >= 0;
        } else {
            return false;
        }*/
    }

    private ShatteredTile getTileByID(int tileID) {
        for (ShatteredTile tile : originalTilesList) {
            if (tile.getTileId() == tileID) {
                return tile;
            }
        }
        return null;
    }

    public void removeTileFromPuzzleView(ShatteredTile tile) {
        droppedTilesList.remove(tile);
        this.invalidate();
    }

    public void onCongratulationsDialogBoxOKClicked(Bitmap photoBitmap) {
        this.photoBitmap = photoBitmap;
        this.invalidate();
    }

    public void onSaveInstanceState_(Bundle outState) {
        FracturePhotoApplication.setOriginalTilesList(originalTilesList);
        FracturePhotoApplication.setDroppedTilesList(droppedTilesList);
    }

    public void onRestoreInstanceState_(Bundle savedInstanceState) {
        droppedTilesList = FracturePhotoApplication.getDroppedTilesList();
        originalTilesList = FracturePhotoApplication.getOriginalTilesList();
        if (droppedTilesList != null && droppedTilesList.size() >= 1) {
            for (ShatteredTile tile : droppedTilesList) {
                System.err.println("onRestoreInstanceState : " + tile.getTileId());
            }
            this.invalidate();
        }
    }

    public void restoreLastTappedTile(int lastTappedTileId) {
        ShatteredTile tile = getTileByID(lastTappedTileId);
        if (tile != null) {
            tile.setTapped(true);
            tappedTile = tile;
        }
    }

    private void doJoinThisTile(ShatteredTile movingTile) {
        for (final ShatteredTile tile : droppedTilesList) {
            if (tile.getTileId() != movingTile.getTileId()) { //Why is this IF??? :: To stop being checked the tile with the same tile
                if (areTilesAttachable(tile, movingTile)) {
                    if (CheckForPuzzleSolveHelper.isTileProperlyPlacedToAttach(tile, movingTile)) {

                        final Bitmap comboBitmap = Utils.getComboBitmapOfTwoTiles(puzzleActivity, tile, movingTile, puzzleActivity.photoBitmap.getWidth(), puzzleActivity.photoBitmap.getHeight(), false);
                        tile.setDisplayBitmap(highlightBitmap(comboBitmap));
                        tile.setBitmapWidth(comboBitmap.getWidth());
                        tile.setBitmapHeight(comboBitmap.getHeight());
                        tile.getSurroundingTiles().addAll(movingTile.getSurroundingTiles());
                        movingTile.getSurroundingTiles().addAll(tile.getSurroundingTiles());
                        tile.setSurroundingTiles(Utils.removeDuplicatesFromList(tile.getSurroundingTiles()));
                        movingTile.setSurroundingTiles(Utils.removeDuplicatesFromList(movingTile.getSurroundingTiles()));
                        for (ShatteredTile droppedTile : droppedTilesList) {
                            droppedTile.setTapped(false);
                            //droppedTile.setTopTile(false);
                        }
                        tile.setTapped(true);
                        //tile.setTopTile(true);*/
                        tile.setX1(Math.min(tile.getX1(), movingTile.getX1()));
                        tile.setY1(Math.min(tile.getY1(), movingTile.getY1()));
                        tile.setX2(Math.max(tile.getX2(), movingTile.getX2()));
                        tile.setY2(Math.max(tile.getY2(), movingTile.getY2()));
                        tile.setCurrentX(Math.min(tile.getCurrentX(), movingTile.getCurrentX()));
                        tile.setCurrentY(Math.min(tile.getCurrentY(), movingTile.getCurrentY()));
                        //no need of below checks
                        if (tile.getCurrentX() <= 0) {
                            tile.setCurrentX(0);
                        }
                        if (tile.getCurrentY() <= 0) {
                            tile.setCurrentY(0);
                        }
                        tile.getAttachedWith().addAll(movingTile.getAttachedWith());
                        movingTile.getAttachedWith().addAll(tile.getAttachedWith());
                        tile.setAttachedWith(Utils.removeDuplicatesFromList(tile.getAttachedWith()));
                        movingTile.setAttachedWith(Utils.removeDuplicatesFromList(movingTile.getAttachedWith()));
                        this.tappedTile = tile;
                        movingTile.setTileRecycled(true);

                        //destroying/removing the tile... stop being drawn on canvas
                        droppedTilesList.remove(movingTile);
                        //now invalidating the view
                        invalidate();
                        Log.v("onSingleTapUp", "onSingleTapUp");
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tile.setDisplayBitmap(comboBitmap);
                                tile.setBitmapWidth(comboBitmap.getWidth());
                                tile.setBitmapHeight(comboBitmap.getHeight());
                                invalidate();

                                if (isPuzzleSolved()) {
                                    puzzleActivity.showSuccessDialogBox();
                                    Utils.doPlaySound(puzzleActivity, R.raw.congratulations_audio);
                                }
                            }
                        }, 300);
                        break;
                    } else {
                        System.out.println("---------------------");
                    }
                } else {
                    /*	System.out.println("---------------------");
                    System.err.println("Angle : " + (doTilesHaveSameOrientation(tile, movingTile)));
					System.err.println("Surrounding tiles : " + (haveTilesAnyCommonNeighbour(tile.getSurroundingTiles(), movingTile.getAttachedWith()) || haveTilesAnyCommonNeighbour(movingTile.getSurroundingTiles(), tile.getAttachedWith())));
					System.err.println("tile.getSurroundingTiles() = "+tile.getSurroundingTiles());
					System.err.println("movingTile.getSurroundingTiles() = "+movingTile.getSurroundingTiles());
					System.err.println("Tile AttachedWith == " + tile.getAttachedWith());
					System.err.println("MovingTile AttachedWith == " + movingTile.getAttachedWith());
					System.err.println("tile.getTileId() = "+tile.getTileId());
					System.err.println("movingTile.getTileId() = "+movingTile.getTileId());
					 */
                }
            }
        }
    }

    private static Bitmap highlightBitmap(Bitmap src) {
        // create new bitmap, which will be painted and becomes result image
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth() + 96, src.getHeight() + 96, Bitmap.Config.ARGB_8888);
        // setup canvas for painting
        Canvas canvas = new Canvas(bmOut);
        // setup default color
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
        // create a blur paint for capturing alpha
        Paint ptBlur = new Paint();
        ptBlur.setMaskFilter(new BlurMaskFilter(15, Blur.NORMAL));
        int[] offsetXY = new int[2];
        // capture alpha into a bitmap
        Bitmap bmAlpha = src.extractAlpha(ptBlur, offsetXY);
        // create a color paint
        Paint ptAlphaColor = new Paint();
        ptAlphaColor.setColor(Color.BLACK);
        // paint color for captured alpha region (bitmap)
        canvas.drawBitmap(bmAlpha, offsetXY[0], offsetXY[1], ptAlphaColor);
        // free memory
        bmAlpha.recycle();
        // paint the image source
        canvas.drawBitmap(src, 0, 0, null);
        // return out final image
        return bmOut;
    }

    private boolean areTilesAttachable(ShatteredTile tile, ShatteredTile movingTile) {
        return doTilesHaveSameOrientation(tile, movingTile) && (haveTilesAnyCommonNeighbour(tile.getSurroundingTiles(), movingTile.getAttachedWith()) || haveTilesAnyCommonNeighbour(movingTile.getSurroundingTiles(), tile.getAttachedWith()));
    }

    private boolean haveTilesAnyCommonNeighbour(ArrayList<Integer> tile1Surroundings, ArrayList<Integer> tile2AttachedWith) {
        for (Integer aw2 : tile2AttachedWith) {
            if (tile1Surroundings.contains(aw2)) {
                return true;
            }
        }
        return false;
    }

    private boolean doTilesHaveSameOrientation(ShatteredTile tile, ShatteredTile movingTile) {
        short angle1 = tile.getOrientation();
        short angle2 = movingTile.getOrientation();
        if ((angle1 >= 0 && angle2 >= 0) || (angle1 < 0 && angle2 < 0)) {
            return angle1 == angle2;
        } else {
            return (Math.abs(angle1) + Math.abs(angle2)) % 4 == 0;
        }
    }

    public boolean isPuzzleSolved() {
        return droppedTilesList != null
                && droppedTilesList.size() == 1
                && puzzleActivity.galleryAdapter.getCount() == 0
                && tappedTile != null
                && tappedTile.getOrientation() == 0;
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (tappedTile != null && tappedTile.isTapped() && droppedTilesList.size() >= 2) {
                    doJoinThisTile(tappedTile);
                }
                break;
            default:
                break;
        }
        return true;
    }
}
