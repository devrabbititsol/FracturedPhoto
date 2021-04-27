package com.logictreeit.android.fracturedphoto.models;

import java.io.Serializable;

public class GalleryPuzzle implements Serializable {
    String puzzle_image;
    String puzzle_url;
    String puzzle_name;

    public String getPuzzle_name_with_extension() {
        return getPuzzle_name() + ".fp";
    }

    public String getPuzzle_name() {
        return puzzle_name;
    }

    public void setPuzzle_name(String puzzle_name) {
        this.puzzle_name = puzzle_name;
    }

    public String getPuzzle_url() {
        return puzzle_url;
    }

    public void setPuzzle_url(String puzzle_url) {
        this.puzzle_url = puzzle_url;
    }

    public String getPuzzle_image() {
        return puzzle_image;
    }

    public void setPuzzle_image(String puzzle_image) {
        this.puzzle_image = puzzle_image;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    boolean purchased;
    boolean isNew;
}
