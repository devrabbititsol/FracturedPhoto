package com.logictreeit.android.fracturedphoto.adapters;

import java.util.ArrayList;

import android.content.ClipData;
import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.dran.fracturedphoto.R;
import com.logictreeit.android.fracturedphoto.activities.PlayShatteredPuzzleActivity;
import com.logictreeit.android.fracturedphoto.custom_ui.CustomGalleryView;
import com.logictreeit.android.fracturedphoto.helpers.ShatteredImageDragShadowBuilder;
import com.logictreeit.android.fracturedphoto.models.ShatteredTile;

public class ShatteredPuzzleGalleryAdapter extends BaseAdapter {
    private static final String TAG = "TAG_";
    private final ClipData clipData;
    private PlayShatteredPuzzleActivity puzzleActivity;
    private LayoutInflater inflater;
    private ArrayList<ShatteredTile> tilesList;
    public static int draggingTileID = -1;
    private DragShadowBuilder shadowBuilder;

    public ShatteredPuzzleGalleryAdapter(PlayShatteredPuzzleActivity puzzleActivity, ArrayList<ShatteredTile> tilesList) {
        this.puzzleActivity = puzzleActivity;
        this.inflater = (LayoutInflater)this.puzzleActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.tilesList = tilesList;
        this.clipData =  ClipData.newPlainText("", "");
    }

    @Override
    public int getCount() {
        return this.tilesList != null ? this.tilesList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return this.tilesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View rowView = convertView;
        final ViewHolder holder;
        if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
            rowView = inflater.inflate(R.layout.shattered_gallery_item, null);
            holder = new ViewHolder();
            holder.image = (ImageView) rowView.findViewById(R.id.galleryItemImage);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        ShatteredTile tile = tilesList.get(position);
        holder.image.setImageBitmap(tile.getDisplayBitmap());
        holder.image.setId(tile.getTileId());
        final GestureDetector gestureDetector = new GestureDetector(puzzleActivity, new TileGestureListener(holder.image));
        holder.image.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        return rowView;
    }

    public int getTilePosition(ShatteredTile requiredTile) {
        int pos = 0;
        for(ShatteredTile tile : tilesList){
            if(tile.getTileId() == requiredTile.getTileId()){
                return pos;
            }
            ++pos;
        }
        return pos;
    }

    static class ViewHolder {
        public ImageView image;
    }

    private class TileGestureListener implements GestureDetector.OnGestureListener {

        private final ImageView imageView;

        public TileGestureListener(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            //shadowBuilder = new View.DragShadowBuilder(imageView);
            shadowBuilder = new ShatteredImageDragShadowBuilder(imageView);
            draggingTileID = imageView.getId();
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceMovedInX, float distanceMovedInY) {
            float dY = e2.getY() - e1.getY();
            float dX = e2.getX() - e1.getX();
            if (Math.abs(dX) > Math.abs(dY)) {
                if (dX > 0) {
                    //Log.v(TAG, "Swiping towards right");
                } else {
                    //Log.v(TAG, "Swiping towards left");
                }
            } else {
                if (dY > 0) {
                    //Log.v(TAG, "Swiping towards bottom");
                } else {
                    //Log.v(TAG, "Swiping towards top");
                    imageView.startDrag(clipData, shadowBuilder, imageView, 0);
                }
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    private ShatteredTile getTileByID(int tileID) {
        for (ShatteredTile tile : tilesList) {
            if (tile.getTileId() == tileID) {
                return tile;
            }
        }
        return null;
    }

    public void removeTileByID(int tileID) {
        ShatteredTile tile = getTileByID(tileID);
        if (tile != null) {
            this.tilesList.remove(tile);
            notifyDataSetChanged();
        }
    }

    public void addTileToGallery(ShatteredTile newTile, CustomGalleryView gallery) {
        System.err.println("A New Tile with id "+newTile.getTileId()+" has been Dropped onto Gallery, and its orientation is : "+ newTile.getOrientation());
        int where = whereToInsertNewTileInGallery(newTile);
        System.err.println("where = " + where);
        this.tilesList.add(where, newTile);
        notifyDataSetChanged();
        gallery.setSelection(where);
    }

    private int whereToInsertNewTileInGallery(ShatteredTile newTile) {
        if(tilesList.size() != 0){
            //if NOT all tiles are removed from Gallery
            if (tilesList.get(0).getCurrentPosition() > newTile.getCurrentPosition()) {
                return 0;
            }
            if (tilesList.get(tilesList.size() - 1).getCurrentPosition() < newTile.getCurrentPosition()) {
                return tilesList.size();
            }
            int size = tilesList.size();
            for (int i = 0; i < size; i++) {
                ShatteredTile oldTile = tilesList.get(i);
                if (oldTile.getCurrentPosition() > newTile.getCurrentPosition()) {
                    return i;
                }
            }
            return 0;
        }else{
            System.err.println("All tiles have been removed, so return 0");
            return 0;
        }
    }

}