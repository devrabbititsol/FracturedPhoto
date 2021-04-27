package com.logictreeit.android.fracturedphoto.helpers;

public class Mask {

	int resourceID;
	float x1, y1, x2, y2;
	String name;
	boolean isCenterMask;

	public Mask(String name, int resourceID, float x1, float y1, float x2, float y2, float wSF, float hSF, boolean isCenterMask) {
		this.name = name;
		this.resourceID = resourceID;
		this.x1 = x1 * wSF;
		this.y1 = y1 * hSF;
		this.x2 = x2 * wSF;
		this.y2 = y2 * hSF;
		this.isCenterMask = isCenterMask;
	}
	
	public Mask() {
		super();
	}

	public boolean isCenterMask() {
		return isCenterMask;
	}

	public void setCenterMask(boolean isCenterMask) {
		this.isCenterMask = isCenterMask;
	}

	public int getResourceID() {
		return resourceID;
	}

	public void setResourceID(int resourceID) {
		this.resourceID = resourceID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getResourceId() {
		return resourceID;
	}

	public void setResourceId(int id) {
		this.resourceID = id;
	}

	public float getX1() {
		return x1;
	}

	public void setX1(int x1) {
		this.x1 = x1;
	}

	public float getY1() {
		return y1;
	}

	public void setY1(int y1) {
		this.y1 = y1;
	}
	public float getX2() {
		return x2;
	}

	public void setX2(int x2) {
		this.x2 = x2;
	}

	public float getY2() {
		return y2;
	}

	public void setY2(int y2) {
		this.y2 = y2;
	}
}
