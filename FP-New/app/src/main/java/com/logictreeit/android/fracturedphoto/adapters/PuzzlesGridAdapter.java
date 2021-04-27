package com.logictreeit.android.fracturedphoto.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.dran.fracturedphoto.R;
import com.logictreeit.android.fracturedphoto.activities.PuzzlesGalleryActivity;
import com.logictreeit.android.fracturedphoto.models.GalleryPuzzle;
import com.logictreeit.android.fracturedphoto.utils.Utils;

import java.util.ArrayList;

public class PuzzlesGridAdapter extends BaseAdapter {
    private final PuzzlesGalleryActivity activity;
    private final ArrayList<GalleryPuzzle> dataset;
    private final LayoutInflater inflater;
    private final float cornerRadius;

    public PuzzlesGridAdapter(PuzzlesGalleryActivity activity, ArrayList<GalleryPuzzle> dataset) {
        this.dataset = dataset;
        this.activity = activity;
        this.inflater = activity.getLayoutInflater();
        this.cornerRadius = Utils.convertDpToPixels(10, activity);
    }

    @Override
    public int getCount() {
        return dataset.size();
    }

    @Override
    public Object getItem(int position) {
        return dataset.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        final ViewHolder holder;
        if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
            rowView = inflater.inflate(R.layout.gallery_puzzle_row, null);
            holder = new ViewHolder();
            holder.downloadImage = rowView.findViewById(R.id.downloadImage);
            holder.newLayout = rowView.findViewById(R.id.newLayout);
            holder.puzzleImageView = rowView.findViewById(R.id.puzzleImageView);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        final GalleryPuzzle puzzle = dataset.get(position);
        if (puzzle.isPurchased()){
            holder.downloadImage.setVisibility(View.GONE);
        } else {
            holder.downloadImage.setVisibility(View.VISIBLE);
        }
        holder.downloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.goForDownloadPuzzle(puzzle);
            }
        });

        if (puzzle.isNew()){
            holder.newLayout.setVisibility(View.VISIBLE);
        } else {
            holder.newLayout.setVisibility(View.GONE);
        }

        Glide.with(activity)
                .asBitmap()
                .load(puzzle.getPuzzle_image())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .centerCrop()
                .dontAnimate()
                .into(new BitmapImageViewTarget(holder.puzzleImageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(activity.getResources(), resource);
                        circularBitmapDrawable.setCornerRadius(cornerRadius);
                        circularBitmapDrawable.setAntiAlias(true);
                        holder.puzzleImageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
        return rowView;
    }

    private static class ViewHolder {
        ImageView puzzleImageView;
        ImageView newLayout;
        ImageView downloadImage;
    }
}
