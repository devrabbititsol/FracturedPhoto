package com.logictreeit.android.fracturedphoto.models;

import android.graphics.Bitmap;

public class SquareTile {
	
	public Bitmap bitmap;
	public int tileId, tileOriginalPosition, currentPosition, width, height, rotation; 

	public SquareTile(int id, int width, int height, Bitmap bitmap, int position, int rotation) {
		this.width = width;
		this.height = height;
		this.bitmap = bitmap;
		this.tileId = id;
		this.tileOriginalPosition = position;
		this.rotation = rotation;
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bmp) {
		this.bitmap = bmp;
	}

	public int getTileId() {
		return tileId;
	}

	public void setTileId(int tileId) {
		this.tileId = tileId;
	}

	public int getTileOriginalPosition() {
		return tileOriginalPosition;
	}

	public void setTileOriginalPosition(int postion) {
		this.tileOriginalPosition = postion;
	}

	public int getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}
}
